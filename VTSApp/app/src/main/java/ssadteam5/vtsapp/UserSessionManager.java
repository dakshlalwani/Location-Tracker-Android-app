package ssadteam5.vtsapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.HashMap;

class UserSessionManager
{
    // Shared Preferences reference
    private final SharedPreferences pref;
    // Editor reference for Shared preferences
    private final Editor editor;
    // Context
    private final Context _context;

    // Sharedpref file name
    private static final String PREFER_NAME = "AndroidExamplePref";

    // All Shared Preferences Keys
    private static final String IS_USER_LOGIN = "IsUserLoggedIn";

    // Email address (make variable public to access from outside)
    public static final String KEY_EMAIL = "email";

    // Tenant Id (make variable public to acces from outside)
    public static final String KEY_TENANT = "tenant";

    // Token needed to be stored
    public static final String KEY_TOKEN = "token";

    // Constructor
    public UserSessionManager(Context context)
    {
        this._context = context;
        int PRIVATE_MODE = 0;
        pref = _context.getSharedPreferences(PREFER_NAME, PRIVATE_MODE);
        editor = pref.edit();
        editor.commit();
    }

    //Create login session
    public void createUserLoginSession(String email, String tenant, String token)
    {
        // Storing login value as TRUE
        editor.putBoolean(IS_USER_LOGIN, true);

        // Storing email in pref
        editor.putString(KEY_EMAIL, email);

        // Storing tenant id in pref
        editor.putString(KEY_TENANT, tenant);

        // Storing the unique token
        editor.putString(KEY_TOKEN, token);
        // commit changes
        editor.commit();
    }

    /**
     * Check login method will check user login status
     * If false it will redirect user to login page
     * Else do anything
     * */
    public boolean checkLogin()
    {
        // Check login status
        if(!this.isUserLoggedIn())
        {
            // user is not logged in redirect him to Login Activity
            Intent i = new Intent(_context, LoginActivity.class);

            // Closing all the Activities from stack
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Add new Flag to start new Activity
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Staring Login Activity
            _context.startActivity(i);

            return true;
        }
        return false;
    }

    /**
     * Get stored session data
     * */
    public HashMap<String, String> getUserDetails()
    {
        //Use hashmap to store user credentials
        HashMap<String, String> user = new HashMap<>();

        // user email id
        user.put(KEY_EMAIL, pref.getString(KEY_EMAIL, null));

        // user tenant in string format
        user.put(KEY_TENANT, pref.getString(KEY_TENANT, null));
        user.put(KEY_TOKEN, pref.getString(KEY_TOKEN, null));
        // return user
        return user;
    }

    /**
     * Clear session details
     * */
    public void logoutUser()
    {
        // Clearing all user data from Shared Preferences
        editor.clear();
        editor.commit();
        // Clearing all user responses of UserData Class
        UserData userData = new UserData(_context);
        userData.destroyResponse();

        // After logout redirect user to Login Activity
        Intent i = new Intent(_context, LoginActivity.class);

        // Closing all the Activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Add new Flag to start new Activity
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Staring Login Activity
        _context.startActivity(i);
    }


    // Check for login
    private boolean isUserLoggedIn()
    {
        return pref.getBoolean(IS_USER_LOGIN, false);
    }
}