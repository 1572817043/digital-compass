package com.a0000.digicompass.modules.product.controller;

import com.a0000.digicompass.common.api.ApiResponse;
import com.a0000.digicompass.modules.product.dto.ProductImageBindRequest;
import com.a0000.digicompass.modules.product.dto.ProductDetail;
import com.a0000.digicompass.modules.product.dto.ProductListItem;
import com.a0000.digicompass.modules.product.dto.ProductSaveRequest;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/products")
public class AdminProductController {

    private final ProductService productService;

    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ApiResponse<List<ProductListItem>> list(
            @RequestParam(required = false) Integer status
    ) {
        return ApiResponse.success(productService.listAllProducts(status));
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductDetail> detail(@PathVariable Long id) {
        return ApiResponse.success(productService.getAdminProductDetail(id));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestBody java.util.Map<String, Object> body) {
        int status = body.get("status") != null ? ((Number) body.get("status")).intValue() : 1;
        productService.updateProductStatus(id, status);
        return ApiResponse.success(null);
    }

    @PostMapping
    public ApiResponse<Long> create(@Valid @RequestBody ProductSaveRequest request) {
        return ApiResponse.success(productService.createProduct(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody ProductSaveRequest request) {
        productService.updateProduct(id, request);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/images")
    public ApiResponse<Void> bindImage(@PathVariable Long id, @Valid @RequestBody ProductImageBindRequest request) {
        productService.bindImage(id, request);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}/images/{imageId}")
    public ApiResponse<Void> deleteImage(@PathVariable Long id, @PathVariable Long imageId) {
        productService.deleteImage(id, imageId);
        return ApiResponse.success(null);
    }
}
