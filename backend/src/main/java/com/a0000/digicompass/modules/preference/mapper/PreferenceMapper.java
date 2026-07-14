package com.a0000.digicompass.modules.preference.mapper;

import com.a0000.digicompass.modules.preference.dto.UserPreferenceItem;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PreferenceMapper {

    private final JdbcTemplate jdbc;

    public PreferenceMapper(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public UserPreferenceItem findByUserId(Long userId) {
        List<UserPreferenceItem> results = jdbc.query(
                "SELECT id, min_budget, max_budget, category_id, brand_ids, usage_scenes, priority_tags, avoid_tags, remark FROM dc_user_preference WHERE user_id = ?",
                new Object[]{userId},
                (rs, rowNum) -> new UserPreferenceItem(
                        rs.getLong("id"),
                        rs.getBigDecimal("min_budget"),
                        rs.getBigDecimal("max_budget"),
                        rs.getObject("category_id") != null ? rs.getLong("category_id") : null,
                        rs.getString("brand_ids"),
                        rs.getString("usage_scenes"),
                        rs.getString("priority_tags"),
                        rs.getString("avoid_tags"),
                        rs.getString("remark")
                )
        );
        return results.isEmpty() ? null : results.getFirst();
    }

    public void insert(Long userId, UserPreferenceItem item) {
        jdbc.update("""
                INSERT INTO dc_user_preference (user_id, min_budget, max_budget, category_id, brand_ids, usage_scenes, priority_tags, avoid_tags, remark)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, userId,
                item.minBudget(), item.maxBudget(), item.categoryId(),
                item.brandIds(), item.usageScenes(), item.priorityTags(),
                item.avoidTags(), item.remark());
    }

    public void update(Long userId, UserPreferenceItem item) {
        jdbc.update("""
                UPDATE dc_user_preference
                SET min_budget=?, max_budget=?, category_id=?, brand_ids=?, usage_scenes=?, priority_tags=?, avoid_tags=?, remark=?
                WHERE user_id=?
                """,
                item.minBudget(), item.maxBudget(), item.categoryId(),
                item.brandIds(), item.usageScenes(), item.priorityTags(),
                item.avoidTags(), item.remark(), userId);
    }
}
