package ca.kendallroth.mileageapp.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import java.util.List;

import ca.kendallroth.mileageapp.R;
import ca.kendallroth.mileageapp.utils.AccountUtils;
import ca.kendallroth.mileageapp.utils.XMLFileUtils;

/**
 * Reset password activity that enables a user to reset their password
 */
public class ResetPasswordActivity extends AppCompatActivity {

  private Button mResetPasswordButton;
  private EditText mPasswordInput;
  private EditText mPasswordConfirmInput;
  private ProgressDialog mProgressDialog;
  private TextInputLayout mPasswordViewLayout;
  private TextInputLayout mPasswordConfirmViewLayout;
  private TextView mEmailView;

  // Email address that sent the request
  private String mEmailAccount;

  // Login asynchronous task
  private ResetPasswordTask mResetPasswordTask = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_reset_password);

    // Get the email associated with the password reset
    Intent intent = getIntent();
    mEmailAccount = intent.getStringExtra("emailAccount");

    // Exit immediately if no valid email address was provided for the reset
    if (!AccountUtils.validateEmail(mEmailAccount)) {
      // TODO: Needs to be polished (ie. proper return status?)
      Intent responseIntent = new Intent();
      responseIntent.putExtra("validEmail", false);
      finish();
    }

    // Email input (for display only)
    mEmailView = (TextView) findViewById(R.id.email_text);
    mEmailView.setText(String.format("%s: %s", getString(R.string.label_password_reset), mEmailAccount));

    // Password input
    mPasswordInput = (EditText) findViewById(R.id.password_input);
    mPasswordViewLayout = (TextInputLayout) findViewById(R.id.password_layout);

    // Password confirmation input
    mPasswordConfirmInput = (EditText) findViewById(R.id.password_confirm_input);
    mPasswordConfirmViewLayout = (TextInputLayout) findViewById(R.id.password_confirm_layout);

    // Password reset button
    mResetPasswordButton = (Button) findViewById(R.id.reset_password_button);
    mResetPasswordButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        // Attempt to reset the password
        attemptPasswordReset(mEmailAccount);
      }
    });

    // Progress dialog
    mProgressDialog = new ProgressDialog(this);
    mProgressDialog.setMessage(getString(R.string.progress_message_reset_password));

    // Hide the Action bar (on all "Authentication" activities)
    getSupportActionBar().hide();
  }

  /**
   * Attempts to reset a password for the account specified by the reset form. If there are
   *  form errors the errors are presented and no attempt is made.
   */
  private void attemptPasswordReset(String accountEmail) {
    if (mResetPasswordTask != null) {
      return;
    }

    // Reset errors
    mPasswordViewLayout.setError(null);
    mPasswordConfirmViewLayout.setError(null);

    // Store values at the time of the password reset attempt
    String password = mPasswordInput.getText().toString();
    String passwordConfirm = mPasswordConfirmInput.getText().toString();

    boolean cancel = false;
    View focusView = null;

    // Check for a valid password confirmation
    if (TextUtils.isEmpty(password)) {
      mPasswordViewLayout.setError(getString(R.string.error_field_required));
      focusView = mPasswordConfirmInput;
      cancel = true;
    } else if (!AccountUtils.validatePasswordConfirm(password, passwordConfirm)) {
      mPasswordViewLayout.setError(getString(R.string.error_mismatching_passwords));
      focusView = mPasswordInput;
      cancel = true;
    }

    // Check for a valid password
    if (TextUtils.isEmpty(password)) {
      mPasswordViewLayout.setError(getString(R.string.error_field_required));
      focusView = mPasswordConfirmInput;
      cancel = true;
    } else if (!AccountUtils.validatePassword(password)) {
      mPasswordViewLayout.setError(getString(R.string.error_invalid_password));
      focusView = mPasswordInput;
      cancel = true;
    }

    if (cancel) {
      // Ignore password reset attempt (due to error) and set focus to last field with error.
      focusView.requestFocus();
    } else {
      // Hide the soft keyboard
      View view = this.getCurrentFocus();
      if (view != null) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
      }

      // Show a progress spinner, and kick off a background task to perform the password reset.
      mProgressDialog.show();
      mResetPasswordTask = new ResetPasswordTask(accountEmail, password);
      mResetPasswordTask.execute((Void) null);
    }
  }

  /**
   * Clear the inputs and errors
   */
  public void clearInputs() {
    // Password confirmation input
    mPasswordInput.setText("");
    mPasswordViewLayout.setError(null);
    mPasswordInput.requestFocus();

    // Password input
    mPasswordInput.setText("");
    mPasswordViewLayout.setError(null);
    mPasswordInput.requestFocus();
  }


  /**
   * Represents an asynchronous task used to reset a password.
   */
  private class ResetPasswordTask extends AsyncTask<Void, Void, Boolean> {

    private final String mEmail;
    private final String mPassword;

    ResetPasswordTask(String email, String password) {
      mEmail = email;
      mPassword = password;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
      Log.d("MileageApp", String.format("Password reset attempt from '%s'", mEmail));

      // TODO: attempt authentication against a network service.
      try {
        // Simulate network access
        Thread.sleep(2000);
      } catch (InterruptedException e) {
        return false;
      }

      Document document;

      try {
        // Read XML file with user information
        document = XMLFileUtils.getFile(getBaseContext(), XMLFileUtils.USERS_FILE_NAME);

        // Select all the "user" nodes in the document
        List<Node> users = document.selectNodes("/users/user");

        boolean passwordResetSuccessful = false;

        // Verify that the requested user email exists (but don't alert if not)
        for (Node user : users) {
          if (user.valueOf("@email").equals(mEmail)) {
            // Update the password
            Element userElement = (Element) user;
            userElement.addAttribute("password", mPassword);

            // Write the updated file
            XMLFileUtils.createFile(getBaseContext(), XMLFileUtils.USERS_FILE_NAME, document);

            passwordResetSuccessful = true;
            break;
          }
        }

        Log.d("MileageApp.auth", String.format("Password reset for email %s", passwordResetSuccessful ? "successful" : "failed"));

        return passwordResetSuccessful;
      } catch (Exception e) {
        // Return false (no match) if the file parsing fails or throws an exception
        return false;
      }
    }

    @Override
    protected void onPostExecute(final Boolean success) {
      mResetPasswordTask = null;
      mProgressDialog.dismiss();

      if (success) {
        // Indicate a successful password reset
        setResult(RESULT_OK);
        finish();
      } else {
        // TODO: Indicate that the reset has failed
      }
    }

    @Override
    protected void onCancelled() {
      mResetPasswordTask = null;
      mProgressDialog.dismiss();
    }
  }
}
