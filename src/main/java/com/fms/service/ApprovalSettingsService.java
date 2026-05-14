package com.fms.service;

import com.fms.entity.AppSetting;
import com.fms.repository.AppSettingRepository;
import com.fms.entity.Role;
import com.fms.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Persists which Spring Security role names may approve/reject external
 * (partner / supplier) work-order estimates. Stored as a comma-separated
 * list in {@code app_settings} under {@value #KEY_ESTIMATE_APPROVER_ROLES}.
 */
@Service
@RequiredArgsConstructor
public class ApprovalSettingsService {

    public static final String KEY_ESTIMATE_APPROVER_ROLES = "ESTIMATE_APPROVER_ROLES";
    private static final String DEFAULT_ROLES = "ROLE_ADMIN,ROLE_MANAGER,ROLE_FINANCE";

    private final AppSettingRepository appSettingRepository;

    @Transactional(readOnly = true)
    public List<String> getEstimateApproverRoleNames() {
        return appSettingRepository.findById(KEY_ESTIMATE_APPROVER_ROLES)
                .map(AppSetting::getValue)
                .filter(StringUtils::hasText)
                .map(v -> Arrays.stream(v.split(","))
                        .map(String::trim)
                        .filter(StringUtils::hasText)
                        .collect(Collectors.toList()))
                .filter(list -> !list.isEmpty())
                .orElse(Arrays.stream(DEFAULT_ROLES.split(","))
                        .map(String::trim)
                        .collect(Collectors.toList()));
    }

    @Transactional(readOnly = true)
    public String getEstimateApproverRolesRaw() {
        return appSettingRepository.findById(KEY_ESTIMATE_APPROVER_ROLES)
                .map(AppSetting::getValue)
                .filter(StringUtils::hasText)
                .orElse(DEFAULT_ROLES);
    }

    @Transactional
    public void saveEstimateApproverRolesRaw(String csv) {
        AppSetting s = appSettingRepository.findById(KEY_ESTIMATE_APPROVER_ROLES)
                .orElseGet(() -> AppSetting.builder().key(KEY_ESTIMATE_APPROVER_ROLES).build());
        s.setValue(StringUtils.hasText(csv) ? csv.trim() : DEFAULT_ROLES);
        appSettingRepository.save(s);
    }

    /** True when the user has at least one role listed in the current approver configuration. */
    @Transactional(readOnly = true)
    public boolean userMayApproveExternalEstimates(User user) {
        if (user == null || user.getRoles() == null) {
            return false;
        }
        List<String> allowed = getEstimateApproverRoleNames();
        return user.getRoles().stream()
                .map(Role::getName)
                .anyMatch(allowed::contains);
    }
}
