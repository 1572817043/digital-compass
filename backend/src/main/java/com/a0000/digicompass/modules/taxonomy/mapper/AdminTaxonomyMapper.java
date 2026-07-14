package com.a0000.digicompass.modules.taxonomy.mapper;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AdminTaxonomyMapper {

    private final JdbcTemplate jdbc;

    public AdminTaxonomyMapper(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // Categories
    public record CategoryItem(Long id, String code, String name, String description, int sortOrder, int enabled) {}

    public List<CategoryItem> findAllCategories() {
        return jdbc.query("SELECT id, code, name, description, sort_order, enabled FROM dc_category ORDER BY sort_order",
                (rs, rn) -> new CategoryItem(rs.getLong("id"), rs.getString("code"), rs.getString("name"),
                        rs.getString("description"), rs.getInt("sort_order"), rs.getInt("enabled")));
    }

    public Long insertCategory(String code, String name, String description, int sortOrder, int enabled) {
        jdbc.update("INSERT INTO dc_category (code, name, description, sort_order, enabled) VALUES (?, ?, ?, ?, ?)",
                code, name, description, sortOrder, enabled);
        return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }

    public void updateCategory(Long id, String code, String name, String description, int sortOrder, int enabled) {
        jdbc.update("UPDATE dc_category SET code=?, name=?, description=?, sort_order=?, enabled=? WHERE id=?",
                code, name, description, sortOrder, enabled, id);
    }

    public void updateCategoryStatus(Long id, int enabled) {
        jdbc.update("UPDATE dc_category SET enabled = ? WHERE id = ?", enabled, id);
    }

    // Brands
    public record BrandItem(Long id, String name, int sortOrder) {}

    public List<BrandItem> findAllBrands() {
        return jdbc.query("SELECT id, name, sort_order FROM dc_brand ORDER BY sort_order",
                (rs, rn) -> new BrandItem(rs.getLong("id"), rs.getString("name"), rs.getInt("sort_order")));
    }

    public Long insertBrand(String name, int sortOrder) {
        jdbc.update("INSERT INTO dc_brand (name, sort_order) VALUES (?, ?)", name, sortOrder);
        return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }

    public void updateBrand(Long id, String name, int sortOrder) {
        jdbc.update("UPDATE dc_brand SET name=?, sort_order=? WHERE id=?", name, sortOrder, id);
    }
}
