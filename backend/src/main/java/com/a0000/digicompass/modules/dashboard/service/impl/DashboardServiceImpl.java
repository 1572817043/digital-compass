package com.a0000.digicompass.modules.dashboard.service.impl;

import com.a0000.digicompass.modules.dashboard.dto.DashboardOverview;
import com.a0000.digicompass.modules.dashboard.mapper.DashboardMapper;
import com.a0000.digicompass.modules.dashboard.service.DashboardService;
import org.springframework.stereotype.Service;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final DashboardMapper dashboardMapper;

    public DashboardServiceImpl(DashboardMapper dashboardMapper) {
        this.dashboardMapper = dashboardMapper;
    }

    @Override
    public DashboardOverview getOverview() {
        return dashboardMapper.getOverview();
    }
}
