package com.a0000.digicompass.modules.ai.knowledge.service.impl;

import com.a0000.digicompass.modules.ai.knowledge.dto.AiKnowledgeChunkItem;
import com.a0000.digicompass.modules.ai.knowledge.dto.AiKnowledgeItem;
import com.a0000.digicompass.modules.ai.knowledge.dto.AiKnowledgeSaveRequest;
import com.a0000.digicompass.modules.ai.knowledge.mapper.AiKnowledgeMapper;
import com.a0000.digicompass.modules.ai.knowledge.service.AiKnowledgeChunkService;
import com.a0000.digicompass.modules.ai.knowledge.service.AiKnowledgeService;
import com.a0000.digicompass.modules.product.dto.ProductDetail;
import com.a0000.digicompass.modules.product.dto.ProductListItem;
import com.a0000.digicompass.modules.product.service.ProductService;
import java.math.BigDecimal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AiKnowledgeServiceImpl implements AiKnowledgeService {

    private static final Logger log = LoggerFactory.getLogger(AiKnowledgeServiceImpl.class);

    private final AiKnowledgeMapper knowledgeMapper;
    private final AiKnowledgeChunkService chunkService;
    private final ProductService productService;

    public AiKnowledgeServiceImpl(AiKnowledgeMapper knowledgeMapper, AiKnowledgeChunkService chunkService, ProductService productService) {
        this.knowledgeMapper = knowledgeMapper;
        this.chunkService = chunkService;
        this.productService = productService;
    }

    @Override
    public List<AiKnowledgeItem> listKnowledge(String keyword, String knowledgeType, Integer status, Long categoryId, Long productId) {
        return knowledgeMapper.findAll(keyword, knowledgeType, status, categoryId, productId);
    }

    @Override
    public AiKnowledgeItem getKnowledge(Long id) {
        AiKnowledgeItem item = knowledgeMapper.findById(id);
        if (item == null) throw new IllegalArgumentException("知识不存在");
        return item;
    }

    @Override
    public Long createKnowledge(AiKnowledgeSaveRequest request) {
        int status = request.status() != null ? request.status() : 1;
        Long id = knowledgeMapper.insert(request.categoryId(), request.productId(), request.title(),
                request.content(), request.knowledgeType(), request.tags(), request.source(), status);
        chunkService.rebuildChunks(id, request.title(), request.content(), request.productId(), request.categoryId());
        return id;
    }

    @Override
    public void updateKnowledge(Long id, AiKnowledgeSaveRequest request) {
        AiKnowledgeItem existing = knowledgeMapper.findById(id);
        if (existing == null) throw new IllegalArgumentException("知识不存在");
        int status = request.status() != null ? request.status() : existing.status();
        knowledgeMapper.update(id,
                request.categoryId() != null ? request.categoryId() : existing.categoryId(),
                request.productId() != null ? request.productId() : existing.productId(),
                request.title() != null ? request.title() : existing.title(),
                request.content() != null ? request.content() : existing.content(),
                request.knowledgeType() != null ? request.knowledgeType() : existing.knowledgeType(),
                request.tags() != null ? request.tags() : existing.tags(),
                request.source() != null ? request.source() : existing.source(),
                status);
        chunkService.rebuildChunks(id,
                request.title() != null ? request.title() : existing.title(),
                request.content() != null ? request.content() : existing.content(),
                request.productId() != null ? request.productId() : existing.productId(),
                request.categoryId() != null ? request.categoryId() : existing.categoryId());
    }

    @Override
    public void deleteKnowledge(Long id) {
        chunkService.deleteChunks(id);
        knowledgeMapper.deleteById(id);
    }

    @Override
    public void rebuildChunks(Long id) {
        AiKnowledgeItem item = knowledgeMapper.findById(id);
        if (item == null) throw new IllegalArgumentException("知识不存在");
        chunkService.rebuildChunks(id, item.title(), item.content(), item.productId(), item.categoryId());
    }

    @Override
    public List<AiKnowledgeChunkItem> listChunks(Long knowledgeId) {
        return chunkService.listChunks(knowledgeId);
    }

    @Override
    public int rebuildProductKnowledge() {
        List<ProductListItem> products = productService.listProducts(null, null, null, null, null, null, null, null, null, null);
        int count = 0;
        for (ProductListItem product : products) {
            try {
                AiKnowledgeItem existing = knowledgeMapper.findByProductId(product.id());
                String content = buildProductKnowledgeText(product);
                String title = "产品知识：" + product.name();

                if (existing != null) {
                    knowledgeMapper.update(existing.id(), product.categoryId(), product.id(), title, content, "product", null, "product", 1);
                    chunkService.rebuildChunks(existing.id(), title, content, product.id(), product.categoryId());
                } else {
                    Long id = knowledgeMapper.insert(product.categoryId(), product.id(), title, content, "product", null, "product", 1);
                    chunkService.rebuildChunks(id, title, content, product.id(), product.categoryId());
                }
                count++;
            } catch (Exception e) {
                log.warn("生成产品知识失败: productId={}, error={}", product.id(), e.getMessage());
            }
        }
        return count;
    }

    private String buildProductKnowledgeText(ProductListItem product) {
        StringBuilder sb = new StringBuilder();
        sb.append(product.name()).append("是").append(product.brandName()).append("品牌的");
        sb.append(product.categoryName()).append("产品");
        if (product.model() != null) sb.append("，型号为").append(product.model());
        sb.append("。");
        if (product.summary() != null) sb.append(product.summary()).append(" ");
        if (product.officialPrice() != null) sb.append("官方起售价约").append(product.officialPrice()).append("元。");

        ProductDetail detail = productService.getProductDetail(product.id());
        if (detail != null) {
            if (!detail.specs().isEmpty()) {
                sb.append("核心参数：");
                for (var spec : detail.specs()) {
                    sb.append(spec.name()).append(" ").append(spec.value()).append("、");
                }
                sb.setLength(sb.length() - 1);
                sb.append("。");
            }
            if (!detail.tags().isEmpty()) {
                sb.append("标签：");
                for (var tag : detail.tags()) {
                    if (tag.tagName() != null) sb.append(tag.tagName()).append("、");
                }
                sb.setLength(sb.length() - 1);
                sb.append("。");
            }
            if (!detail.prices().isEmpty()) {
                for (var price : detail.prices()) {
                    if ("used".equals(price.priceType()) && price.minPrice() != null) {
                        sb.append("二手参考价约").append(price.minPrice()).append("-").append(price.maxPrice()).append("元。");
                        break;
                    }
                }
            }
        }
        return sb.toString();
    }
}
