package ca.kendallroth.mileageapp.utils;

/**
 * Utility class to indicate the response of an action
 */
public class Response {
  private StatusCode statusCode;
  private String message;
  private Object data;

  public StatusCode getStatusCode() {
    return this.statusCode;
  }

  public String getMessage() {
    return this.message;
  }

  public Object getData() {
    return this.data;
  }

  /**
   * Response from an operation
   * @param statusCode Operation completion status code
   * @param message    Operation completion message
   */
  public Response(StatusCode statusCode, String message) {
    this.statusCode = statusCode;
    this.message = message;
  }

  /**
   * Response from an operation
   * @param statusCode Operation completion status code
   * @param message    Operation completion message
   * @param data       Generic completion data
   */
  public Response(StatusCode statusCode, String message, Object data) {
    this.statusCode = statusCode;
    this.message = message;
    this.data = data;
  }

  /**
   * Check if the response succeeded with an optional warning
   * @return Whether the response succeeded
   */
  public boolean isSuccess() {
    return this.statusCode == StatusCode.SUCCESS || this.statusCode == StatusCode.WARNING;
  }

  /**
   * Check if the response failed or has errors
   * @return Whether the response failed
   */
  public boolean isFailure() {
    return this.statusCode == StatusCode.ERROR || this.statusCode == StatusCode.FAILURE;
  }

  /**
   * Convert the Response to a string
   * @return String representation of Response object
   */
  @Override
  public String toString() {
    return String.format("Status Code: '%s'  -  Message: '%s'", statusCode, message);
  }
}
