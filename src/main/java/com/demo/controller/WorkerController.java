package com.demo.controller;

import com.demo.model.Job;
import com.demo.service.WorkerLookupService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Api("Worker API")
@RequestMapping(value = "matcher-api/worker")
public class WorkerController {

    private WorkerLookupService workerLookupService;

    @Autowired
    public WorkerController(WorkerLookupService workerLookupService) {
        this.workerLookupService = workerLookupService;
    }

    @GetMapping(value = "/{id}/jobs")
    @ApiOperation(value = "Returns matching jobs for worker",
            notes =
            "     * Method that accepts a worker ID and returns top 3 highest paying job matches.\n" +
            "     * Jobs matching is based on following conditions:\n" +
            "           a. Worker's skill set should match job requirement\n" +
            "           b. Worker must have all the required certificates that the job demands.\n" +
            "           c. If the job has a requirement that applicant must possess drivers license, then worker must possess drivers license. Otherwise, it does not matter if worker has one or not.\n" +
            "           d. The job location must be within the worker's preferred max job distance.\n" +
            "           f. The job starting date must fall on a day that the worked is available based on his/her preference.",
            response = ResponseEntity.class)
    public ResponseEntity getJobsMatherForWorkers(@PathVariable @ApiParam("worker id") String id) {
        try{
            List<Job> jobList = workerLookupService.getMatchingJobsForWorker(id);
            return ResponseEntity.ok(jobList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
