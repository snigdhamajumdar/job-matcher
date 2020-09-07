package com.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Job {

    private boolean driverLicenseRequired;
    private List<String> requiredCertificates;
    private Location location;
    private String billRate;
    private Integer workersRequired;
    private String startDate;
    private String about;
    private String jobTitle;
    private String company;
    private String guid;
    private String jobId;

    @JsonIgnore
    public Double getNumericBillingRate() {
        return Double.valueOf(this.getBillRate().substring(1));
    }
}
