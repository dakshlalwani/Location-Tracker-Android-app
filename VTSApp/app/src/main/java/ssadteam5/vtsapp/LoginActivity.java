package ssadteam5.vtsapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * A login screen that offers login via email, password and tenant ID.
 */
public class LoginActivity extends AppCompatActivity
{
    /**
     *  These variables store the login response
     */
    private String status;    //The variable status determines whether the login is successful or not.
    private String errorCode;
    private String errorMessage;
    private String email;
    private String tenant;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private EditText mEmailView;
    private EditText mPasswordView;
    private EditText mTenantIdView;
    private View mProgressView;
    private View mLoginFormView;

    private UserSessionManager session;     // To store the user session

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        session = new UserSessionManager(getApplicationContext());

        // Set up the login form.
        mEmailView = findViewById(R.id.email);

        mPasswordView = findViewById(R.id.password);
        mTenantIdView = findViewById(R.id.tenantId);
        mTenantIdView.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent)
            {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    /**
     * Attempts to sign in the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin()
    {
        if (mAuthTask != null)
        {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);
        mTenantIdView.setError(null);

        // Store values at the time of the login attempt.
        email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String tenantId = mTenantIdView.getText().toString();
        tenant = tenantId;

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if ((!TextUtils.isEmpty(password) && !isPasswordValid(password)) || TextUtils.isEmpty(password))
        {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email))
        {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        }
        if (TextUtils.isEmpty(tenantId))
        {
            mTenantIdView.setError(getString(R.string.error_field_required));
            focusView = mTenantIdView;
            cancel = true;
        }
        else if (!isEmailValid(email))
        {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel)
        {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        }
        else
        {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
//            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
//            SharedPreferences.Editor editor = settings.edit();
//            editor.putString("logged", "logged");
//            editor.commit();
            if(isOnline())
            {
                showProgress(true);
                mAuthTask = new UserLoginTask(email, password, tenantId);
                mAuthTask.execute((Void) null);
            }
            else
            {
                Toast toast = Toast.makeText(this, getString(R.string.no_network_connection), Toast.LENGTH_LONG);
                toast.show();
            }
        }
    }

    private boolean isEmailValid(String email)
    {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password)
    {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show)
    {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter()
        {
            @Override
            public void onAnimationEnd(Animator animation)
            {
                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter()
        {
            @Override
            public void onAnimationEnd(Animator animation)
            {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }


    private void launchDrawer()
    {
        Log.d("check","drawer launched");
        Intent intent = new Intent(this, NavDrawerActivity.class);
        startActivity(intent);
    }
    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean>
    {

        private final String mEmail;
        private final String mPassword;
        private final String mTenantId;

        UserLoginTask(String email, String password, String tenantId)
        {
            mEmail = email;
            mPassword = password;
            mTenantId = tenantId;
        }

        @Override
        protected Boolean doInBackground(Void... params)
        {

            HttpURLConnection conn;
            try {
                StringBuilder response = new StringBuilder();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("email", mEmail);
                jsonObject.put("password", mPassword);
                jsonObject.put("tenantId", mTenantId);
                URL url = new URL(getString(R.string.login_controller));
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Accept","*/*");
                conn.setRequestProperty("Authorization","Eyedentify");
                conn.setRequestProperty("Content-Type","application/json");
                conn.setDoOutput(true);
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                wr.write(jsonObject.toString());
                wr.close();

                InputStream in = conn.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(in);
                int inputStreamData = inputStreamReader.read();
                while (inputStreamData != -1)
                {
                    char current = (char) inputStreamData;
                    inputStreamData = inputStreamReader.read();
                    response.append(current);
                }
                JSONObject resp = new JSONObject(response.toString());
                status = resp.get("status").toString();
                String token = resp.get("token").toString();
                errorCode = resp.get("errorCode").toString();
                errorMessage = resp.get("errorMessage").toString();
                session.createUserLoginSession(email, tenant, token);
                Thread.sleep(1000);
            } catch (Exception e)
            {
                e.printStackTrace();
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success)
        {
            mAuthTask = null;
            showProgress(false);

            if (success)
            {
                if(status.equals("SUCCESS"))
                {
                    launchDrawer();
                    finish();
                }
                else if(status.equals("FAILURE"))
                {
                    switch (errorCode)
                    {
                        case "3116":
                            mEmailView.setError(errorMessage);
                            mEmailView.requestFocus();
                            break;
                        case "4103":
                            mPasswordView.setError(errorMessage);
                            mPasswordView.requestFocus();
                            break;
                        case "4104":
                            mTenantIdView.setError(errorMessage);
                            mTenantIdView.requestFocus();
                            break;
                    }
                }
            }
            else
            {
                Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.error_incorrect_login), Toast.LENGTH_LONG);
                toast.show();
            }
        }

        @Override
        protected void onCancelled()
        {
            mAuthTask = null;
            showProgress(false);
        }
    }
    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        LoginActivity.this.finishAfterTransition();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // To check the internet services
    private boolean isOnline()
    {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }
}

