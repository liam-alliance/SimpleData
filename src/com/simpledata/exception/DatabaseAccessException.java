package com.simpledata.exception;

public class DatabaseAccessException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public DatabaseAccessException() {
		super("Cannot Create Row In Database Table");
	}
	
}
