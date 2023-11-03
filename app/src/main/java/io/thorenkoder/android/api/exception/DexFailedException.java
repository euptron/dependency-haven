package io.thorenkoder.android.api.exception;

public class DexFailedException extends RuntimeException {

	/**
	 * Creates exception with the specified message. If you are wrapping another exception, consider
	 * using {@link #DexFailedException(String, Throwable)} instead.
	 *
	 * @param msg error message describing a possible cause of this exception.
	 */
	public DexFailedException(String msg) {
		super(msg);
	}

	/**
	 * Creates exception with the specified message and cause.
	 *
	 * @param msg error message describing what happened.
	 * @param cause root exception that caused this exception to be thrown.
	 */
	public DexFailedException(String msg, Throwable cause) {
		super(msg, cause);
	}

	/**
	 * Creates exception with the specified cause. Consider using
	 * {@link #DexFailedException(String, Throwable)} instead if you can describe what happened.
	 *
	 * @param cause root exception that caused this exception to be thrown.
	 */
	public DexFailedException(Throwable cause) {
		super(cause);
	}
}