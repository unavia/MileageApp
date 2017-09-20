package ca.kendallroth.mileageapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class CreateAccountActivity extends AppCompatActivity {

  // Create Account asynchronous task
  private CreateAccountTask mAuthTask;

  // UI references.
  private Button mCreateAccountButton;
  private Button mLoginPromptButton;
  private EditText mEmailView;
  private EditText mNameView;
  private EditText mPasswordView;
  private EditText mPasswordConfirmView;
  private TextInputLayout mEmailViewLayout;
  private TextInputLayout mNameViewLayout;
  private TextInputLayout mPasswordViewLayout;
  private TextInputLayout mPasswordConfirmViewLayout;
  private ProgressDialog mProgressDialog;
  private View mCreateAccountFormView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_create_account);

    // Email field
    mEmailView = (EditText) findViewById(R.id.email);
    mEmailViewLayout = (TextInputLayout) findViewById(R.id.email_layout);

    // Name field
    mNameView = (EditText) findViewById(R.id.name);
    mNameViewLayout = (TextInputLayout) findViewById(R.id.name_layout);

    // Password field
    mPasswordView = (EditText) findViewById(R.id.password);
    mPasswordViewLayout = (TextInputLayout) findViewById(R.id.password_layout);

    // Password confirmation field
    mPasswordConfirmView = (EditText) findViewById(R.id.password_confirm);
    mPasswordConfirmViewLayout = (TextInputLayout) findViewById(R.id.password_confirm_layout);

    // Create account button
    mCreateAccountButton = (Button) findViewById(R.id.create_account_button);
    mCreateAccountButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        doAccountCreate();
      }
    });

    // Login prompt button
    mLoginPromptButton = (Button) findViewById(R.id.login_button);
    mLoginPromptButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        displayLoginActivity();
      }
    });

    // Progress dialog
    mProgressDialog = new ProgressDialog(this);
    mProgressDialog.setMessage(getString(R.string.progress_message_create_account));

    // Layout views
    mCreateAccountFormView = (View) findViewById(R.id.create_account_form);

    // Hide the Action bar (on all "Authentication" activities)
    getSupportActionBar().hide();
  }


  /**
   * Validate the email address
   * @param email Email address
   * @return Whether email address is valid
   */
  private boolean isEmailValid(String email) {
    //TODO: Replace with updated logic
    return email.contains("@") && email.contains(".");
  }

  /**
   * Validate the user's name
   * @param name User name
   * @return Whether name is valid
   */
  private boolean isNameValid(String name) {
    // TODO: Replace with updated logic
    return name.length() > 2;
  }

  /**
   * Validate the password
   * @param password Password
   * @return Whether password is valid
   */
  private boolean isPasswordValid(String password) {
    //TODO: Replace with updated logic
    return password.length() > 4;
  }

  /**
   * Validate the confirmation password
   * @param password        Password
   * @param passwordConfirm Confirmation password
   * @return Whether the confirmation password matches
   */
  private boolean isPasswordConfirmValid(String password, String passwordConfirm) {
    return password.equals(passwordConfirm);
  }


  /**
   * Display the Login activity
   */
  private void displayLoginActivity() {
    // TODO: Expand this later

    // Finish the activity (should display "Login" activity)
    finish();
  }


  /**
   * Attempt to create an account
   */
  private void doAccountCreate() {
    // Only permit one "Account Create" request at a time
    if (mAuthTask != null) {
      return;
    }

    // Reset errors.
    mEmailViewLayout.setError(null);
    mNameViewLayout.setError(null);
    mPasswordViewLayout.setError(null);
    mPasswordConfirmViewLayout.setError(null);

    // Store values at the time of the login attempt.
    String email = mEmailView.getText().toString();
    String name = mNameView.getText().toString();
    String password = mPasswordView.getText().toString();
    String passwordConfirm = mPasswordConfirmView.getText().toString();

    boolean cancel = false;
    View focusView = null;

    // Check for a valid password confirmation (must match), if the user entered one.
    if (TextUtils.isEmpty(password)) {
      mPasswordConfirmViewLayout.setError(getString(R.string.error_field_required));
      focusView = mPasswordConfirmView;
      cancel = true;
    }
    else if (!isPasswordConfirmValid(password, passwordConfirm)) {
      mPasswordConfirmViewLayout.setError(getString(R.string.error_mismatching_passwords));
      focusView = mPasswordConfirmView;
      cancel = true;
    }

    // Check for a valid password, if the user entered one.
    if (TextUtils.isEmpty(password)) {
      mPasswordViewLayout.setError(getString(R.string.error_field_required));
      focusView = mPasswordView;
      cancel = true;
    }
    else if (!isPasswordValid(password)) {
      mPasswordViewLayout.setError(getString(R.string.error_invalid_password));
      focusView = mPasswordView;
      cancel = true;
    }

    // Check for a valid email address
    if (TextUtils.isEmpty(email)) {
      mEmailViewLayout.setError(getString(R.string.error_field_required));
      focusView = mEmailView;
      cancel = true;
    } else if (!isEmailValid(email)) {
      mEmailViewLayout.setError(getString(R.string.error_invalid_email));
      focusView = mEmailView;
      cancel = true;
    }

    // Check for a valid name (is last to set focus properly).
    if (TextUtils.isEmpty(name)) {
      mNameViewLayout.setError(getString(R.string.error_field_required));
      focusView = mNameView;
      cancel = true;
    } else if (!isNameValid(name)) {
      mNameViewLayout.setError(getString(R.string.error_invalid_name));
      focusView = mNameView;
      cancel = true;
    }

    if (cancel) {
      // Ignore create attempt (due to error) and set focus to last field with error
      focusView.requestFocus();
    } else {
      // Show a progress spinner and start a background task to perform the account creation attempt
      mProgressDialog.show();

      mAuthTask = new CreateAccountTask(email, name, password);
      mAuthTask.execute((Void) null);
    }
  }

  /**
   * Represents an asynchronous account creation task used to create a user account
   */
  public class CreateAccountTask extends AsyncTask<Void, Void, Boolean> {

    private final String mEmail;
    private final String mName;
    private final String mPassword;

    CreateAccountTask(String email, String name, String password) {
      mEmail = email;
      mName = name;
      mPassword = password;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
      // TODO: attempt authentication against a network service.

      try {
        // Simulate network access.
        Thread.sleep(2000);
      } catch (InterruptedException e) {
        return false;
      }

      // Deny account creation if account already "exists"
      if (mEmail.equals("kendall@example.com")) {
        return false;
      }

      return true;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
      mAuthTask = null;
      mProgressDialog.dismiss();

      if (success) {
        // NOTE: Determine what to do on successful account creation (likely send email)
        finish();
      } else {
        mEmailViewLayout.setError(getString(R.string.error_account_already_exists));
        mEmailViewLayout.requestFocus();
      }

      // NOTE: Won't display currently due to "finishing" the activity
      // Define a snackbar based on the operation status
      CharSequence snackbarResource = success
          ? getString(R.string.success_create_account)
          : getString(R.string.failure_create_account);
      Snackbar resultSnackbar = Snackbar.make(mCreateAccountFormView, snackbarResource, Snackbar.LENGTH_SHORT);
      resultSnackbar.show();
    }

    @Override
    protected void onCancelled() {
      // NOTE: Should it be possible to cancel Account Creation?
      mAuthTask = null;
      mProgressDialog.dismiss();
    }
  }
}
