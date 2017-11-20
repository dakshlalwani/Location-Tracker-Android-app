package ssadteam5.vtsapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

class UserData
{
    private final SharedPreferences pref;
    private final Editor editor;
    private final Context _context;
    // Sharedpref file name
    private static final String PREFER_NAME = "UserData";

    // User name (make variable public to access from outside)
    public static final String KEY_RESPONSE = "response";

    private static final String DATA_FETCHED = "data_fetched";

    private static final String REPORTS_COMPUTED = "reports_computed";
    private static final String REPORTS_FETCHED = "reports_fetched";
    public static final String KEY_REPORTS = "reports";
    private static final String START_DATE = "start_date";
    private static final String END_DATE = "end_date";
    private static final String VEHICLE_NO = "vehicle_no";

    public UserData(Context context)
    {
        this._context = context;
        int PRIVATE_MODE = 0;
        pref = _context.getSharedPreferences(PREFER_NAME, PRIVATE_MODE);
        editor = pref.edit();
        editor.commit();
    }

    public void fetchData(){
        UserSessionManager session = new UserSessionManager(_context);
        String mToken = session.getUserDetails().get(UserSessionManager.KEY_TOKEN);
        HttpURLConnection conn;
        try {
            StringBuilder response = new StringBuilder();
            URL url = new URL("http://eyedentifyapps.com:8080/api/auth/device/all/");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "*/*");
            conn.setRequestProperty("Authorization", "Bearer " + mToken);
            InputStream in = conn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(in);
            int inputStreamData = inputStreamReader.read();
            while (inputStreamData != -1)
            {
                char current = (char) inputStreamData;
                inputStreamData = inputStreamReader.read();
                response.append(current);
            }
            Log.d("resp", response.toString());
            editor.putString(KEY_RESPONSE, response.toString());
            editor.putBoolean(DATA_FETCHED, true);
            editor.commit();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void fetchReports(String mStartDate, String mEndDate, String mVehicleNo, String mToken){
        HttpURLConnection conn;
        try {
            String response;
            JSONObject jsonObject = new JSONObject();
            JSONObject jo = new JSONObject();
            JSONObject jo2 = new JSONObject();
            jo.put("$gt", mStartDate);
            jo.put("$lt", mEndDate);
            jo2.put("$gt", "0.0000");
            jsonObject.put("DeviceId", mVehicleNo);
            jsonObject.put("GPSTimestamp", jo);
            jsonObject.put("Latitude", jo2);
            Log.d("json", jsonObject.toString());
            URL url = new URL("http://eyedentifyapps.com:8080/api/native/query/APAC_EYES_GPS?orderBy=GPSTimestamp/");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + mToken);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(jsonObject.toString());
            wr.close();
            int count = 0;
            BufferedReader ini = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String temp;
            StringBuilder out = new StringBuilder();
            while ((temp = ini.readLine()) != null) {
                count += 1;
                Log.d("in-while", String.valueOf(count));
                out.append(temp);
            }
            response = out.toString();
            editor.putString(START_DATE, mStartDate);
            editor.putString(END_DATE, mEndDate);
            editor.putString(VEHICLE_NO, mVehicleNo);
            editor.putString(KEY_REPORTS, response);
            editor.putBoolean(REPORTS_FETCHED, true);
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

        /**
         * Get stored session data
         * */
    public HashMap<String, String> getResponse()
    {

        //Use hashmap to store user credentials
        HashMap<String, String> response = new HashMap<>();

        // user response
        response.put(KEY_RESPONSE, pref.getString(KEY_RESPONSE, null));

        return response;
    }

    /**
     * Get already stored dates
     * History of reports can be stored and shown to customer
     */
    public HashMap<String, String> getReports(){
        HashMap<String, String> reports = new HashMap<>();
        reports.put(KEY_REPORTS, pref.getString(KEY_REPORTS, null));
        return reports;
    }

    public boolean isReportFetched(String mStartDate, String mEndDate, String mVehicleNo){
        if(pref.getBoolean(REPORTS_FETCHED, false)){
            if(mStartDate.equals(pref.getString(START_DATE, null)) && mEndDate.equals(pref.getString(END_DATE, null)) && mVehicleNo.equals(pref.getString(VEHICLE_NO, null))){
                return true;
            }
        }
        return false;
    }
    public boolean isIdleReportComputed(String mStartDate, String mEndDate, String mVehicleNo){
        if(pref.getBoolean(REPORTS_COMPUTED, false)){
            if(mStartDate.equals(pref.getString(START_DATE, null)) && mEndDate.equals(pref.getString(END_DATE, null)) && mVehicleNo.equals(pref.getString(VEHICLE_NO, null))){
                return true;
            }
        }
        return false;
    }

    public boolean isDataFetched()
    {
        return pref.getBoolean(DATA_FETCHED, false);
    }

    public void destroyResponse()
    {
        editor.clear();
        editor.commit();
    }

//    private class dateReports{
//        private String startDate, endDate, vehicleNo;
//        public dateReports(String mStartDate, String mEndDate, String mVehicleNo){
//            startDate = mStartDate;
//            endDate = mEndDate;
//            vehicleNo = mVehicleNo;
//        };
//    }
}
