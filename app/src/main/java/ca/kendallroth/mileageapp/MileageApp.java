package ca.kendallroth.mileageapp;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import ca.kendallroth.mileageapp.utils.AuthUtils;
import ca.kendallroth.mileageapp.utils.Response;
import ca.kendallroth.mileageapp.utils.StatusCode;
import ca.kendallroth.mileageapp.utils.XMLFileUtils;

/**
 * Custom Application class to handle checking for authentication file on app start.
 *   Taken from https://stackoverflow.com/questions/7360846/how-can-i-execute-something-just-once-per-application-start
 */
public class MileageApp extends Application {

  // Expose the app context
  private static Context appContext;

  public static Context getContext() {
    return appContext;
  }

  public MileageApp() {
    super();
  }

  @Override
  public void onCreate() {
    super.onCreate();

    // Set the static app context (to enable access from "outside" files)
    appContext = getBaseContext();

    // Set the Authentication file context
    AuthUtils.fileContext = getBaseContext();

    // Check for the authentication file or create if it doesn't exist
    checkAuthenticationFile();
  }

  /**
   * Create the authentication file with initial values
   */
  private void checkAuthenticationFile() {
    // Skip this step if the authentication file already exists
    if(findAuthFile()) {
      return;
    }

    // Create the authentication file
    Response createAuthFileResponse = AuthUtils.createAuthFile();
    boolean createAuthFileSuccess = createAuthFileResponse.getStatusCode() == StatusCode.SUCCESS;

    // Add a sample user
    if (createAuthFileSuccess) {
      AuthUtils.addAuthUser("kendall@example.com", "Kendall Roth", "password");
    }

    // TODO: Do something with response
  }

  /**
   * Determine whether the authentication file already exists
   * @return Whether the authentication file already exists
   */
  private boolean findAuthFile() {
    // Find the authentication file in internal storage
    boolean authFileExists = getBaseContext().getFileStreamPath(XMLFileUtils.USERS_FILE_NAME).exists();

    String authFileStatus = authFileExists
        ? "Authentication file already exists"
        : "Authentication file doesn't exist";
    Log.d("MileageApp", authFileStatus);

    return authFileExists;
  }
}
