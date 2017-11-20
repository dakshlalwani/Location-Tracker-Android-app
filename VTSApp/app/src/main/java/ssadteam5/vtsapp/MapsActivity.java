package ssadteam5.vtsapp;

import android.animation.ValueAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
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

import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.WebSocket;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.client.StompClient;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener
{
    private GoogleMap mGoogleMap;
    private StompClient mStompClient;
    private final ArrayList<Marker> markerList = new ArrayList<>();
    private Float courseOverGround = null;
    private int flag;
    private android.support.v7.app.ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        actionBar = getSupportActionBar();
        getSupportActionBar().setTitle("Map");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        SupportMapFragment mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);

        UserSessionManager session = new UserSessionManager(getApplicationContext());
        String token = session.getUserDetails().get(UserSessionManager.KEY_TOKEN);

        JWT jwt = new JWT(token);
        Claim claim = jwt.getClaim("organisationId");
        String organisationId = claim.asString();

        mStompClient = Stomp.over(WebSocket.class,getString(R.string.web_socket));
        mStompClient.connect();
        Log.d("yololo", "hello");
        mStompClient.topic("/device/message" + organisationId).subscribe(topicMessage -> {
            JSONObject payload = new JSONObject(topicMessage.getPayload());
            try
            {
                final String deviceName = payload.get("DeviceId").toString();
                Double lat = Double.parseDouble(payload.get("Latitude").toString());
                Double lon = Double.parseDouble(payload.get("Longitude").toString());
                final String speed = payload.get("Speed").toString();
                final String FuelLevel = payload.get("FuelLevel").toString();
                final String GSMStrength = payload.get("GSMStrength").toString();
                final String InternalBatteryVoltage = payload.get("InternalBatteryVoltage").toString();
                final String EngineStatus = payload.get("EngineStatus").toString();
                flag = 0;
                try {
                    courseOverGround = Float.parseFloat(payload.get("CourseOverGround").toString());
                }
                catch (Exception e){
                    flag = 1;
                    e.printStackTrace();
                }
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable()
                {
                    public void run()
                    {
                        boolean New = true;
                        for (int i = 0; i < markerList.size(); i++)
                        {
                            Log.d("test", deviceName);
                            Log.d("test", markerList.get(i).getTag().toString());
                            if (deviceName.equals(markerList.get(i).getTag().toString()))
                            {
                                Marker oldpos = markerList.get(i);
                                oldpos.setSnippet("Engine Status: " + EngineStatus + "\n" + "Speed: " + speed + "Km/h" + "\n" + "Fuel Level: " + FuelLevel + "\n" + "Internal Battery Voltage: " + InternalBatteryVoltage + "\n" +
                                        "GSM Strength: " + GSMStrength);
                                /* To update the opened Info window */
                                if(oldpos.isInfoWindowShown()) {
                                    oldpos.hideInfoWindow();
                                    oldpos.showInfoWindow();
                                }
                                /**/
                                if(flag == 1)
                                    courseOverGround = getAngle(oldpos.getPosition(), new LatLng(lat, lon));
                                rotateMarker(markerList.get(i), new LatLng(lat, lon), courseOverGround);
                                mGoogleMap.addCircle(new CircleOptions()
                                        .center(oldpos.getPosition())
                                        .radius(2)
                                        .strokeColor(Color.RED)
                                        .fillColor(Color.RED));
                                New = false;
                            }
                            Log.d("mynew", deviceName + "  " + markerList.get(i).getTag().toString());
                        }
                        if (New)
                        {
                            Marker amarker = mGoogleMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(lat, lon ))
                                    .title(deviceName));
                            int height = 160;
                            int width = 80;
                            BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.car4);
                            Bitmap b=bitmapdraw.getBitmap();
                            Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
                            amarker.setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));
                            amarker.setTag(deviceName);
                            amarker.setSnippet("Engine Status: " + EngineStatus + "\n" + "Speed: " + speed + "Km/h" + "\n" + "Fuel Level: " + FuelLevel + "\n" + "Internal Battery Voltage: " + InternalBatteryVoltage + "\n" +
                                    "GSM Strength: " + GSMStrength);
                            amarker.setAnchor(0.5f, 0.5f);
                            amarker.setInfoWindowAnchor(0.5f, 0.5f);
                            if(flag == 0)
                                amarker.setRotation(courseOverGround);
                            markerList.add(amarker);
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

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        Log.d("destroy", "here");
        mStompClient.disconnect();
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
    private static final CharSequence[] MAP_TYPE_ITEMS =
            {"Road Map", "Hybrid", "Satellite", "Terrain"};

    private void showMapTypeSelectorDialog()
    {
        // Prepare the dialog by setting up a Builder.
        final String fDialogTitle = "Select Map Type";
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(fDialogTitle);

        // Find the current map type to pre-check the item representing the current state.
        int checkItem = mGoogleMap.getMapType() - 1;

        // Add an OnClickListener to the dialog, so that the selection will be handled.
        builder.setSingleChoiceItems(
                MAP_TYPE_ITEMS,
                checkItem,
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int item) {
                        // Locally create a finalised object.

                        // Perform an action depending on which item was selected.
                        switch (item) {
                            case 1:
                                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                                break;
                            case 2:
                                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                                break;
                            case 3:
                                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                                break;
                            default:
                                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        }
                        dialog.dismiss();
                    }
                }
        );

        // Build the dialog and show it.
        AlertDialog fMapTypeDialog = builder.create();
        fMapTypeDialog.setCanceledOnTouchOutside(true);
        fMapTypeDialog.show();
    }

    private void rotateMarker(final Marker marker, final LatLng destination, final float rotation) {

        if (marker != null) {

            final LatLng startPosition = marker.getPosition();
            final float startRotation = marker.getRotation();

            final LatLngInterpolator latLngInterpolator = new LatLngInterpolator.Spherical();
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
            valueAnimator.setDuration(3000); // duration 3 second
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {

                    try {
                        float v = animation.getAnimatedFraction();
                        LatLng newPosition = latLngInterpolator.interpolate(v, startPosition, destination);
                        float bearing = computeRotation(v, startRotation, rotation);

                        marker.setRotation(bearing);
                        marker.setPosition(newPosition);

                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
            valueAnimator.start();
        }
    }

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
        theta += Math.PI / 2.0;

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

        return (float) angle;
    }
}

