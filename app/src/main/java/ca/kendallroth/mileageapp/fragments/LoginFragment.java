package ca.kendallroth.mileageapp.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import ca.kendallroth.mileageapp.R;
import ca.kendallroth.mileageapp.activities.RequestPasswordResetActivity;
import ca.kendallroth.mileageapp.activities.ResetPasswordActivity;
import ca.kendallroth.mileageapp.utils.AccountUtils;
import ca.kendallroth.mileageapp.utils.AuthUtils;
import ca.kendallroth.mileageapp.utils.ClearableFragment;
import ca.kendallroth.mileageapp.utils.Response;
import ca.kendallroth.mileageapp.utils.ScrollableFragment;
import ca.kendallroth.mileageapp.utils.StatusCode;

import static android.app.Activity.RESULT_OK;

/**
 * Fragment to allow users to login (or automatically authenticate)
 */
public class LoginFragment extends Fragment implements ClearableFragment, ScrollableFragment {

  // Activity response codes
  public final int ACTIVITY_CODE_REQUEST_PASSWORD_RESET = 1;
  public final int ACTIVITY_CODE_RESET_PASSWORD = 2;

  // UI references.
  private Button mForgotPasswordButton;
  private Button mLoginButton;
  private EditText mEmailInput;
  private EditText mPasswordInput;
  private ScrollView mScrollView;
  private TextInputLayout mEmailViewLayout;
  private TextInputLayout mPasswordViewLayout;
  private ProgressDialog mProgressDialog;
  private View mFormLayout;

  // Helper constants for argument variable names
  private static final String ARG_TITLE = "mTitle";

  private String mTitle;

  private ILoginAttemptListener mILoginAttemptListener;

  // Login asynchronous task
  private LoginTask mAuthTask = null;

  public LoginFragment() {
    // Required empty public constructor
  }

  // Use this factory method to create a new instance of this fragment using the provided parameters
  public static LoginFragment newInstance(String title) {
    LoginFragment fragment = new LoginFragment();

    Bundle args = new Bundle();
    args.putString(ARG_TITLE, title);
    fragment.setArguments(args);

    return fragment;
  }

  /**
   * Activities that contain this fragment must implement the methods in this interface in order to
   *  communicated with the fragment.
   *  Example: "http://developer.android.com/training/basics/fragments/communicating.html"
   */
  public interface ILoginAttemptListener {
    /**
     * Trigger a login attempt response
     * @param success Whether login attempt was successful
     */
    public void onLoginAttempt(boolean success);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (getArguments() != null) {
      mTitle = getArguments().getString(ARG_TITLE);
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View loginView = inflater.inflate(R.layout.fragment_login, container, false);

    // Initialize the view and variables
    initView(loginView);

    return loginView;
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);

    // Verify that the interface was properly implemented in the Activity
    if (context instanceof ILoginAttemptListener) {
      mILoginAttemptListener = (ILoginAttemptListener) context;
    } else {
      throw new RuntimeException(context.toString() + " must implement ILoginAttemptListener");
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();

    mILoginAttemptListener = null;
  }

  /**
   * Handle responses from started activities in this Activity
   * @param requestCode Request code the activity was started with
   * @param resultCode  Result code from the Activity
   * @param data        Result data (Intent)
   */
  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    // Determine which activity response has been received
    switch(requestCode) {
      // Request password reset Activity result
      case ACTIVITY_CODE_REQUEST_PASSWORD_RESET:
        onRequestPasswordResetActivityResult(resultCode, data);
        break;
      // Reset password Activity result
      case ACTIVITY_CODE_RESET_PASSWORD:
        onResetPasswordActivityResult(resultCode, data);
        break;
      default:
        break;
    }
  }

  /**
   * Initialize the Fragment view after view creation
   * @param loginView Fragment view
   */
  private void initView(View loginView) {
    // Email input
    mEmailInput = (EditText) loginView.findViewById(R.id.email_input);
    mEmailViewLayout = (TextInputLayout) loginView.findViewById(R.id.email_layout);

    // Password input
    mPasswordInput = (EditText) loginView.findViewById(R.id.password_input);
    mPasswordViewLayout = (TextInputLayout) loginView.findViewById(R.id.password_layout);
    mPasswordInput.setOnEditorActionListener(new OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
        // Attempt login on <Enter> keypress while the Password field has focus
        if (id == R.id.login || id == EditorInfo.IME_NULL) {
          attemptLogin();
          return true;
        }
        return false;
      }
    });

    // Login button
    mLoginButton = (Button) loginView.findViewById(R.id.login_button);
    mLoginButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        attemptLogin();
      }
    });

    // Forgot password button
    mForgotPasswordButton = (Button) loginView.findViewById(R.id.forgot_password_button);
    mForgotPasswordButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        startRequestPasswordResetActivity();
      }
    });

    // Progress dialog
    mProgressDialog = new ProgressDialog(loginView.getContext());
    mProgressDialog.setMessage(getString(R.string.progress_message_login_attempt));

    // Layout views
    mFormLayout = loginView.findViewById(R.id.form_layout);
    mScrollView = (ScrollView) loginView.findViewById(R.id.scroll_view);
  }

  /**
   * Start the Request Password Reset activity
   */
  private void startRequestPasswordResetActivity() {
    // Start the Request Password Reset activity and set the result/callback code
    Intent requestPasswordResetIntent = new Intent(getActivity(), RequestPasswordResetActivity.class);
    startActivityForResult(requestPasswordResetIntent, ACTIVITY_CODE_REQUEST_PASSWORD_RESET);
  }

  /**
   * Start the Reset Password activity
   */
  private void startResetPasswordActivity(String accountEmail) {
    // Start the Reset Password activity and set the result/callback code
    Intent resetPasswordIntent = new Intent(getContext(), ResetPasswordActivity.class);
    resetPasswordIntent.putExtra("emailAccount", accountEmail);
    startActivityForResult(resetPasswordIntent, ACTIVITY_CODE_RESET_PASSWORD);
  }

  /**
   * Attempts to sign in or register the account specified by the login form.
   * If there are form errors (invalid email, missing fields, etc.), the
   * errors are presented and no actual login attempt is made.
   */
  private void attemptLogin() {
    if (mAuthTask != null) {
      return;
    }

    // Reset errors.
    mEmailViewLayout.setError(null);
    mPasswordViewLayout.setError(null);

    // Store values at the time of the login attempt.
    String email = mEmailInput.getText().toString();
    String password = mPasswordInput.getText().toString();

    boolean cancel = false;
    View focusView = null;

    // Check for a valid password, if the user entered one.
    if (TextUtils.isEmpty(password)) {
      mPasswordViewLayout.setError(getString(R.string.error_field_required));
      focusView = mPasswordInput;
      cancel = true;
    }
    else if (!AccountUtils.validatePassword(password)) {
      mPasswordViewLayout.setError(getString(R.string.error_invalid_password));
      focusView = mPasswordInput;
      cancel = true;
    }

    // Check for a valid email address (is last to set focus properly).
    if (TextUtils.isEmpty(email)) {
      mEmailViewLayout.setError(getString(R.string.error_field_required));
      focusView = mEmailInput;
      cancel = true;
    } else if (!AccountUtils.validateEmail(email)) {
      mEmailViewLayout.setError(getString(R.string.error_invalid_email));
      focusView = mEmailInput;
      cancel = true;
    }

    if (cancel) {
      // Ignore login attempt (due to error) and set focus to last field with error
      focusView.requestFocus();
    } else {
      // Hide the soft keyboard
      View view = getActivity().getCurrentFocus();
      if (view != null) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
      }

      // Show a progress spinner, and kick off a background task to perform the user login attempt.
      mProgressDialog.show();
      mAuthTask = new LoginTask(email, password);
      mAuthTask.execute((Void) null);
    }
  }

  /**
   * Prefill the login view after account creation
   * @param email Pre-filled email address
   */
  public void prefillLogin(String email) {
    // Reject the prefill attempt if invalid information is given
    if (!AccountUtils.validateEmail(email)) {
      return;
    }

    // Prefill the email field and focus on the password field
    mEmailInput.setText(email);
    mEmailViewLayout.setError(null);

    mPasswordInput.requestFocus();
  }

  /**
   * Clear the inputs and errors
   */
  public void clearInputs() {
    mEmailInput.setText("");
    mEmailViewLayout.setError(null);
    mEmailInput.requestFocus();

    mPasswordInput.setText("");
    mPasswordViewLayout.setError(null);
  }

  /**
   * Scroll to the top of the fragment
   */
  public void scrollToTop() {
    scrollToTop(true);
  }

  /**
   * Scroll to the top of the fragment
   * @param smoothScroll Whether smooth scrolling should be enabled
   */
  public void scrollToTop(boolean smoothScroll) {
    if (smoothScroll) {
      mScrollView.smoothScrollTo(0, 0);
    } else {
      mScrollView.scrollTo(0, 0);
    }
  }

  /**
   * Callback from request password reset Activity
   * @param resultCode Result code from the Activity
   * @param data       Result data (Intent)
   */
  private void onRequestPasswordResetActivityResult(int resultCode, Intent data) {
    boolean success = resultCode == RESULT_OK;
    String emailAccount = data.getStringExtra("emailAccount");

    // Start the Reset Password activity if the result was a success
    if (success) {
      Log.d("MileageApp", emailAccount);

      startResetPasswordActivity(emailAccount);
    } else {
      // TODO: Handle cancelling password reset request
    }

    // Use the android "content" layout as the snackbar anchor
    View snackbarRoot = getActivity().findViewById(android.R.id.content);

    // Define a snackbar based on the operation status
    CharSequence snackbarResource = success
        ? getString(R.string.success_request_password_reset)
        : getString(R.string.cancelled_request_password_reset);
    Snackbar resultSnackbar = Snackbar.make(snackbarRoot, snackbarResource, Snackbar.LENGTH_SHORT);
    resultSnackbar.show();
  }

  private void onResetPasswordActivityResult(int resultCode, Intent data) {
    boolean success = resultCode == RESULT_OK;

    // Use the android "content" layout as the snackbar anchor
    View snackbarRoot = getActivity().findViewById(android.R.id.content);

    // Define a snackbar based on the operation status
    CharSequence snackbarResource = success
        ? getString(R.string.success_reset_password)
        : getString(R.string.failure_reset_password);
    Snackbar resultSnackbar = Snackbar.make(snackbarRoot, snackbarResource, Snackbar.LENGTH_SHORT);
    resultSnackbar.show();
  }

  /**
   * Represents an asynchronous login/registration task used to authenticate the user.
   */
  private class LoginTask extends AsyncTask<Void, Void, Response> {

    private final String mEmail;
    private final String mPassword;

    LoginTask(String email, String password) {
      mEmail = email;
      mPassword = password;
    }

    @Override
    protected Response doInBackground(Void... params) {
      StatusCode responseStatus = null;
      String responseString = "";

      // TODO: attempt authentication against a network service.
      try {
        // Simulate network access.
        Thread.sleep(2000);
      } catch (InterruptedException e) {
        responseStatus = StatusCode.FAILURE;
        responseString = "code_failure_login_interrupted_connection";
        return new Response(responseStatus, responseString);
      }

      // Attempt authentication for the user
      return AuthUtils.findAuthUser(mEmail, mPassword);
    }

    @Override
    protected void onPostExecute(final Response response) {
      mAuthTask = null;
      mProgressDialog.dismiss();

      CharSequence snackbarResource = null;

      // Handle the login attempt response
      if (response.getStatusCode() == StatusCode.SUCCESS) {
        // Indicate the success of the login attempt in the parent callback
        if (mILoginAttemptListener != null) {
          mILoginAttemptListener.onLoginAttempt(true);
        }

        snackbarResource = getString(R.string.success_login);
      } else if (response.getStatusCode() == StatusCode.ERROR) {
        // Indicate an invalid password/account
        mPasswordViewLayout.setError(getString(R.string.error_incorrect_login));
        mPasswordInput.requestFocus();
      } else {
        // Login attempt failed (unknown)
        snackbarResource = getString(R.string.failure_login);
      }

      if (snackbarResource != null) {
        // Need to use the android "content" layout as the snackbar anchor (since this is a fragment)
        View snackbarRoot = getActivity().findViewById(android.R.id.content);
        Snackbar.make(snackbarRoot, snackbarResource, Snackbar.LENGTH_SHORT).show();
      }
    }

    @Override
    protected void onCancelled() {
      // NOTE: Should logging in be canceallable?
      mAuthTask = null;
      mProgressDialog.dismiss();
    }
  }
}
