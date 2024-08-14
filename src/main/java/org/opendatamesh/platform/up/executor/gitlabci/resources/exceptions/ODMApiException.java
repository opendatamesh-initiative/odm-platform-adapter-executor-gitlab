package org.opendatamesh.platform.up.executor.gitlabci.resources.exceptions;

import lombok.Setter;
import org.springframework.http.HttpStatus;

@Setter
public abstract class ODMApiException extends RuntimeException{

	ODMApiStandardErrors error;

	public ODMApiException() {
		super();
	}

	public ODMApiException(String message) {
        super(message);
    }

	public ODMApiException(ODMApiStandardErrors error, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		setError(error);
	}

	public ODMApiException(ODMApiStandardErrors error, String message, Throwable cause) {
		super(message, cause);
		setError(error);
	}

	public ODMApiException(ODMApiStandardErrors error, String message) {
		super(message);
		setError(error);
	}

	public ODMApiException(Throwable cause) {
		super(cause);
	}

    /**
	 * @return the error
	 */
	public ODMApiStandardErrors getStandardError() {
		return error;
	}

	/**
	 * @return the error code
	 */
	public String getStandardErrorCode() {
		return error!=null?error.code():null;
	}

	/**
	 * @return the error description
	 */
	public String getStandardErrorDescription() {
		return error!=null?error.description():null;
	}



	/**
	 * @return the errorName
	 */
	public String getErrorName() {
		return getClass().getSimpleName();
	}

	/**
	 * @return the status
	 */
	public abstract HttpStatus getStatus();


}