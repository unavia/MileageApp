package ca.kendallroth.mileageapp.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import ca.kendallroth.mileageapp.R;
import ca.kendallroth.mileageapp.utils.AccountUtils;
import ca.kendallroth.mileageapp.utils.AuthUtils;
import ca.kendallroth.mileageapp.utils.ClearableFragment;
import ca.kendallroth.mileageapp.utils.Response;
import ca.kendallroth.mileageapp.utils.StatusCode;

/**
 * Fragment for enabling a user to register for the app
 */
public class RegisterFragment extends Fragment implements ClearableFragment {

  // Create Account asynchronous task
  private CreateAccountTask mAuthTask;

  // UI references.
  private Button mCreateAccountButton;
  private EditText mEmailInput;
  private EditText mNameInput;
  private EditText mPasswordInput;
  private EditText mPasswordConfirmInput;
  private TextInputLayout mEmailViewLayout;
  private TextInputLayout mNameViewLayout;
  private TextInputLayout mPasswordViewLayout;
  private TextInputLayout mPasswordConfirmViewLayout;
  private ProgressDialog mProgressDialog;

  // Helper constants for argument variable names
  private static final String ARG_TITLE = "mTitle";

  private String mTitle;

  private IAccountCreateListener mIAccountCreateListener;

  public RegisterFragment() {
    // Required empty public constructor
  }

  // Use this factory method to create a new instance of this fragment using the provided parameters
  public static RegisterFragment newInstance(String title) {
    RegisterFragment fragment = new RegisterFragment();

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
  public interface IAccountCreateListener {
    /**
     * Trigger an account creation process (only requested after account information has been validated)
     * @param email    Account email
     * @param password Account password
     */
    public void onAccountCreateRequest(String email, String password);
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
    View createAccountView = inflater.inflate(R.layout.fragment_register, container, false);

    // Initialize the view and variables
    this.initView(createAccountView);

    return createAccountView;
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);

    // Verify that the interface was properly implemented in the Activity
    if (context instanceof IAccountCreateListener) {
      mIAccountCreateListener = (IAccountCreateListener) context;
    } else {
      throw new RuntimeException(context.toString() + " must implement IAccountCreateListener");
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();

    mIAccountCreateListener = null;
  }

  /**
   * Initialize the Fragment view after view creation
   * @param createAccountView Fragment view
   */
  private void initView(View createAccountView) {
    // Email field
    mEmailInput = (EditText) createAccountView.findViewById(R.id.email_input);
    mEmailViewLayout = (TextInputLayout) createAccountView.findViewById(R.id.email_layout);

    // Name field
    mNameInput = (EditText) createAccountView.findViewById(R.id.name_input);
    mNameViewLayout = (TextInputLayout) createAccountView.findViewById(R.id.name_layout);

    // Password field
    mPasswordInput = (EditText) createAccountView.findViewById(R.id.password_input);
    mPasswordViewLayout = (TextInputLayout) createAccountView.findViewById(R.id.password_layout);

    // Password confirmation field
    mPasswordConfirmInput = (EditText) createAccountView.findViewById(R.id.password_confirm_input);
    mPasswordConfirmViewLayout = (TextInputLayout) createAccountView.findViewById(R.id.password_confirm_layout);
    mPasswordConfirmInput.setOnEditorActionListener(new OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        // Attempt registering on <Enter> keypress while the Confirm Password has focus
        if (actionId == R.id.password_confirm_input || actionId == EditorInfo.IME_NULL) {
          doAccountCreate();
          return true;
        }

        return false;
      }
    });

    // Create account button
    mCreateAccountButton = (Button) createAccountView.findViewById(R.id.create_account_button);
    mCreateAccountButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        doAccountCreate();
      }
    });

    // Progress dialog
    mProgressDialog = new ProgressDialog(getContext());
    mProgressDialog.setMessage(getString(R.string.progress_message_create_account));
  }


  /**
   * Clear the inputs and errors
   */
  public void clearInputs() {
    mEmailInput.setText("");
    mEmailViewLayout.setError(null);
    mEmailInput.requestFocus();

    mNameInput.setText("");
    mNameViewLayout.setError(null);

    mPasswordInput.setText("");
    mPasswordViewLayout.setError(null);

    mPasswordConfirmInput.setText("");
    mPasswordConfirmViewLayout.setError(null);
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
    String email = mEmailInput.getText().toString();
    String name = mNameInput.getText().toString();
    String password = mPasswordInput.getText().toString();
    String passwordConfirm = mPasswordConfirmInput.getText().toString();

    boolean cancel = false;
    View focusView = null;

    // Check for a valid password confirmation (must match), if the user entered one.
    if (TextUtils.isEmpty(passwordConfirm)) {
      mPasswordConfirmViewLayout.setError(getString(R.string.error_field_required));
      focusView = mPasswordConfirmInput;
      cancel = true;
    }
    else if (!AccountUtils.validatePasswordConfirm(password, passwordConfirm)) {
      mPasswordConfirmViewLayout.setError(getString(R.string.error_mismatching_passwords));
      focusView = mPasswordConfirmInput;
      cancel = true;
    }

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

    // Check for a valid email address
    if (TextUtils.isEmpty(email)) {
      mEmailViewLayout.setError(getString(R.string.error_field_required));
      focusView = mEmailInput;
      cancel = true;
    } else if (!AccountUtils.validateEmail(email)) {
      mEmailViewLayout.setError(getString(R.string.error_invalid_email));
      focusView = mEmailInput;
      cancel = true;
    }

    // Check for a valid name (is last to set focus properly).
    if (TextUtils.isEmpty(name)) {
      mNameViewLayout.setError(getString(R.string.error_field_required));
      focusView = mNameInput;
      cancel = true;
    } else if (!AccountUtils.validateName(name)) {
      mNameViewLayout.setError(getString(R.string.error_invalid_name));
      focusView = mNameInput;
      cancel = true;
    }

    if (cancel) {
      // Ignore create attempt (due to error) and set focus to last field with error
      focusView.requestFocus();
    } else {
      // Hide the soft keyboard
      View view = getActivity().getCurrentFocus();
      if (view != null) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
      }

      // Show a progress spinner and start a background task to perform the account creation attempt
      mProgressDialog.show();
      mAuthTask = new CreateAccountTask(email, name, password);
      mAuthTask.execute((Void) null);
    }
  }


  /**
   * Represents an asynchronous account creation task used to create a user account
   */
  private class CreateAccountTask extends AsyncTask<Void, Void, Response> {

    private final String mEmail;
    private final String mName;
    private final String mPassword;

    CreateAccountTask(String email, String name, String password) {
      mEmail = email;
      mName = name;
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
        responseString = "code_failure_register_interrupted_connection";
        return new Response(responseStatus, responseString);
      }

      // Create the new account
      return AuthUtils.addAuthUser(mEmail, mName, mPassword);
    }

    @Override
    protected void onPostExecute(final Response response) {
      mAuthTask = null;
      mProgressDialog.dismiss();

      CharSequence snackbarResource = null;

      // Handle the registration attempt response
      if (response.getStatusCode() == StatusCode.SUCCESS) {
        // TODO: Determine what to do on successful account creation (likely send email)

        // Clear the form
        clearInputs();

        // TODO: Account creation should happen in parent
        if (mIAccountCreateListener != null) {
          // Set parent Activity to Login view using callback
          mIAccountCreateListener.onAccountCreateRequest(mEmail, mPassword);

          snackbarResource = getString(R.string.success_create_account);
        }
      } else if (response.getStatusCode() == StatusCode.ERROR) {
        // Indicate that an account with this email already exists
        mEmailViewLayout.setError(getString(R.string.error_account_already_exists));
        mEmailViewLayout.requestFocus();
      } else {
        // Registration attempt failed (unknown)
        snackbarResource = getString(R.string.failure_registration);
      }

      // Need to use the android "content" layout as the snackbar anchor (since this is a fragment)
      if (snackbarResource != null) {
        View snackbarRoot = getActivity().findViewById(android.R.id.content);
        Snackbar.make(snackbarRoot, snackbarResource, Snackbar.LENGTH_SHORT).show();
      }
    }

    @Override
    protected void onCancelled() {
      // NOTE: Should it be possible to cancel Account Creation?
      mAuthTask = null;
      mProgressDialog.dismiss();
    }
  }
}
