package ca.kendallroth.mileageapp.utils;

import android.content.Context;
import android.util.Log;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

import java.util.List;

import ca.kendallroth.mileageapp.MileageApp;
import ca.kendallroth.mileageapp.R;

/**
 * Utility functions for authorization workflows
 */
public abstract class AuthUtils {

  // Associate file context when app starts
  public static Context fileContext = null;

  // Static response for invalid file context (if not set)
  private static Response invalidFileContext = new Response(
      StatusCode.FAILURE,
      MileageApp.getContext().getString(R.string.code_failure_file_context)
  );


  /**
   * Create the temporary authentication file
   * @return Operation response with status and message
   */
  public static Response createAuthFile() {
    if (fileContext == null) return invalidFileContext;

    Document document;
    StatusCode responseStatus = null;
    String responseString = "";

    try {
      // Create the XML document and add the root element
      document = DocumentHelper.createDocument();
      Element root = document.addElement("users");

      // Create the XML file
      XMLFileUtils.createFile(fileContext, XMLFileUtils.USERS_FILE_NAME, document);

      responseStatus = StatusCode.SUCCESS;
      responseString = "code_success_init_account_file";

      Log.d("MileageApp.auth", "Creating empty authentication file was successful");
    } catch (Exception e) {
      responseStatus = StatusCode.FAILURE;
      responseString = "code_failure_init_account_file_parse";

      Log.d("MileageApp.auth", "Creating authentication file failed with a file parsing error");
    }

    return new Response(responseStatus, responseString);
  }


  /**
   * Add an authenticated user to the auth file
   * @param email    User email
   * @param name     User name
   * @param password User password
   * @return Operation response with status and message
   */
  public static Response addAuthUser(String email, String name, String password) {
    if (fileContext == null) return invalidFileContext;

    Document document;
    StatusCode responseStatus = null;
    String responseString = "";

    try {
      // Read XML file with user information
      document = XMLFileUtils.getFile(fileContext, XMLFileUtils.USERS_FILE_NAME);

      // Select all the "user" nodes in the document
      Element usersNode = (Element) document.selectSingleNode("/users");
      List<Node> users = document.selectNodes("/users/user");

      boolean doesUserExist = false;

      // Validate that the user email is unique
      for (Node user : users) {
        if (user.valueOf("@email").equals(email)) {
          doesUserExist = true;

          break;
        }
      }

      // Only create the new user if the email is unique (not used)
      if (!doesUserExist) {
        usersNode.addElement("user")
            .addAttribute("email", email)
            .addAttribute("name", name)
            .addAttribute("password", password);

        // Write the updated file
        XMLFileUtils.createFile(fileContext, XMLFileUtils.USERS_FILE_NAME, document);

        responseStatus = StatusCode.SUCCESS;
        responseString = "code_success_add_account";
      } else {
        responseStatus = StatusCode.ERROR;
        responseString = "code_error_add_account_already_exists";
      }

      String addStatus = !doesUserExist ? " was successful" : " failed because the account already existed";
      Log.d("MileageApp.auth", String.format("Adding account for email '%s' and password '%s' %s", email, password, addStatus));
    } catch (Exception e) {
      // Return a failure status (no match) if the file parsing fails or throws an exception
      responseStatus = StatusCode.FAILURE;
      responseString = "code_failure_add_account_file_parse";

      Log.d("MileageApp.auth", String.format("Adding account for email '%s' failed with a file parsing error", email));
    }

    return new Response(responseStatus, responseString);
  }


  /**
   * Remove an authenticated user from the auth file
   * @param email    User email for location
   * @param password Confirmation password
   * @return Operation response with status and message
   */
  public static Response removeAuthUser(String email, String password) {
    if (fileContext == null) return invalidFileContext;

    Document document;
    StatusCode responseStatus = null;
    String responseString = "";

    try {
      // Read XML file with user information
      document = XMLFileUtils.getFile(fileContext, XMLFileUtils.USERS_FILE_NAME);

      // Select all the "user" nodes in the document
      List<Node> users = document.selectNodes("/users/user");

      // Verify that the requested user email exists
      for (Node user : users) {
        if (user.valueOf("@email").equals(email)) {
          if (user.valueOf("@password").equals(password)) {
            // Remove the user
            user.detach();

            // Write the updated file
            XMLFileUtils.createFile(fileContext, XMLFileUtils.USERS_FILE_NAME, document);

            responseStatus = StatusCode.SUCCESS;
            responseString = "code_success_remove_account";
          } else {
            responseStatus = StatusCode.ERROR;
            responseString = "code_error_remove_account_invalid_password";
          }

          // Once the user is found there is no need to continue searching (even if password was wrong)
          break;
        }
      }

      // Set a warning status if the requested account (email) was not found
      if (responseStatus == null) {
        responseStatus = StatusCode.WARNING;
        responseString = "code_warning_remove_account_not_found";
      }

      // Debugging operation status
      String removalStatus = "";
      switch (responseStatus) {
        case SUCCESS:
          removalStatus = " was successful";
          break;
        case WARNING:
          removalStatus = " did not complete (no account)";
          break;
        case ERROR:
        default:
          removalStatus = " failed with an invalid password";
          break;
      }

      Log.d("MileageApp.auth", String.format("Removing account for email '%s' and password '%s' %s", email, password, removalStatus));
    } catch (Exception e) {
      // Return a failure status (no match) if the file parsing fails or throws an exception
      responseStatus = StatusCode.FAILURE;
      responseString = "code_failure_remove_account_file_parse";

      Log.d("MileageApp.auth", String.format("Removing account for email '%s' failed with a file parsing error", email));
    }

    return new Response(responseStatus, responseString);
  }


  /**
   * Verify and update an authenticated user's password
   * @param email       User email for location
   * @param newPassword User's new password
   * @param oldPassword User's old password
   * @return Operation response with status and message
   */
  public static Response updateAuthUserPassword(String email, String newPassword, String oldPassword) {
    if (fileContext == null) return invalidFileContext;

    return new Response(null, MileageApp.getContext().getString(R.string.code_method_not_implemented));
  }


  /**
   * Update an authenticated user's password
   * @param email       User email for location
   * @param newPassword User's new password
   * @return Operation response with status and message
   */
  public static Response UpdateAuthUserPassword(String email, String newPassword) {
    if (fileContext == null) return invalidFileContext;

    Document document;
    StatusCode responseStatus = null;
    String responseString = "";

    try {
      // Read XML file with user information
      document = XMLFileUtils.getFile(fileContext, XMLFileUtils.USERS_FILE_NAME);

      // Select all the "user" nodes in the document
      List<Node> users = document.selectNodes("/users/user");

      // Verify that the requested user email exists (but don't alert if not)
      for (Node user : users) {
        if (user.valueOf("@email").equals(email)) {
          // Update the password
          Element userElement = (Element) user;
          userElement.addAttribute("password", newPassword);

          // Write the updated file
          XMLFileUtils.createFile(fileContext, XMLFileUtils.USERS_FILE_NAME, document);

          responseStatus = StatusCode.SUCCESS;
          responseString = "code_success_password_reset";
          break;
        }
      }

      // Set a warning status if the requested account (email) was not found
      if (responseStatus == null) {
        responseStatus = StatusCode.WARNING;
        responseString = "code_warning_password_reset_account_not_found";
      }

      String resetStatus = responseStatus == StatusCode.SUCCESS ? "was successful" : "did not complete (no account)";
      Log.d("MileageApp.auth", String.format("Password reset for email '%s' %s", email, resetStatus));
    } catch (Exception e) {
      // Return a failure status (no match) if the file parsing fails or throws an exception
      responseStatus = StatusCode.FAILURE;
      responseString = "code_failure_password_reset_file_parse";

      Log.d("MileageApp.auth", String.format("Password reset for email '%s' failed with a file parsing error", email));
    }

    return new Response(responseStatus, responseString);
  }


  /**
   * Reset the authentication file
   * @return Operation response with status and message
   */
  public static Response resetAuthFile() {
    if (fileContext == null) return invalidFileContext;

    Document document;
    StatusCode responseStatus = null;
    String responseString = "";

    try {
      // Read XML file with user information
      document = XMLFileUtils.getFile(fileContext, XMLFileUtils.USERS_FILE_NAME);

      // Select all the "user" nodes in the document
      List<Node> users = document.selectNodes("/users/user");

      // Remove all users from the file
      for(Node user : users) {
        user.detach();
      }

      // Write the updated file
      XMLFileUtils.createFile(fileContext, XMLFileUtils.USERS_FILE_NAME, document);

      responseStatus = StatusCode.SUCCESS;
      responseString = "code_success_auth_file_reset";

      Log.d("MileageApp.auth", "Authentication file reset");
    } catch (Exception e) {
      // Return a failure status (no match) if the file parsing fails or throws an exception
      responseStatus = StatusCode.FAILURE;
      responseString = "code_failure_auth_file_reset_file_parse";

      Log.d("MileageApp.auth", "Authentication file reset failed with a file parsing error");
    }

    return new Response(responseStatus, responseString);
  }
}
