package com.demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Worker {

    private Integer rating;
    private Boolean isActive;
    private List<String> certificates;
    private List<String> skills;
    private JobSearchAddress jobSearchAddress;
    private String transportation;
    private Boolean hasDriversLicense;
    private List<WorkerAvailability> availability;
    private String phone;
    private String email;
    private WorkerName name;
    private Integer age;
    private String guid;
    private String userId;

}
