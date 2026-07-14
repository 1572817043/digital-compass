package com.a0000.digicompass.modules.product.controller;

import com.a0000.digicompass.common.api.ApiResponse;
import com.a0000.digicompass.modules.product.dto.LinkSaveRequest;
import com.a0000.digicompass.modules.product.dto.PriceSaveRequest;
import com.a0000.digicompass.modules.product.dto.ProductDetail;
import com.a0000.digicompass.modules.product.dto.ProductTagItem;
import com.a0000.digicompass.modules.product.dto.ProductTagSaveRequest;
import com.a0000.digicompass.modules.product.dto.SpecSaveRequest;
import com.a0000.digicompass.modules.product.entity.ProductSpec;
import com.a0000.digicompass.modules.product.service.ProductService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/products/{productId}")
public class AdminProductMetaController {

    private final ProductService productService;

    public AdminProductMetaController(ProductService productService) {
        this.productService = productService;
    }

    // ========== Specs ==========

    @GetMapping("/specs")
    public ApiResponse<List<ProductSpec>> listSpecs(@PathVariable Long productId) {
        return ApiResponse.success(productService.listSpecs(productId));
    }

    @PostMapping("/specs")
    public ApiResponse<Long> createSpec(@PathVariable Long productId, @Valid @RequestBody SpecSaveRequest request) {
        return ApiResponse.success(productService.createSpec(productId, request));
    }

    @PutMapping("/specs/{specId}")
    public ApiResponse<Void> updateSpec(@PathVariable Long productId, @PathVariable Long specId, @Valid @RequestBody SpecSaveRequest request) {
        productService.updateSpec(productId, specId, request);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/specs/{specId}")
    public ApiResponse<Void> deleteSpec(@PathVariable Long productId, @PathVariable Long specId) {
        productService.deleteSpec(productId, specId);
        return ApiResponse.success(null);
    }

    // ========== Prices ==========

    @GetMapping("/prices")
    public ApiResponse<List<ProductDetail.PriceInfo>> listPrices(@PathVariable Long productId) {
        return ApiResponse.success(productService.listPrices(productId));
    }

    @PostMapping("/prices")
    public ApiResponse<Long> createPrice(@PathVariable Long productId, @Valid @RequestBody PriceSaveRequest request) {
        return ApiResponse.success(productService.createPrice(productId, request));
    }

    @PutMapping("/prices/{priceId}")
    public ApiResponse<Void> updatePrice(@PathVariable Long productId, @PathVariable Long priceId, @Valid @RequestBody PriceSaveRequest request) {
        productService.updatePrice(productId, priceId, request);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/prices/{priceId}")
    public ApiResponse<Void> deletePrice(@PathVariable Long productId, @PathVariable Long priceId) {
        productService.deletePrice(productId, priceId);
        return ApiResponse.success(null);
    }

    // ========== Links ==========

    @GetMapping("/links")
    public ApiResponse<List<ProductDetail.PurchaseLinkInfo>> listLinks(@PathVariable Long productId) {
        return ApiResponse.success(productService.listLinks(productId));
    }

    @PostMapping("/links")
    public ApiResponse<Long> createLink(@PathVariable Long productId, @Valid @RequestBody LinkSaveRequest request) {
        return ApiResponse.success(productService.createLink(productId, request));
    }

    @PutMapping("/links/{linkId}")
    public ApiResponse<Void> updateLink(@PathVariable Long productId, @PathVariable Long linkId, @Valid @RequestBody LinkSaveRequest request) {
        productService.updateLink(productId, linkId, request);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/links/{linkId}")
    public ApiResponse<Void> deleteLink(@PathVariable Long productId, @PathVariable Long linkId) {
        productService.deleteLink(productId, linkId);
        return ApiResponse.success(null);
    }

    // ========== Tags ==========

    @GetMapping("/tags")
    public ApiResponse<List<ProductTagItem>> listTags(@PathVariable Long productId) {
        return ApiResponse.success(productService.listTags(productId));
    }

    @PostMapping("/tags")
    public ApiResponse<Long> createTag(@PathVariable Long productId, @Valid @RequestBody ProductTagSaveRequest request) {
        return ApiResponse.success(productService.createTag(productId, request));
    }

    @PutMapping("/tags/{tagId}")
    public ApiResponse<Void> updateTag(@PathVariable Long productId, @PathVariable Long tagId, @Valid @RequestBody ProductTagSaveRequest request) {
        productService.updateTag(productId, tagId, request);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/tags/{tagId}")
    public ApiResponse<Void> deleteTag(@PathVariable Long productId, @PathVariable Long tagId) {
        productService.deleteTag(productId, tagId);
        return ApiResponse.success(null);
    }
}
