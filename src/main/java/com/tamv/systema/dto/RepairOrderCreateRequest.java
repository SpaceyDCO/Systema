package com.tamv.systema.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class RepairOrderCreateRequest {
    private Long customerId;
    private String equipmentName;
    private String serialNumber;
    private String reportedIssue;
}
