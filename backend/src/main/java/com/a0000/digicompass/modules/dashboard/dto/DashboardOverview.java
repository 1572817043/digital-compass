package com.a0000.digicompass.modules.dashboard.dto;

import java.util.List;

public record DashboardOverview(
        int productCount,
        int userCount,
        int favoriteCount,
        int historyCount,
        int priceAlertCount,
        int assistantConversationCount,
        int aiWorkflowLogCount,
        List<RecentProduct> recentProducts,
        List<RecentWorkflowLog> recentWorkflowLogs
) {
    public record RecentProduct(Long id, String name, String brandName, String categoryName, java.math.BigDecimal officialPrice, String createdAt) {}
    public record RecentWorkflowLog(Long id, String username, String userRequirement, boolean fallbackUsed, String createdAt) {}
}
