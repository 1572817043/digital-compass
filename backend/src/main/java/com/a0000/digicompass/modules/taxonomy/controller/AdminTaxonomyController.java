package com.a0000.digicompass.modules.taxonomy.controller;

import com.a0000.digicompass.common.api.ApiResponse;
import com.a0000.digicompass.modules.taxonomy.mapper.AdminTaxonomyMapper;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminTaxonomyController {

    private final AdminTaxonomyMapper taxonomyMapper;

    public AdminTaxonomyController(AdminTaxonomyMapper taxonomyMapper) {
        this.taxonomyMapper = taxonomyMapper;
    }

    // Categories
    @GetMapping("/categories")
    public ApiResponse<List<AdminTaxonomyMapper.CategoryItem>> listCategories() {
        return ApiResponse.success(taxonomyMapper.findAllCategories());
    }

    @PostMapping("/categories")
    public ApiResponse<Long> createCategory(@RequestBody java.util.Map<String, Object> body) {
        String code = (String) body.get("code");
        String name = (String) body.get("name");
        String description = (String) body.get("description");
        int sortOrder = body.get("sortOrder") != null ? ((Number) body.get("sortOrder")).intValue() : 0;
        int enabled = body.get("enabled") != null ? ((Number) body.get("enabled")).intValue() : 1;
        if (code == null || code.isBlank() || name == null || name.isBlank()) {
            throw new IllegalArgumentException("code 和 name 不能为空");
        }
        return ApiResponse.success(taxonomyMapper.insertCategory(code, name, description, sortOrder, enabled));
    }

    @PutMapping("/categories/{id}")
    public ApiResponse<Void> updateCategory(@PathVariable Long id, @RequestBody java.util.Map<String, Object> body) {
        String code = trimToNull((String) body.get("code"));
        String name = trimToNull((String) body.get("name"));
        String description = (String) body.get("description");
        int sortOrder = body.get("sortOrder") != null ? ((Number) body.get("sortOrder")).intValue() : 0;
        int enabled = body.get("enabled") != null ? ((Number) body.get("enabled")).intValue() : 1;
        if (code == null || name == null) {
            throw new IllegalArgumentException("code 和 name 不能为空");
        }
        if (enabled != 0 && enabled != 1) {
            throw new IllegalArgumentException("enabled 只能是 0 或 1");
        }
        taxonomyMapper.updateCategory(id, code, name, description, sortOrder, enabled);
        return ApiResponse.success(null);
    }

    @PutMapping("/categories/{id}/status")
    public ApiResponse<Void> updateCategoryStatus(@PathVariable Long id, @RequestParam int enabled) {
        if (enabled != 0 && enabled != 1) {
            throw new IllegalArgumentException("enabled 只能是 0 或 1");
        }
        taxonomyMapper.updateCategoryStatus(id, enabled);
        return ApiResponse.success(null);
    }

    // Brands
    @GetMapping("/brands")
    public ApiResponse<List<AdminTaxonomyMapper.BrandItem>> listBrands() {
        return ApiResponse.success(taxonomyMapper.findAllBrands());
    }

    @PostMapping("/brands")
    public ApiResponse<Long> createBrand(@RequestBody java.util.Map<String, Object> body) {
        String name = (String) body.get("name");
        int sortOrder = body.get("sortOrder") != null ? ((Number) body.get("sortOrder")).intValue() : 0;
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name 不能为空");
        return ApiResponse.success(taxonomyMapper.insertBrand(name, sortOrder));
    }

    @PutMapping("/brands/{id}")
    public ApiResponse<Void> updateBrand(@PathVariable Long id, @RequestBody java.util.Map<String, Object> body) {
        String name = trimToNull((String) body.get("name"));
        int sortOrder = body.get("sortOrder") != null ? ((Number) body.get("sortOrder")).intValue() : 0;
        if (name == null) {
            throw new IllegalArgumentException("name 不能为空");
        }
        taxonomyMapper.updateBrand(id, name, sortOrder);
        return ApiResponse.success(null);
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
