package com.a0000.digicompass.modules.dashboard.controller;

import com.a0000.digicompass.common.api.ApiResponse;
import com.a0000.digicompass.modules.dashboard.dto.DashboardOverview;
import com.a0000.digicompass.modules.dashboard.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/overview")
    public ApiResponse<DashboardOverview> overview() {
        return ApiResponse.success(dashboardService.getOverview());
    }
}
