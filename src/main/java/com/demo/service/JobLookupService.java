package com.demo.service;

import com.demo.model.Job;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Data
public class JobLookupService {

    private RestTemplate restTemplate;

    private List<Job> jobList;

    @Autowired
    public JobLookupService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    public void post() {
        jobList = new ArrayList<>();
        jobList.addAll(pullJobListFromAPI());
    }

    private List<Job> pullJobListFromAPI() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<String>(headers);

        ResponseEntity<Job[]> responseEntity = restTemplate.exchange("http://test.swipejobs.com/api/jobs", HttpMethod.GET, entity, Job[].class);
        return Arrays.asList(responseEntity.getBody());
    }
}
