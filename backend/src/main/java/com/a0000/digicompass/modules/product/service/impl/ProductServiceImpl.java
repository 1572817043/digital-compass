package com.a0000.digicompass.modules.product.service.impl;

import com.a0000.digicompass.modules.product.dto.LinkSaveRequest;
import com.a0000.digicompass.modules.product.dto.PriceSaveRequest;
import com.a0000.digicompass.modules.product.dto.ProductDetail;
import com.a0000.digicompass.modules.product.dto.ProductImageBindRequest;
import com.a0000.digicompass.modules.product.dto.ProductListItem;
import com.a0000.digicompass.modules.product.dto.ProductSaveRequest;
import com.a0000.digicompass.modules.product.dto.ProductTagItem;
import com.a0000.digicompass.modules.product.dto.ProductTagSaveRequest;
import com.a0000.digicompass.modules.product.dto.SpecSaveRequest;
import com.a0000.digicompass.modules.product.entity.Brand;
import com.a0000.digicompass.modules.product.entity.Category;
import com.a0000.digicompass.modules.product.entity.ProductSpec;
import com.a0000.digicompass.modules.product.mapper.ProductMapper;
import com.a0000.digicompass.modules.product.service.ProductService;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;

    public ProductServiceImpl(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    @Override
    public List<Brand> listBrands() {
        return productMapper.findAllBrands();
    }

    @Override
    public List<Category> listCategories() {
        return productMapper.findAllCategories();
    }

    @Override
    public List<ProductListItem> listProducts(
            Long categoryId,
            Long brandId,
            String keyword,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String tagType,
            String tagName,
            Boolean hasUsedPrice,
            Boolean hasPurchaseLink,
            String sortBy
    ) {
        return productMapper.findProducts(
                categoryId, brandId, keyword, minPrice, maxPrice,
                tagType, tagName, hasUsedPrice, hasPurchaseLink, sortBy
        );
    }

    @Override
    public ProductDetail getProductDetail(Long id) {
        ProductDetail detail = productMapper.findDetail(id);
        if (detail == null) {
            throw new IllegalArgumentException("产品不存在");
        }
        if (detail.status() != 1) {
            throw new IllegalArgumentException("产品不存在或已下架");
        }
        return detail;
    }

    @Override
    public ProductDetail getAdminProductDetail(Long id) {
        ProductDetail detail = productMapper.findDetail(id);
        if (detail == null) {
            throw new IllegalArgumentException("产品不存在");
        }
        return detail;
    }

    @Override
    public List<ProductListItem> listAllProducts(Integer status) {
        return productMapper.findAllProducts(status);
    }

    @Override
    public void updateProductStatus(Long id, int status) {
        if (status != 0 && status != 1) throw new IllegalArgumentException("状态只能是 0 或 1");
        productMapper.updateStatus(id, status);
    }

    @Override
    public Long createProduct(ProductSaveRequest request) {
        return productMapper.insertProduct(
                request.categoryId(), request.brandId(), request.name(), request.model(),
                request.summary(), request.coverUrl(), request.officialPrice(),
                request.score(), request.status()
        );
    }

    @Override
    public void updateProduct(Long id, ProductSaveRequest request) {
        productMapper.updateProduct(id,
                request.categoryId(), request.brandId(), request.name(), request.model(),
                request.summary(), request.coverUrl(), request.officialPrice(),
                request.score(), request.status()
        );
    }

    @Override
    public void deleteProduct(Long id) {
        productMapper.deleteProduct(id);
    }

    @Override
    @Transactional
    public void bindImage(Long productId, ProductImageBindRequest request) {
        productMapper.insertProductImage(productId, request.imageUrl(), request.imageType(), request.sortOrder() != null ? request.sortOrder() : 0);
    }

    @Override
    public void deleteImage(Long productId, Long imageId) {
        productMapper.deleteProductImage(imageId);
    }

    @Override
    public int countProducts() {
        return productMapper.countProducts();
    }

    @Override
    public int countCategories() {
        return productMapper.countCategories();
    }

    @Override
    public List<ProductSpec> listSpecs(Long productId) {
        return productMapper.findSpecs(productId);
    }

    @Override
    public Long createSpec(Long productId, SpecSaveRequest request) {
        return productMapper.insertSpec(productId, request.specGroup(), request.specName(), request.specValue(),
                request.sortOrder() != null ? request.sortOrder() : 0);
    }

    @Override
    public void updateSpec(Long productId, Long specId, SpecSaveRequest request) {
        productMapper.updateSpec(specId, productId, request.specGroup(), request.specName(), request.specValue(),
                request.sortOrder() != null ? request.sortOrder() : 0);
    }

    @Override
    public void deleteSpec(Long productId, Long specId) {
        productMapper.deleteSpec(specId, productId);
    }

    @Override
    public List<ProductDetail.PriceInfo> listPrices(Long productId) {
        return productMapper.findPrices(productId);
    }

    @Override
    public Long createPrice(Long productId, PriceSaveRequest request) {
        validatePriceRequest(request);
        return productMapper.insertPrice(productId, request.priceType(), request.platform(),
                request.minPrice(), request.maxPrice(), request.avgPrice(),
                normalizeSampleCount(request.sampleCount()), request.referenceDate(), normalizeSourceType(request.sourceType()), request.remark());
    }

    @Override
    public void updatePrice(Long productId, Long priceId, PriceSaveRequest request) {
        validatePriceRequest(request);
        productMapper.updatePrice(priceId, productId, request.priceType(), request.platform(),
                request.minPrice(), request.maxPrice(), request.avgPrice(),
                normalizeSampleCount(request.sampleCount()), request.referenceDate(), normalizeSourceType(request.sourceType()), request.remark());
    }

    @Override
    public void deletePrice(Long productId, Long priceId) {
        productMapper.deletePrice(priceId, productId);
    }

    private void validatePriceRequest(PriceSaveRequest request) {
        if (request.minPrice() == null && request.maxPrice() == null && request.avgPrice() == null) {
            throw new IllegalArgumentException("至少填写一个价格值");
        }
        if (request.minPrice() != null && request.maxPrice() != null && request.minPrice().compareTo(request.maxPrice()) > 0) {
            throw new IllegalArgumentException("最低价不能大于最高价");
        }
    }

    private int normalizeSampleCount(Integer sampleCount) {
        return sampleCount != null ? sampleCount : 0;
    }

    private String normalizeSourceType(String sourceType) {
        return sourceType != null && !sourceType.isBlank() ? sourceType.trim() : "manual";
    }

    @Override
    public List<ProductDetail.PurchaseLinkInfo> listLinks(Long productId) {
        return productMapper.findLinks(productId);
    }

    @Override
    public Long createLink(Long productId, LinkSaveRequest request) {
        return productMapper.insertLink(productId, request.platform(), request.linkType(), request.title(), request.url(),
                request.sortOrder() != null ? request.sortOrder() : 0,
                request.enabled() != null ? request.enabled() : 1);
    }

    @Override
    public void updateLink(Long productId, Long linkId, LinkSaveRequest request) {
        productMapper.updateLink(linkId, productId, request.platform(), request.linkType(), request.title(), request.url(),
                request.sortOrder() != null ? request.sortOrder() : 0,
                request.enabled() != null ? request.enabled() : 1);
    }

    @Override
    public void deleteLink(Long productId, Long linkId) {
        productMapper.deleteLink(linkId, productId);
    }

    @Override
    public List<ProductTagItem> listTags(Long productId) {
        return productMapper.findTags(productId);
    }

    @Override
    public Long createTag(Long productId, ProductTagSaveRequest request) {
        return productMapper.insertTag(productId, request.tagType(), request.tagName(), request.tagValue(),
                request.sortOrder() != null ? request.sortOrder() : 0);
    }

    @Override
    public void updateTag(Long productId, Long tagId, ProductTagSaveRequest request) {
        productMapper.updateTag(tagId, productId, request.tagType(), request.tagName(), request.tagValue(),
                request.sortOrder() != null ? request.sortOrder() : 0);
    }

    @Override
    public void deleteTag(Long productId, Long tagId) {
        productMapper.deleteTag(tagId, productId);
    }
}
