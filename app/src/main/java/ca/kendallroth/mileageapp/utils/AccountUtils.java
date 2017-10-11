package ca.kendallroth.mileageapp.utils;

/**
 * Utility functions for Login and Create Account workflows
 */
public abstract class AccountUtils {

  /**
   * Validate an account email
   * @param email Email address
   * @return Whether the email address is valid
   */
  public static boolean validateEmail(String email) {
    //TODO: Replace with updated logic
    return email.contains("@") && email.contains(".");
  }

  /**
   * Validate an account user's name
   * @param name User name
   * @return Whether name is valid
   */
  public static boolean validateName(String name) {
    // TODO: Replace with updated logic
    return name.length() > 2;
  }

  /**
   * Validate an account password
   * @param password Password
   * @return Whether the password is valid
   */
  public static boolean validatePassword(String password) {
    //TODO: Replace with updated logic
    return password.length() >= 4;
  }

  /**
   * Validate an account confirmation password
   * @param password        Password
   * @param passwordConfirm Confirmation password
   * @return Whether the confirmation password matches
   */
  public static boolean validatePasswordConfirm(String password, String passwordConfirm) {
    return password.equals(passwordConfirm);
  }
}
