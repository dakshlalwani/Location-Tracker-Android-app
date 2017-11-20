package ssadteam5.vtsapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

public class Reports extends AppCompatActivity {
    private Bundle bund;
    private ViewPager viewPager;
    private PagerAdapter adapter;
    private TabLayout tabLayout;
    private UserData userData;
    private ProgressDialog Dialog;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_reports);
        Bundle b = getIntent().getExtras();
//        String token = getArguments().getString("token");
//        String startdate = getArguments().getString("startdate");
//        String enddate = getArguments().getString("enddate");
//        String vehicle = getArguments().getString("vehicle");

        Dialog = new ProgressDialog(Reports.this);
        String token = b.getString("token");
        String startdate = b.getString("startdate");
        String enddate = b.getString("enddate");
        String vehicle = b.getString("vehicle");
        Log.d("Dates", startdate + " " + enddate);

        tabLayout = findViewById(R.id.tabl);
        tabLayout.addTab(tabLayout.newTab().setText("Trip report"));
        tabLayout.addTab(tabLayout.newTab().setText("Idle report"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        userData = new UserData(Reports.this.getApplicationContext());


        viewPager = findViewById(R.id.pager);
        adapter = new PagerAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount(), bund);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        ReportsInfo mRepTask = new ReportsInfo(vehicle, startdate, enddate, token);
        mRepTask.execute((Void) null);
    }
    @Override
    public void onDestroy()
    {
        if(Dialog.isShowing()) Dialog.dismiss();
        super.onDestroy();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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

    public class ReportsInfo extends AsyncTask<Void, Void, String> {

        private final String mVehicleNo;
        private final String mStartDate;
        private final String mEndDate;
        private final String mToken;

        ReportsInfo(String vehicleNo, String startDate, String endDate, String token) {
            mVehicleNo = vehicleNo;
            mStartDate = startDate + "T00:00:00Z";
            mEndDate = endDate + "T23:59:59Z";
            mToken = token;
        }

        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Loading");
            Dialog.show();
        }


        @Override
        protected String doInBackground(Void... params) {
            Log.d("Fragment", "Reports");
            // Use the below statement for testing purposes
            /*if (!userData.isReportFetched(mStartDate, mEndDate, mVehicleNo))*/
            userData.fetchReports(mStartDate, mEndDate, mVehicleNo, mToken);
            String response = userData.getReports().get(UserData.KEY_REPORTS);
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            if(Dialog.isShowing())
                Dialog.hide();

            bund = new Bundle();
            bund.putString("resp", response);
            Log.d("resp", response);
            //viewPager.getAdapter().notifyDataSetChanged();
            adapter = new PagerAdapter
                    (getSupportFragmentManager(), tabLayout.getTabCount(), bund);
            viewPager.setAdapter(adapter);
            viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
            tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    viewPager.setCurrentItem(tab.getPosition());
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {

                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {

                }
            });

        }
    }
}
