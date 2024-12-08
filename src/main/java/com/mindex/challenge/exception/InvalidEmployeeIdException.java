package com.mindex.challenge.exception;

public class InvalidEmployeeIdException extends RuntimeException {
	
	public InvalidEmployeeIdException(String id) {
		super("Invalid employeeId: " + id);
	}
}
