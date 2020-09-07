package com.demo.service;

import com.demo.model.Job;
import com.demo.model.JobSearchAddress;
import com.demo.model.Location;
import com.demo.model.Worker;
import com.google.common.base.Preconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.demo.util.GeoUtil.distance;
import static com.demo.util.StreamUtil.chainPredicatesByAnd;

@Service
public class WorkerLookupService {

    private RestTemplate restTemplate;

    private JobLookupService jobLookupService;

    private List<Worker> workerList;

    @Autowired
    public WorkerLookupService(RestTemplate restTemplate, JobLookupService jobLookupService) {
        this.restTemplate = restTemplate;
        this.jobLookupService = jobLookupService;
    }

    @PostConstruct
    public void post() {
        workerList = new ArrayList<>();
        workerList.addAll(pullWorkerListFromAPI());
    }

    /**
     * Method that accepts a worker ID and returns top 3 highest paying job matches.
     * Jobs matching is based on following conditions:
     *  a. Worker's skill set should match job requirement
     *  b. Worker must have all the required certificates that the job demands.
     *  c. If the job has a requirement that applicant must possess drivers license, then worker must possess drivers license.
     *     Otherwise, it does not matter if worker has one or not.
     *  d. The job location must be within the worker's preferred max job distance.
     *  f. The job starting date must fall on a day that the worked is available based on his/her preference.
     * @param workerId
     * @return List of {@link Job} that match all the conditions defined above
     * @throws Exception when worker's ID is not found or empty
     */
    public List<Job> getMatchingJobsForWorker(String workerId) throws Exception {
        Worker worker = getWorkerByID(workerId);
        return jobLookupService.getJobList()
                .stream()
                .filter(chainPredicatesByAnd(filterByMatchingSkillSet(worker),
                        filterByRequiredCertificates(worker),
                        filterByDriverLicenseRequirement(worker),
                        filterByDistance(worker),
                        filterByAvailabilityDay(worker)))
                .sorted((j2, j1) -> Double.compare(j1.getNumericBillingRate(), j2.getNumericBillingRate()))
                .limit(3)
                .collect(Collectors.toList());
    }

    /**
     * Returns a worker with the same user id as provided by input param
     * @param id
     * @return
     * @throws Exception
     */
    private Worker getWorkerByID(String id) throws Exception {
        Preconditions.checkArgument(!StringUtils.isEmpty(id), "ID cannot be empty");
        if(CollectionUtils.isEmpty(workerList)) {
            workerList.addAll(pullWorkerListFromAPI());
        }
        return workerList
                .stream()
                .filter(worker -> worker.getUserId().equalsIgnoreCase(id))
                .findFirst()
                .orElseThrow( () -> new Exception("Worker Not Found"));
    }

    /**
     * Method to populate workers from provided API
     * @return List of {@link Worker}
     */
    private List<Worker> pullWorkerListFromAPI() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity(headers);

        ResponseEntity<Worker[]> responseEntity = restTemplate.exchange("http://test.swipejobs.com/api/workers", HttpMethod.GET, entity, Worker[].class);
        return Arrays.asList(responseEntity.getBody());
    }

    /**
     * Method to create predicate that filters jobs whose start date matches worker's availability
     * @param worker
     * @return {@link Predicate}
     */
    private Predicate<Job> filterByAvailabilityDay(Worker worker) {
        return job -> worker.getAvailability()
                .stream()
                .filter(Objects::nonNull)
                .map(workerAvailability -> DayOfWeek.of(workerAvailability.getDayIndex()))
                .collect(Collectors.toList())
                .contains(LocalDateTime.parse(job.getStartDate(), DateTimeFormatter.ISO_DATE_TIME).getDayOfWeek());
    }

    /**
     * Method to create predicate that filters jobs within the distance set by worker
     * @param worker
     * @return {@link Predicate}
     */
    private Predicate<Job> filterByDistance(Worker worker) {
        return job -> isJobWithinWorkerLocationPreference(job.getLocation(), worker.getJobSearchAddress());
    }

    /**
     * Method to create predicate that checks whether worker has drivers license if job requirement is such.
     * @param worker
     * @return {@link Predicate}
     */
    private Predicate<Job> filterByDriverLicenseRequirement(Worker worker) {
        return job -> job.isDriverLicenseRequired() ? worker.getHasDriversLicense() : true;
    }

    /**
     * Method to create predicate that filters jobs where worker's certificates matches with required certificates of job
     * @param worker
     * @return {@link Predicate}
     */
    private Predicate<Job> filterByRequiredCertificates(Worker worker) {
        return job -> worker.getCertificates().containsAll(job.getRequiredCertificates());
    }

    /**
     * Method to create predicate that filters jobs with the job title matching worker's skill set
     * @param worker
     * @return {@link Predicate}
     */
    private Predicate<Job> filterByMatchingSkillSet(Worker worker) {
        return job -> worker.getSkills()
                .stream()
                .anyMatch(skill -> skill.equalsIgnoreCase(job.getJobTitle()));
    }

    /**
     * If distance between job location and worker is less than or equal to the worker's preference return true, else return false.
     * @param jobLocation
     * @param workerJobLocationPreference
     * @return boolean
     */
    private boolean isJobWithinWorkerLocationPreference(Location jobLocation, JobSearchAddress workerJobLocationPreference) {
        return distance(jobLocation.getLatitude(), jobLocation.getLongitude(), workerJobLocationPreference.getLatitude(), workerJobLocationPreference.getLongitude(), workerJobLocationPreference.getUnit()) <= workerJobLocationPreference.getMaxJobDistance() ? true : false;
    }
}
