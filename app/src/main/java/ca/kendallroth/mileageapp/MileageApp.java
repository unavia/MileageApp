package ca.kendallroth.mileageapp;

import android.app.Application;
import android.util.Log;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.io.File;

/**
 * Custom Application class to handle checking for authentication file on app start.
 *   Taken from https://stackoverflow.com/questions/7360846/how-can-i-execute-something-just-once-per-application-start
 */
public class MileageApp extends Application {
  /**
   * Constructor
   */
  public MileageApp() {
    super();
  }

  @Override
  public void onCreate() {
    super.onCreate();

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
    try {
      Document outputDocument = DocumentHelper.createDocument();
      Element root = outputDocument.addElement("users");

      // Add the initial user
      root.addElement("user")
          .addAttribute("email", "kendall@example.com")
          .addAttribute("name", "Example")
          .addAttribute("password", "hello");

      // Create the XML file
      XMLFileUtils.createFile(getBaseContext(), XMLFileUtils.USERS_FILE_NAME, outputDocument);

      Log.d("MileageApp", "Users file created with sample user");
    } catch(Exception e) {
      Log.e("MileageApp", "XML file creation failed");
    }
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
