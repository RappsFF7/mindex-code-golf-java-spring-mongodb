package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.exception.InvalidEmployeeIdException;
import com.mindex.challenge.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class EmployeeServiceImpl implements EmployeeService {

	private static final Logger LOG = LoggerFactory.getLogger(EmployeeServiceImpl.class);

	@Autowired
	private EmployeeRepository employeeRepository;

	@Override
	public Employee create(Employee employee) {
		LOG.debug("Creating employee [{}]", employee);

		employee.setEmployeeId(UUID.randomUUID().toString());
		employeeRepository.insert(employee);

		return employee;
	}

	@Override
	public Employee read(String id) {
		LOG.debug("Reading employee with id [{}]", id);

		Employee employee = employeeRepository.findByEmployeeId(id);

		if (employee == null) {
			throw new InvalidEmployeeIdException(id);
		}

		return employee;
	}

	@Override
	public Employee update(Employee employee) {
		LOG.debug("Updating employee [{}]", employee);

		return employeeRepository.save(employee);
	}

	@Override
	public ReportingStructure reportingStructure(String employeeId) {
		LOG.debug("Generating employeeStructure for employee id [{}]", employeeId);

		ReportingStructure reportingStructure = employeeRepository.getReportingStructureByEmployeeId(employeeId);

		if (reportingStructure == null) {
			throw new InvalidEmployeeIdException(employeeId);
		}

		return reportingStructure;
	}

	@Override
	public Compensation compensation(String employeeId) {
		LOG.debug("Reading compensation for employee id [{}]", employeeId);

		Employee employee = employeeRepository.findByEmployeeId(employeeId);

		if (employee == null) {
			throw new InvalidEmployeeIdException(employeeId);
		}

		return employee.getCompensation();
	}

	@Override
	public Compensation compensationUpdate(String employeeId, Compensation compensation) {
		LOG.debug("Updating compensation for employee id [{}]", employeeId);

		Employee employee = employeeRepository.findByEmployeeId(employeeId);

		if (employee == null) {
			throw new InvalidEmployeeIdException(employeeId);
		}

		employee.setCompensation(compensation);
		Employee updatedEmployee = employeeRepository.save(employee);

		return updatedEmployee.getCompensation();
	}
}
