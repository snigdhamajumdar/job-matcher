package com.demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JobSearchAddress {
    private String unit;
    private Integer maxJobDistance;
    private Double longitude;
    private Double latitude;
}
