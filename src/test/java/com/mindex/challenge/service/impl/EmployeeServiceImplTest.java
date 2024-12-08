package com.mindex.challenge.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.EmployeeService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Calendar;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EmployeeServiceImplTest {
	
	public static final String BASE_URL = "http://localhost";

    private String employeeUrl;
    private String employeeIdUrl;
    private String reportingStructureUrl;
    private String compensationUrl;
    private String compensationUpdateUrl;

    @Autowired
    private EmployeeService employeeService;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void setup() {
        employeeUrl = BASE_URL + ":" + port + "/employee";
        employeeIdUrl = BASE_URL + ":" + port + "/employee/{id}";
        reportingStructureUrl = BASE_URL + ":" + port + "/employee/{id}/reportingStructure";
        compensationUrl = BASE_URL + ":" + port + "/employee/{id}/compensation";
        compensationUpdateUrl = BASE_URL + ":" + port + "/employee/{id}/compensation";
    }

    @Test
    public void testCreateReadUpdate() {
        Employee testEmployee = new Employee();
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setDepartment("Engineering");
        testEmployee.setPosition("Developer");

        // Create checks
        Employee createdEmployee = restTemplate.postForEntity(employeeUrl, testEmployee, Employee.class).getBody();

        assertNotNull(createdEmployee.getEmployeeId());
        assertEmployeeEquivalence(testEmployee, createdEmployee);


        // Read checks
        Employee readEmployee = restTemplate.getForEntity(employeeIdUrl, Employee.class, createdEmployee.getEmployeeId()).getBody();
        assertEquals(createdEmployee.getEmployeeId(), readEmployee.getEmployeeId());
        assertEmployeeEquivalence(createdEmployee, readEmployee);


        // Update checks
        readEmployee.setPosition("Development Manager");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Employee updatedEmployee =
                restTemplate.exchange(employeeIdUrl,
                        HttpMethod.PUT,
                        new HttpEntity<Employee>(readEmployee, headers),
                        Employee.class,
                        readEmployee.getEmployeeId()).getBody();

        assertEmployeeEquivalence(readEmployee, updatedEmployee);


        // Read checks
        // (To make sure our update didn't duplicate the record)
        Employee readEmployeeAfterUp = restTemplate.getForEntity(employeeIdUrl, Employee.class, createdEmployee.getEmployeeId()).getBody();
        assertEquals(updatedEmployee.getEmployeeId(), readEmployeeAfterUp.getEmployeeId());
        assertEmployeeEquivalence(updatedEmployee, readEmployeeAfterUp);
    }

    private static void assertEmployeeEquivalence(Employee expected, Employee actual) {
        assertEquals(expected.getFirstName(), actual.getFirstName());
        assertEquals(expected.getLastName(), actual.getLastName());
        assertEquals(expected.getDepartment(), actual.getDepartment());
        assertEquals(expected.getPosition(), actual.getPosition());
    }
    
    @Test
    public void testReportingStructure() {
        // Read reporting structure
    	//   Employee 1
    	{
        	String employeeId = "16a596ae-edd3-4847-99fe-c4518e82c86f";
        	int expectedNumberOfReports = 4;
        	
        	ReportingStructure reportingStructure = restTemplate.getForEntity(reportingStructureUrl, ReportingStructure.class, employeeId).getBody();
            assertEquals(expectedNumberOfReports, reportingStructure.getNumberOfReports());
    	}
    	//   Employee 2
    	{
        	String employeeId = "03aa1462-ffa9-4978-901b-7c001562cf6f";
        	int expectedNumberOfReports = 2;
        	
        	ReportingStructure reportingStructure = restTemplate.getForEntity(reportingStructureUrl, ReportingStructure.class, employeeId).getBody();
            assertEquals(expectedNumberOfReports, reportingStructure.getNumberOfReports());
    	}
    }
    
    @Test
    public void testCompensation() {
        // Read compensation
    	{
        	String employeeId = "16a596ae-edd3-4847-99fe-c4518e82c86f";
        	int expectedSalary = 120000;
        	
        	Compensation compensation = restTemplate.getForEntity(compensationUrl, Compensation.class, employeeId).getBody();
            assertEquals(expectedSalary, compensation.getSalary());
    	}
    }
    
    @Test
    public void testCompensationUpdate() throws Exception {
        // Read compensation
    	{
        	String employeeId = "16a596ae-edd3-4847-99fe-c4518e82c86f";
        	
        	Compensation newCompensation = new Compensation();
        	newCompensation.setSalary(200000);
        	
        	Calendar effectiveDate = Calendar.getInstance();
        	effectiveDate.set(2024, 1, 1);
        	newCompensation.setEffectiveDate(effectiveDate.getTime());
        	
        	Compensation compensation = restTemplate.postForEntity(compensationUpdateUrl, newCompensation, Compensation.class, employeeId).getBody();
            
            var mapper = new ObjectMapper();
            assertEquals(mapper.writeValueAsString(newCompensation), mapper.writeValueAsString(compensation));
    	}
    }
}
