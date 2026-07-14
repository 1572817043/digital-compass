package com.a0000.digicompass.modules.product.controller;

import com.a0000.digicompass.common.api.ApiResponse;
import com.a0000.digicompass.modules.product.dto.ProductDetail;
import com.a0000.digicompass.modules.product.dto.ProductListItem;
import com.a0000.digicompass.modules.product.entity.Brand;
import com.a0000.digicompass.modules.product.entity.Category;
import com.a0000.digicompass.modules.product.service.ProductService;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/brands")
    public ApiResponse<List<Brand>> brands() {
        return ApiResponse.success(productService.listBrands());
    }

    @GetMapping("/categories")
    public ApiResponse<List<Category>> categories() {
        return ApiResponse.success(productService.listCategories());
    }

    @GetMapping("/products")
    public ApiResponse<List<ProductListItem>> products(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String tagType,
            @RequestParam(required = false) String tagName,
            @RequestParam(required = false) Boolean hasUsedPrice,
            @RequestParam(required = false) Boolean hasPurchaseLink,
            @RequestParam(required = false) String sortBy
    ) {
        return ApiResponse.success(productService.listProducts(
                categoryId, brandId, keyword, minPrice, maxPrice,
                tagType, tagName, hasUsedPrice, hasPurchaseLink, sortBy
        ));
    }

    @GetMapping("/products/{id}")
    public ApiResponse<ProductDetail> productDetail(@PathVariable Long id) {
        return ApiResponse.success(productService.getProductDetail(id));
    }
}
