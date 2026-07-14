package com.a0000.digicompass.modules.preference.service.impl;

import com.a0000.digicompass.modules.preference.dto.UserPreferenceItem;
import com.a0000.digicompass.modules.preference.dto.UserPreferenceSaveRequest;
import com.a0000.digicompass.modules.preference.mapper.PreferenceMapper;
import com.a0000.digicompass.modules.preference.service.PreferenceService;
import org.springframework.stereotype.Service;

@Service
public class PreferenceServiceImpl implements PreferenceService {

    private final PreferenceMapper preferenceMapper;

    public PreferenceServiceImpl(PreferenceMapper preferenceMapper) {
        this.preferenceMapper = preferenceMapper;
    }

    @Override
    public UserPreferenceItem getPreference(Long userId) {
        UserPreferenceItem item = preferenceMapper.findByUserId(userId);
        if (item == null) {
            return new UserPreferenceItem(null, null, null, null, null, null, null, null, null);
        }
        return item;
    }

    @Override
    public void savePreference(Long userId, UserPreferenceSaveRequest request) {
        validateBudgetRange(request);
        UserPreferenceItem item = new UserPreferenceItem(
                null,
                request.minBudget(),
                request.maxBudget(),
                request.categoryId(),
                request.brandIds(),
                request.usageScenes(),
                request.priorityTags(),
                request.avoidTags(),
                request.remark()
        );
        UserPreferenceItem existing = preferenceMapper.findByUserId(userId);
        if (existing == null) {
            preferenceMapper.insert(userId, item);
        } else {
            preferenceMapper.update(userId, item);
        }
    }

    private void validateBudgetRange(UserPreferenceSaveRequest request) {
        if (request.minBudget() != null && request.maxBudget() != null
                && request.maxBudget().compareTo(request.minBudget()) < 0) {
            throw new IllegalArgumentException("最高预算不能低于最低预算");
        }
    }
}
