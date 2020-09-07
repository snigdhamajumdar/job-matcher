package com.demo.service;

import com.demo.model.*;
import com.demo.util.GeoUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.mockito.internal.util.reflection.FieldSetter;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class WorkerLookupServiceTest {

    private static final String WORKER_ID_WITH_MATCHING_JOBS = "8";
    private static final String WORKER_ID_WITH_NO_MATCHING_JOBS = "0";
    @Mock
    private RestTemplate restTemplate;

    @Mock
    private JobLookupService jobLookupService;

    @InjectMocks
    private WorkerLookupService underTest;

    @BeforeEach
    void setUp() throws NoSuchFieldException {
        MockitoAnnotations.initMocks(this);
        FieldSetter.setField(underTest, underTest.getClass().getDeclaredField("workerList"), new ArrayList<Worker>());
        ResponseEntity<Worker[]> workers = createTestWorker();
        Mockito.when(
                restTemplate.exchange(
                ArgumentMatchers.eq("http://test.swipejobs.com/api/workers"),
                ArgumentMatchers.eq(HttpMethod.GET),
                ArgumentMatchers.<HttpEntity<String>>any(),
                ArgumentMatchers.<Class<Worker[]>> any()))
                .thenReturn(workers);
    }

    @Test
    public void givenMatchingJobsExist__whenMatchingJobsAPICalled_thenReturnRelevantJobs() throws Exception {
        List<Job> jobList = createMatchingTestJobs();
        Mockito.when(jobLookupService.getJobList()).thenReturn(jobList);
        Worker worker = ReflectionTestUtils.invokeMethod(underTest, "getWorkerByID",WORKER_ID_WITH_MATCHING_JOBS);
        List<Job> actualResult = underTest.getMatchingJobsForWorker(WORKER_ID_WITH_MATCHING_JOBS);
        Assertions.assertNotNull(actualResult);
        Assertions.assertTrue(worker.getSkills().containsAll(actualResult.stream().map(job -> job.getJobTitle()).collect(Collectors.toList())));
        Assertions.assertTrue(worker.getCertificates()
        .containsAll(actualResult
                .stream()
                .map(job -> job.getRequiredCertificates())
                .flatMap(Collection::stream)
                .collect(Collectors.toList())));
        Assertions.assertTrue(actualResult.stream().map(job -> job.isDriverLicenseRequired()).collect(Collectors.toList()).contains(true));
        Assertions.assertTrue(actualResult.stream().map(job -> job.isDriverLicenseRequired()).collect(Collectors.toList()).contains(false));
        actualResult.forEach(job -> {
            double actualDistance = GeoUtil.distance(job.getLocation().getLatitude(),
                    job.getLocation().getLongitude(),
                    worker.getJobSearchAddress().getLatitude(),
                    worker.getJobSearchAddress().getLongitude(),
                    worker.getJobSearchAddress().getUnit());
            Assertions.assertTrue(worker.getJobSearchAddress().getMaxJobDistance() >= actualDistance );

            DayOfWeek actualDayOfWeek = LocalDateTime.parse(job.getStartDate(), DateTimeFormatter.ISO_DATE_TIME).getDayOfWeek();
            Assertions.assertTrue(worker
                    .getAvailability()
                    .stream()
                    .map(workerAvailability -> workerAvailability.getTitle().toLowerCase())
                    .collect(Collectors.toList())
                    .contains(actualDayOfWeek.name().toLowerCase()));
        });
    }

    @Test
    public void givenMatchingJobsExist__whenMatchingJobsAPICalled_thenAtMostThreeJobsReturned() throws Exception {
        List<Job> jobList = createMatchingTestJobs();
        Mockito.when(jobLookupService.getJobList()).thenReturn(jobList);
        List<Job> actualResult = underTest.getMatchingJobsForWorker(WORKER_ID_WITH_MATCHING_JOBS);
        Assertions.assertEquals(3, actualResult.size());
    }

    @Test
    public void givenNoMatchingJobsExist__whenMatchingJobsAPICalled_thenNoJobsReturned() throws Exception {
        List<Job> jobList = createMatchingTestJobs();
        Mockito.when(jobLookupService.getJobList()).thenReturn(jobList);
        List<Job> actualResult = underTest.getMatchingJobsForWorker(WORKER_ID_WITH_NO_MATCHING_JOBS);
        Assertions.assertEquals(0, actualResult.size());
    }

    @Test
    public void givenInvalidWorkerId_whenMatchingJobsAPICalled_throwException() {
        Exception exception = Assertions.assertThrows(Exception.class, () -> {
            underTest.getMatchingJobsForWorker("BLAH");
        });
        Assertions.assertEquals("Worker Not Found", exception.getMessage());
    }

    private List<Job> createMatchingTestJobs() {
        Job maximind = Job.builder()
                .driverLicenseRequired(false)
                .requiredCertificates(Arrays.asList(
                        "Outstanding Memory Award",
                        "Calm in the Eye of the Storm"))
                .location(Location.builder().longitude(14.453499).latitude(49.739001).build())
                .billRate("$17.60")
                .startDate("2015-11-03T09:45:47.56Z")
                .jobTitle("Chief Troublemaker")
                .company("Maximind")
                .guid("562f66aa12b3d30a71d3cd89")
                .jobId("19")
                .build();
        Job syntac = Job.builder()
                .driverLicenseRequired(true)
                .requiredCertificates(Arrays.asList(
                        "Healthy Living Promoter"))
                .location(Location.builder().longitude(14.580436).latitude(49.886497).build())
                .billRate("$15.83")
                .startDate("2015-11-15T11:23:34.31Z")
                .jobTitle("Chief Cheerleader")
                .company("Syntac")
                .guid("562f66aa1ceec2fb3e8bb3a0")
                .jobId("14")
                .build();
        Job centice = Job.builder()
                .driverLicenseRequired(false)
                .requiredCertificates(Arrays.asList(
                        "Outstanding Memory Award"))
                .location(Location.builder().longitude(14.082219).latitude(50.180255).build())
                .billRate("$14.98")
                .startDate("2015-11-12T07:23:56.19Z")
                .jobTitle("The Resinator")
                .company("Centice")
                .guid("562f66aa1ceec2fb3e8bb3a0")
                .jobId("29")
                .build();
        Job nimon = Job.builder()
                .driverLicenseRequired(false)
                .requiredCertificates(Arrays.asList(
                        "The Encouraging Word Award"))
                .location(Location.builder().longitude(14.987061).latitude(50.212725).build())
                .billRate("$14.79")
                .startDate("2015-11-14T10:07:21.887Z")
                .jobTitle("Director of First Impressions")
                .company("Nimon")
                .guid("562f66aaf3cb186fc0776de9")
                .jobId("27")
                .build();
        Job pholio = Job.builder()
                .driverLicenseRequired(true)
                .requiredCertificates(Arrays.asList(
                        "Outstanding Memory Award",
                        "Calm in the Eye of the Storm",
                        "Marvelous Multitasker"))
                .location(Location.builder().longitude(14.312687).latitude(49.828395).build())
                .billRate("$7.47")
                .startDate("2015-11-24T07:35:25.451Z")
                .jobTitle("Chief Cheerleader")
                .company("Pholio")
                .guid("562f66aa89c9c662fa538fb7")
                .jobId("24")
                .build();
        Job lovepad = Job.builder()
                .driverLicenseRequired(true)
                .requiredCertificates(Arrays.asList(
                        "Office Lunch Expert"))
                .location(Location.builder().longitude(14.293204).latitude(50.266116).build())
                .billRate("$6.21")
                .startDate("2015-11-02T22:12:40.263Z")
                .jobTitle("The Resinator")
                .company("Lovepad")
                .guid("562f66aa7383f3a5241674c8")
                .jobId("11")
                .build();
        return Arrays.asList(maximind, syntac, centice, nimon, pholio, lovepad);
    }

    private ResponseEntity<Worker[]> createTestWorker() {
        Worker dianaMooney = Worker.builder()
                .rating(3)
                .isActive(true)
                .certificates(Arrays.asList("The Human Handbook",
                        "Office Lunch Expert",
                        "Outstanding Memory Award",
                        "Outside the Box Thinker",
                        "The Encouraging Word Award",
                        "Calm in the Eye of the Storm",
                        "Healthy Living Promoter",
                        "Marvelous Multitasker",
                        "The Risk Taker"))
                .skills(Arrays.asList(
                        "The Resinator",
                        "Head of global trends and futuring",
                        "Chief Cheerleader",
                        "Creator of opportunities",
                        "Director of First Impressions",
                        "Chief Troublemaker"))
                .jobSearchAddress(JobSearchAddress.builder()
                        .unit("km")
                        .maxJobDistance(50)
                        .longitude(14.592614)
                        .latitude(50.141097)
                        .build())
                .transportation("PUBLIC TRANSPORT")
                .hasDriversLicense(true)
                .availability(Arrays.asList(WorkerAvailability.builder().title("Friday").dayIndex(5).build(),
                        WorkerAvailability.builder().title("Sunday").dayIndex(7).build(),
                        WorkerAvailability.builder().title("Thursday").dayIndex(4).build(),
                        WorkerAvailability.builder().title("Tuesday").dayIndex(2).build(),
                        WorkerAvailability.builder().title("Monday").dayIndex(1).build(),
                        WorkerAvailability.builder().title("Saturday").dayIndex(6).build()))
                .phone("+1 (921) 419-2523")
                .email("diana.mooney@navir.name")
                .name(WorkerName.builder().first("Diana").last("Mooney").build())
                .age(34)
                .guid("562f66478b2c02d14302fda4")
                .userId(WORKER_ID_WITH_MATCHING_JOBS)
                .build();

        Worker andrewsFowler = Worker.builder()
                .rating(2)
                .isActive(true)
                .certificates(Arrays.asList(
                        "Outstanding Innovator",
                        "The Behind the Scenes Wonder",
                        "The Risk Taker",
                        "Outside the Box Thinker",
                        "Marvelous Multitasker",
                        "The Asker of Good Questions",
                        "Outstanding Memory Award",
                        "Office Lunch Expert",
                        "Excellence in Organization"))
                .skills(Arrays.asList(
                        "Creator of opportunities",
                        "Arts and Crafts Designer"))
                .jobSearchAddress(JobSearchAddress.builder()
                        .unit("km")
                        .maxJobDistance(30)
                        .longitude(13.971284)
                        .latitude(49.782281)
                        .build())
                .transportation("CAR")
                .hasDriversLicense(false)
                .availability(Arrays.asList(
                        WorkerAvailability.builder().title("Friday").dayIndex(5).build(),
                        WorkerAvailability.builder().title("Sunday").dayIndex(7).build(),
                        WorkerAvailability.builder().title("Thursday").dayIndex(4).build(),
                        WorkerAvailability.builder().title("Tuesday").dayIndex(2).build(),
                        WorkerAvailability.builder().title("Monday").dayIndex(1).build(),
                        WorkerAvailability.builder().title("Wednesday").dayIndex(3).build()))
                .phone("+1 (847) 420-3272")
                .email("fowler.andrews@comcubine.io")
                .name(WorkerName.builder().first("Andrews").last("Fowler").build())
                .age(30)
                .guid("562f6647410ecd6bf49146e9")
                .userId(WORKER_ID_WITH_NO_MATCHING_JOBS)
                .build();
        Worker[] workers = {dianaMooney, andrewsFowler};
        return ResponseEntity.ok(workers);
    }

}
