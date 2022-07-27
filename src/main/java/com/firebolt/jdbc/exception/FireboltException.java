package com.firebolt.jdbc.exception;

import static com.firebolt.jdbc.exception.ExceptionType.*;
import static org.apache.hc.core5.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.hc.core5.http.HttpStatus.SC_UNAUTHORIZED;

import java.sql.SQLException;

import lombok.Getter;

public class FireboltException extends SQLException {

	@Getter
	private final ExceptionType type;

	public FireboltException(ExceptionType type) {
		super();
		this.type = type;
	}

	public FireboltException(String message) {
		super(message);
		type = ExceptionType.ERROR;
	}

	public FireboltException(String message, Integer httpStatusCode) {
		super(message);
		type = getExceptionType(httpStatusCode);
	}

	public FireboltException(String message, Throwable cause) {
		this(message, cause, ExceptionType.ERROR);
	}

	public FireboltException(String message, ExceptionType type) {
		super(message);
		this.type = type;
	}

	public FireboltException(String message, Integer httpStatusCode, Throwable cause) {
		super(message, cause);
		this.type = getExceptionType(httpStatusCode);
	}

	public FireboltException(String message, Throwable cause, ExceptionType type) {
		super(message, cause);
		this.type = type;
	}

	private ExceptionType getExceptionType(Integer httpStatusCode) {
		if (httpStatusCode == null) {
			return ERROR;
		}
		switch (httpStatusCode) {
		case SC_NOT_FOUND:
			return RESOURCE_NOT_FOUND;
		case SC_UNAUTHORIZED:
			return EXPIRED_TOKEN;
		default:
			return ERROR;
		}
	}
}
