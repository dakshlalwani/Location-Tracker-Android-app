package ssadteam5.vtsapp;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.auth0.android.jwt.Claim;
import com.auth0.android.jwt.JWT;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONObject;

import okhttp3.WebSocket;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.client.StompClient;

public class TrackVehicleActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener
{
    // DEFAULT DISPLAY VALUES
    private final String UNAVAILABLE = "N/A";

    private SlidingUpPanelLayout mLayout;
    private String deviceName;
    private android.support.v7.app.ActionBar actionBar;

    // Add handles to display real-time information
    private Switch ignitionStatusSwitch;
    private TextView gpsTimestampTextView;

    // User specific variables
    UserSessionManager session;
    private UserData userData;
    String organisationId;

    // Map specific variables
    private GoogleMap mGoogleMap;
    SupportMapFragment mapFrag;
    private Marker marker = null;
    boolean isMarkerRotating = false;

    // Websocket connection to consume real-time information
    private StompClient mStompClient;

    /**
     * INIT on activity page load
     */
    private void init(){
        // Initialize sliding pane layout
        mLayout = findViewById(R.id.sliding_layout);
        mLayout.setDragView(null);
        mLayout.setFadeOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
        });
        actionBar = getSupportActionBar();

        // Initialize display elements
        gpsTimestampTextView = findViewById(R.id.gpsTimestampView);
        ignitionStatusSwitch = findViewById(R.id.ignitionStatusSwitch);
        ignitionStatusSwitch.setClickable(false);
    }


    //FIXME Need to modularise this method for re-usability

    /**
     *  Gets called when activity page is created.
     *
     * Page pre-load operations go here
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_vehicle);
        init();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        SupportMapFragment mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map2);
        UserSessionManager session = new UserSessionManager(getApplicationContext());
        userData = new UserData(getApplicationContext());
        mapFrag.getMapAsync(this);

        String token = session.getUserDetails().get(UserSessionManager.KEY_TOKEN);
        deviceName = getIntent().getExtras().getString("deviceName");

        // FIXME Update title of the map to show vehicle license plate number
        getSupportActionBar().setTitle(deviceName);
        // Setting sliding panel text
        //setPanelText();


        JWT jwt = new JWT(token);
        Claim claim = jwt.getClaim("organisationId");
        String organisationId = claim.asString();

        mStompClient = Stomp.over(WebSocket.class,getString(R.string.web_socket));
        mStompClient.connect();
        mStompClient.topic("/device/message" + organisationId).subscribe(topicMessage -> {

            // FIXME Need to add time filter here for GPSTimestamp field to avoid showing historical data
            JSONObject payload = new JSONObject(topicMessage.getPayload());
            try
            {
                // Retrieve values being sent on topic here
                final String deviceId = payload.has("DeviceId") ? payload.getString("DeviceId") : UNAVAILABLE;
                final DateTime gpsUtcTimestamp = payload.has("GPSTimestamp") ? DateTime.parse(payload.getString("GPSTimestamp")) : null;
                final String gpsLocaleTimestamp = gpsUtcTimestamp != null ? gpsUtcTimestamp.withZone(
                        DateTimeZone.forOffsetHoursMinutes(5,30)
                ).toString("hh:mm:ss a") : UNAVAILABLE;
                double lat = payload.has("Latitude") ? payload.getDouble("Latitude") : 0.0000;
                final double lon = payload.has("Longitude") ? payload.getDouble("Longitude") : 0.0000;
                final String speed = payload.has("Speed") ? payload.getString("Speed") : "0.0";
                final String FuelLevel = payload.has("FuelLevel") ? payload.getString("FuelLevel") : UNAVAILABLE;
                final String GSMStrength = payload.has("GSMStrength") ? payload.getString("GSMStrength") : UNAVAILABLE;
                final String InternalBatteryVoltage = payload.has("InternalBatteryVoltage") ? payload.getString("InternalBatteryVoltage") : UNAVAILABLE;
                final String EngineStatus = payload.has("EngineStatus") ? payload.getString("EngineStatus") : UNAVAILABLE;
                final float courseOverGround = payload.has("CourseOverGround") ? new Double(payload.getDouble("CourseOverGround")).floatValue() : 0;

                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable()
                {
                    public void run()
                    {
                        boolean New = true;
                        if(marker != null && deviceId.equals(marker.getTag().toString())) {
                            gpsTimestampTextView.setText(gpsLocaleTimestamp);
                            ignitionStatusSwitch.setChecked("ON".equals(EngineStatus));
                            Marker oldpos = marker;
                            oldpos.setSnippet(
                                    "Time: " + gpsLocaleTimestamp + "\n" +
                                    "Ignition: " + EngineStatus + "\n" +
                                    "Latitude: " + lat + "\n" +
                                    "Longitude: " + lon + "\n" +
                                    "Speed: " + speed + "KM/H" + "\n" +
                                    "GSM Strength: " + GSMStrength
                            );
                            /* To update the opened Info window */
                            if(oldpos.isInfoWindowShown()) {
                                oldpos.hideInfoWindow();
                                oldpos.showInfoWindow();
                            }
                            /**/
                            if(courseOverGround != 0)
                                rotateMarker(marker, new LatLng(lat, lon), getAngle(oldpos.getPosition(), new LatLng(lat, lon)));
                            mGoogleMap.addCircle(new CircleOptions()
                                    .center(oldpos.getPosition())
                                    .radius(2)
                                    .strokeColor(Color.RED)
                                    .fillColor(Color.RED));
                        }
                        else if(deviceName.equals(deviceId)) {
                            gpsTimestampTextView.setText(gpsLocaleTimestamp);
                            ignitionStatusSwitch.setChecked("ON".equals(EngineStatus));
                            marker = mGoogleMap.addMarker(new MarkerOptions()
                                .position(new LatLng(lat, lon))
                                .title("LOCATION INFO")
                            );
                            int height = 140;
                            int width = 70;
                            BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.car4);
                            Bitmap b=bitmapdraw.getBitmap();
                            Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
                            marker.setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));
                            marker.setTag(deviceId);
                            marker.setSnippet(
                                    "Time: " + gpsLocaleTimestamp + "\n" +
                                    "Ignition: " + EngineStatus + "\n" +
                                    "Latitude: " + lat + "\n" +
                                    "Longitude: " + lon + "\n" +
                                    "Speed: " + speed + "KM/H" + "\n" +
                                    "GSM Strength: " + GSMStrength
                            );
                            marker.setAnchor(0.5f, 0.5f);
                            marker.setInfoWindowAnchor(0.5f, 0.5f);
                            marker.setRotation(courseOverGround);
                            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(),16));
                        }
                    }
                });
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onMapClick(LatLng point){
        if(actionBar.isShowing()){
            actionBar.hide();
            /*** Can not implement hidePanel() due to buggy google map view ***/
        }
        else{
            actionBar.show();
        }
        Log.d("ThePointIs", point.toString());
    }

    /**
     * Invoked when google map is loaded.
     *
     * Initialize google map parameters here
     *
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mGoogleMap=googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        LatLng latLng = new LatLng(17.9,78);
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,5));
        mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
        mGoogleMap.getUiSettings().setCompassEnabled(true);
        mGoogleMap.getUiSettings().setRotateGesturesEnabled(false);
        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener()
        {
            @Override
            public boolean onMarkerClick(final Marker marker)
            {
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(),16));
                marker.showInfoWindow();
                return true;
            }
        });
        mGoogleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            // TODO Need to style info window to show information more cleanly
            @Override
            public View getInfoContents(Marker marker) {

                LinearLayout info = new LinearLayout(getApplicationContext());
                info.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(getApplicationContext());
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());

                TextView snippet = new TextView(getApplicationContext());
                snippet.setTextColor(Color.GRAY);
                snippet.setText(marker.getSnippet());

                info.addView(title);
                info.addView(snippet);

                return info;
            }
        });
        mGoogleMap.setOnMapClickListener(this);
    }

    /**
     * Maintain websocket connection even when back button is pressed
     */
    @Override
    public void onBackPressed()
    {
        if (mLayout != null && (mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED || mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED)) {
            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }
        else {
//            mStompClient.topic("/device/message" + organisationId).unsubscribeOn();
            super.onBackPressed();
        }
    }
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        mStompClient.disconnect();
        Log.d("stompinfo", mStompClient.isConnected()+"");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                Intent parentIntent = NavUtils.getParentActivityIntent(this);
                parentIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(parentIntent);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Used to align the icon on the map appropriately based on the latest GPS information received
     *
     * @param marker
     * @param destination
     * @param rotation
     */
    private void rotateMarker(final Marker marker, final LatLng destination, final float rotation) {

        if (marker != null) {

            final LatLng startPosition = marker.getPosition();
            final float startRotation = marker.getRotation();

            final long start = SystemClock.uptimeMillis();

            final LatLngInterpolator latLngInterpolator = new LatLngInterpolator.Spherical();
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
            valueAnimator.setDuration(3000); // duration 3 second
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {

                    try {
                        long elapsed = SystemClock.uptimeMillis() - start;
                        float v = animation.getAnimatedFraction();
                        LatLng newPosition = latLngInterpolator.interpolate(v, startPosition, destination);
                        float bearing = computeRotation(v, startRotation, rotation);

                        marker.setRotation(bearing);
                        marker.setPosition(newPosition);
                        if(elapsed > 3000){
                            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(),16));
                        }
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
            valueAnimator.start();
        }
    }

    // FIXME Move these helper functions to util class

    /**
     * START HELPER FUNCTIONS
     */

    /**
     *
     * Helper function to calculate rotation of the car icon on the map
     *
     * @param fraction
     * @param start
     * @param end
     * @return
     */
    private static float computeRotation(float fraction, float start, float end) {
        float normalizeEnd = end - start; // rotate start to 0
        float normalizedEndAbs = (normalizeEnd + 360) % 360;

        float direction = (normalizedEndAbs > 180) ? -1 : 1; // -1 = anticlockwise, 1 = clockwise
        float rotation;
        if (direction > 0) {
            rotation = normalizedEndAbs;
        } else {
            rotation = normalizedEndAbs - 360;
        }

        float result = fraction * rotation + start;
        return (result + 360) % 360;
    }

    /**
     *
     * Helper function to calculate angle of the car icon shown on the map
     *
     * @param source
     * @param destination
     * @return
     */
    private static float getAngle(LatLng source, LatLng destination) {

        // calculate the angle theta from the deltaY and deltaX values
        // (atan2 returns radians values from [-PI,PI])
        // 0 currently points EAST.
        // NOTE: By preserving Y and X param order to atan2,  we are expecting
        // a CLOCKWISE angle direction.
        double theta = Math.atan2(
                destination.longitude - source.longitude, destination.latitude - source.latitude);

        // rotate the theta angle clockwise by 90 degrees
        // (this makes 0 point NORTH)
        // NOTE: adding to an angle rotates it clockwise.
        // subtracting would rotate it counter-clockwise
        theta -= Math.PI / 2.0;

        // convert from radians to degrees
        // this will give you an angle from [0->270],[-180,0]
        double angle = Math.toDegrees(theta);

        // convert to positive range [0-360)
        // since we want to prevent negative angles, adjust them now.
        // we can assume that atan2 will not return a negative value
        // greater than one partial rotation
        if (angle < 0) {
            angle += 360;
        }

        return (float) angle + 90;
    }

    /**
     * END HELPER FUNCTIONS
     */

    /*
    private void setPanelText(){
        String response = userData.getResponse().get(UserData.KEY_RESPONSE);
        Log.d("myresp", response);
        try {
            String vehicleDetailsDO;
            int idx = 0;
            JSONObject obj = new JSONObject(response);
            JSONArray arr = obj.getJSONArray("deviceDTOS");
            for(int i = 0;i < arr.length(); i++){
                JSONObject ob=arr.getJSONObject(i);
                if(ob.getString("name").equals(deviceName)) {
                    idx = i;
                }
            }
            vehicleDetailsDO = arr.getJSONObject(idx).getString("vehicleDetailsDO");

            TableLayout t = (TableLayout) findViewById(R.id.vehicleInformationPanel);
            TableRow row = new TableRow(this);
            TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
            row.setLayoutParams(lp);
            TextView qty = new TextView(this);
            qty.setText("Test : " + deviceName);
            row.addView(qty);
            t.addView(row);

            if(!vehicleDetailsDO.equals("null")) {
                JSONObject vehicleObj = new JSONObject(vehicleDetailsDO);
//                String vehicleColor = vehicleObj.getString("color");
                String make = vehicleObj.getString("make");
                String nextService = vehicleObj.getString("nextServiceOn");
                String notes = vehicleObj.getString("notes");
                String vehicleNumber = vehicleObj.getString("vehicleNumber");
                String vehicleType = vehicleObj.getString("vehicleType");
                String vehicleName = vehicleObj.getString("vehicleName");
                TableRow row1 = new TableRow(this);
                row1.setLayoutParams(lp);
                TextView qty1 = new TextView(this);
                qty1.setText("Vehicle Name: " + vehicleName);
                row1.addView(qty1);

                TableRow row2 = new TableRow(this);
                row2.setLayoutParams(lp);
                TextView qty2 = new TextView(this);
                qty2.setText("Vehicle Number: " + vehicleNumber);
                row2.addView(qty2);

                TableRow row3 = new TableRow(this);
                row3.setLayoutParams(lp);
                TextView qty3 = new TextView(this);
                qty3.setText("Vehicle Type: " + vehicleType);
                row3.addView(qty3);

                TableRow row4 = new TableRow(this);
                row4.setLayoutParams(lp);
                TextView qty4 = new TextView(this);
                qty4.setText("Make: " + make);
                row4.addView(qty4);

                TableRow row5 = new TableRow(this);
                row5.setLayoutParams(lp);
                TextView qty5 = new TextView(this);
                qty5.setText("Next Service: " + nextService);
                row5.addView(qty5);

                TableRow row6 = new TableRow(this);
                row6.setLayoutParams(lp);
                TextView qty6 = new TextView(this);
                qty6.setText("Notes: " + notes);
                row6.addView(qty6);

                t.addView(row1);
                t.addView(row2);
                t.addView(row3);
                t.addView(row4);
                t.addView(row5);
                t.addView(row6);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }

    }*/

}
