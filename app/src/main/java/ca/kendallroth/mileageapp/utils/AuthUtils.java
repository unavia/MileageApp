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
    } catch (Exception e) {
      // Handle file exceptions
      responseStatus = StatusCode.FAILURE;
      responseString = "code_failure_init_account_file_parse";
    }

    Log.d("MileageApp.auth", String.format("createAuthFile: %s", responseString));

    return new Response(responseStatus, responseString);
  }


  /**
   * Check if a user exists in the authentication file
   * @param email Email identifier
   * @return Operation response with status and message
   */
  public static Response findAuthUser(String email) {
    if (fileContext == null) return invalidFileContext;

    Document document;
    StatusCode responseStatus = null;
    String responseString = "";

    try {
      // Read XML file with user information
      document = XMLFileUtils.getFile(fileContext, XMLFileUtils.USERS_FILE_NAME);

      // Select all the "user" nodes in the document
      List<Node> users = document.selectNodes("/users/user");

      boolean doesUserExist = false;

      // Validate that the user email is unique
      for (Node user : users) {
        if (user.valueOf("@email").equals(email)) {
          doesUserExist = true;

          break;
        }
      }

      if (doesUserExist) {
        responseStatus = StatusCode.SUCCESS;
        responseString = "code_success_find_account";
      } else {
        responseStatus = StatusCode.ERROR;
        responseString = "code_error_find_account";
      }
    } catch (Exception e) {
      // Handle file exceptions
      responseStatus = StatusCode.FAILURE;
      responseString = "code_failure_find_account_file_parse";
    }

    Log.d("MileageApp.auth", String.format("findAuthUser ('%s'): %s", email, responseString));

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
    } catch (Exception e) {
      // Handle file exceptions
      responseStatus = StatusCode.FAILURE;
      responseString = "code_failure_add_account_file_parse";
    }

    Log.d("MileageApp.auth", String.format("addAuthUser ('%s'): %s", email, responseString));

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
        responseStatus = StatusCode.ERROR;
        responseString = "code_warning_remove_account_not_found";
      }
    } catch (Exception e) {
      // Handle file exceptions
      responseStatus = StatusCode.FAILURE;
      responseString = "code_failure_remove_account_file_parse";
    }

    Log.d("MileageApp.auth", String.format("removeAuthUser (%s): %s", email, responseString));

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

    Document document;
    StatusCode responseStatus = null;
    String responseString = "";

    try {
      // Read XML file with user information
      document = XMLFileUtils.getFile(fileContext, XMLFileUtils.USERS_FILE_NAME);

      // Select all the "user" nodes in the document
      List<Node> users = document.selectNodes("/users/user");

      // Verify that the requested user email exists and the old password is correct
      for (Node user : users) {
        if (user.valueOf("@email").equals(email)) {
          if (user.valueOf("@password").equals(oldPassword)) {
            // Update the password
            Element userElement = (Element) user;
            userElement.addAttribute("password", newPassword);

            // Write the updated file
            XMLFileUtils.createFile(fileContext, XMLFileUtils.USERS_FILE_NAME, document);

            responseStatus = StatusCode.SUCCESS;
            responseString = "code_success_password_reset";
          } else {
            // Handle incorrect old password
            responseStatus = StatusCode.ERROR;
            responseString = "code_failure_password_reset_invalid_password";
          }

          break;
        }
      }

      // Set a warning status if the requested account (email) was not found
      if (responseStatus == null) {
        responseStatus = StatusCode.ERROR;
        responseString = "code_warning_password_reset_account_not_found";
      }
    } catch (Exception e) {
      // Handle file exceptions
      responseStatus = StatusCode.FAILURE;
      responseString = "code_failure_password_reset_file_parse";
    }

    Log.d("MileageApp.auth", String.format("updateAuthUserPassword (%s): %s", email, responseString));

    return new Response(responseStatus, responseString);
  }


  /**
   * Update an authenticated user's password
   * @param email       User email for location
   * @param newPassword User's new password
   * @return Operation response with status and message
   */
  public static Response setAuthUserPassword(String email, String newPassword) {
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
        responseStatus = StatusCode.ERROR;
        responseString = "code_warning_password_reset_account_not_found";
      }
    } catch (Exception e) {
      // Handle file exceptions
      responseStatus = StatusCode.FAILURE;
      responseString = "code_failure_password_reset_file_parse";
    }

    Log.d("MileageApp.auth", String.format("setAuthUserPassword (%s): %s", email, responseString));

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
    } catch (Exception e) {
      // Handle file exceptions
      responseStatus = StatusCode.FAILURE;
      responseString = "code_failure_auth_file_reset_file_parse";
    }

    Log.d("MileageApp.auth", String.format("resetAuthFile: %s", responseString));

    return new Response(responseStatus, responseString);
  }
}
