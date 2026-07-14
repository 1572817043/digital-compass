package com.a0000.digicompass.modules.dashboard.mapper;

import com.a0000.digicompass.modules.dashboard.dto.DashboardOverview;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class DashboardMapper {

    private final JdbcTemplate jdbc;

    public DashboardMapper(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public DashboardOverview getOverview() {
        int productCount = jdbc.queryForObject("SELECT COUNT(*) FROM dc_product", Integer.class);
        int userCount = jdbc.queryForObject("SELECT COUNT(*) FROM dc_user", Integer.class);
        int favoriteCount = jdbc.queryForObject("SELECT COUNT(*) FROM dc_user_favorite", Integer.class);
        int historyCount = jdbc.queryForObject("SELECT COUNT(*) FROM dc_user_view_history", Integer.class);
        int priceAlertCount = jdbc.queryForObject("SELECT COUNT(*) FROM dc_price_alert", Integer.class);
        int assistantConversationCount = jdbc.queryForObject("SELECT COUNT(*) FROM dc_assistant_conversation", Integer.class);
        int aiWorkflowLogCount = jdbc.queryForObject("SELECT COUNT(*) FROM dc_ai_workflow_log", Integer.class);

        List<DashboardOverview.RecentProduct> recentProducts = jdbc.query(
                "SELECT p.id, p.name, b.name AS brand_name, c.name AS category_name, p.official_price, DATE_FORMAT(p.created_at, '%Y-%m-%d %H:%i') AS created_at FROM dc_product p LEFT JOIN dc_brand b ON p.brand_id = b.id JOIN dc_category c ON p.category_id = c.id ORDER BY p.id DESC LIMIT 5",
                (rs, rn) -> new DashboardOverview.RecentProduct(
                        rs.getLong("id"), rs.getString("name"), rs.getString("brand_name"),
                        rs.getString("category_name"), rs.getBigDecimal("official_price"), rs.getString("created_at")
                ));

        List<DashboardOverview.RecentWorkflowLog> recentLogs = jdbc.query(
                "SELECT l.id, u.username, l.user_requirement, l.fallback_used, DATE_FORMAT(l.created_at, '%Y-%m-%d %H:%i') AS created_at FROM dc_ai_workflow_log l LEFT JOIN dc_user u ON l.user_id = u.id ORDER BY l.id DESC LIMIT 5",
                (rs, rn) -> new DashboardOverview.RecentWorkflowLog(
                        rs.getLong("id"), rs.getString("username"), rs.getString("user_requirement"),
                        rs.getInt("fallback_used") == 1, rs.getString("created_at")
                ));

        return new DashboardOverview(productCount, userCount, favoriteCount, historyCount,
                priceAlertCount, assistantConversationCount, aiWorkflowLogCount,
                recentProducts, recentLogs);
    }
}
