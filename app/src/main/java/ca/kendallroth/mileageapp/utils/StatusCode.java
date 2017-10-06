package ca.kendallroth.mileageapp.utils;

/**
 * Utility enum to designate the status code of a response
 */
public enum StatusCode {
  SUCCESS,  // Operation successful
  WARNING,  // Operation succeeded with warnings
  ERROR,    // Operation failed with errors
  FAILURE   // Operation did not complete
}
