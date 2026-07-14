package com.a0000.digicompass.modules.preference.service;

import com.a0000.digicompass.modules.preference.dto.UserPreferenceItem;
import com.a0000.digicompass.modules.preference.dto.UserPreferenceSaveRequest;

public interface PreferenceService {

    UserPreferenceItem getPreference(Long userId);

    void savePreference(Long userId, UserPreferenceSaveRequest request);
}
