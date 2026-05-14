package com.fms.dto.workorder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WorkOrderCreateRequest {

    @NotNull
    private Long vehicleId;

    private Long workshopId;

    @NotBlank
    private String serviceType;

    @NotBlank
    private String executionType;

    @NotBlank
    private String requestDescription;

    private Integer currentOdometer;

    private Boolean isUrgent = false;

    private Boolean isWarranty = false;

    private String warrantyReference;
}
