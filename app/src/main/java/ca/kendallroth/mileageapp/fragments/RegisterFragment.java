package ca.kendallroth.mileageapp.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import java.util.List;

import ca.kendallroth.mileageapp.R;
import ca.kendallroth.mileageapp.utils.AccountUtils;
import ca.kendallroth.mileageapp.utils.AuthUtils;
import ca.kendallroth.mileageapp.utils.ClearableFragment;
import ca.kendallroth.mileageapp.utils.Response;
import ca.kendallroth.mileageapp.utils.StatusCode;
import ca.kendallroth.mileageapp.utils.XMLFileUtils;

/**
 * Fragment for enabling a user to register for the app
 */
public class RegisterFragment extends Fragment implements ClearableFragment {

  // Create Account asynchronous task
  private CreateAccountTask mAuthTask;

  // UI references.
  private Button mCreateAccountButton;
  private EditText mEmailView;
  private EditText mNameView;
  private EditText mPasswordView;
  private EditText mPasswordConfirmView;
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
    mEmailView = (EditText) createAccountView.findViewById(R.id.email_input);
    mEmailViewLayout = (TextInputLayout) createAccountView.findViewById(R.id.email_layout);

    // Name field
    mNameView = (EditText) createAccountView.findViewById(R.id.name_input);
    mNameViewLayout = (TextInputLayout) createAccountView.findViewById(R.id.name_layout);

    // Password field
    mPasswordView = (EditText) createAccountView.findViewById(R.id.password_input);
    mPasswordViewLayout = (TextInputLayout) createAccountView.findViewById(R.id.password_layout);

    // Password confirmation field
    mPasswordConfirmView = (EditText) createAccountView.findViewById(R.id.password_confirm_input);
    mPasswordConfirmViewLayout = (TextInputLayout) createAccountView.findViewById(R.id.password_confirm_layout);

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
    mEmailView.setText("");
    mEmailViewLayout.setError(null);
    mEmailView.requestFocus();

    mNameView.setText("");
    mNameViewLayout.setError(null);

    mPasswordView.setText("");
    mPasswordViewLayout.setError(null);

    mPasswordConfirmView.setText("");
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
    else if (!AccountUtils.validatePasswordConfirm(password, passwordConfirm)) {
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
    else if (!AccountUtils.validatePassword(password)) {
      mPasswordViewLayout.setError(getString(R.string.error_invalid_password));
      focusView = mPasswordView;
      cancel = true;
    }

    // Check for a valid email address
    if (TextUtils.isEmpty(email)) {
      mEmailViewLayout.setError(getString(R.string.error_field_required));
      focusView = mEmailView;
      cancel = true;
    } else if (!AccountUtils.validateEmail(email)) {
      mEmailViewLayout.setError(getString(R.string.error_invalid_email));
      focusView = mEmailView;
      cancel = true;
    }

    // Check for a valid name (is last to set focus properly).
    if (TextUtils.isEmpty(name)) {
      mNameViewLayout.setError(getString(R.string.error_field_required));
      focusView = mNameView;
      cancel = true;
    } else if (!AccountUtils.validateName(name)) {
      mNameViewLayout.setError(getString(R.string.error_invalid_name));
      focusView = mNameView;
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
  private class CreateAccountTask extends AsyncTask<Void, Void, Boolean> {

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

      Response createAccountResponse = AuthUtils.addAuthUser(mEmail, mName, mPassword);

      // TODO: Do something with the response
      return createAccountResponse.getStatusCode() == StatusCode.SUCCESS ? true : false;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
      mAuthTask = null;
      mProgressDialog.dismiss();

      if (success) {
        // NOTE: Determine what to do on successful account creation (likely send email)

        // Clear the form
        clearInputs();

        // TODO: Account creation should happen in parent
        if (mIAccountCreateListener != null) {
          // Set parent Activity to Login view using callback
          mIAccountCreateListener.onAccountCreateRequest(mEmail, mPassword);
        }
      } else {
        mEmailViewLayout.setError(getString(R.string.error_account_already_exists));
        mEmailViewLayout.requestFocus();
      }

      // Need to use the android "content" layout as the snackbar anchor (since this is a fragment)
      View snackbarRoot = getActivity().findViewById(android.R.id.content);

      // Define a snackbar based on the operation status
      CharSequence snackbarResource = success
          ? getString(R.string.success_create_account)
          : getString(R.string.failure_create_account);
      Snackbar resultSnackbar = Snackbar.make(snackbarRoot, snackbarResource, Snackbar.LENGTH_SHORT);
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
