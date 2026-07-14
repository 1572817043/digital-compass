package com.a0000.digicompass.modules.product.service;

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
import java.math.BigDecimal;
import java.util.List;

public interface ProductService {

    List<Brand> listBrands();

    List<Category> listCategories();

    List<ProductListItem> listProducts(
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
    );

    ProductDetail getProductDetail(Long id);

    ProductDetail getAdminProductDetail(Long id);

    List<ProductListItem> listAllProducts(Integer status);

    void updateProductStatus(Long id, int status);

    Long createProduct(ProductSaveRequest request);

    void updateProduct(Long id, ProductSaveRequest request);

    void deleteProduct(Long id);

    void bindImage(Long productId, ProductImageBindRequest request);

    void deleteImage(Long productId, Long imageId);

    int countProducts();

    int countCategories();

    // Specs
    List<ProductSpec> listSpecs(Long productId);

    Long createSpec(Long productId, SpecSaveRequest request);

    void updateSpec(Long productId, Long specId, SpecSaveRequest request);

    void deleteSpec(Long productId, Long specId);

    // Prices
    List<ProductDetail.PriceInfo> listPrices(Long productId);

    Long createPrice(Long productId, PriceSaveRequest request);

    void updatePrice(Long productId, Long priceId, PriceSaveRequest request);

    void deletePrice(Long productId, Long priceId);

    // Links
    List<ProductDetail.PurchaseLinkInfo> listLinks(Long productId);

    Long createLink(Long productId, LinkSaveRequest request);

    void updateLink(Long productId, Long linkId, LinkSaveRequest request);

    void deleteLink(Long productId, Long linkId);

    // Tags
    List<ProductTagItem> listTags(Long productId);

    Long createTag(Long productId, ProductTagSaveRequest request);

    void updateTag(Long productId, Long tagId, ProductTagSaveRequest request);

    void deleteTag(Long productId, Long tagId);
}
