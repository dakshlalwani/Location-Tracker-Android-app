package ssadteam5.vtsapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class NavDrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener
{
    private String token;
    private String[] menu;

    private UserSessionManager session;
    private static final String PREFS_NAME = "LoginPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        // First this activity will be opened and if user is logged in, then it is okay, else login activity will be displayed
        session = new UserSessionManager(getApplicationContext());
        // Check user login (this is the important point)
        // If User is not logged in , This will redirect user to LoginActivity
        // and finish current activity from activity stack.
        if(session.checkLogin())
        {
            Log.d("check","finish");
            finish();
        }
        else
        {
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            FloatingActionButton fab = findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    launchMap();
                }
            });
            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.setDrawerListener(toggle);
            toggle.syncState();
            String email = session.getUserDetails().get(UserSessionManager.KEY_EMAIL);
            String tenant = session.getUserDetails().get(UserSessionManager.KEY_TENANT);
            token = session.getUserDetails().get(UserSessionManager.KEY_TOKEN);

            NavigationView navigationView = findViewById(R.id.nav_view);
            Menu navMenu = navigationView.getMenu();
            View hview = navigationView.getHeaderView(0);
            TextView navtenant = hview.findViewById(R.id.tenantName);
            TextView navemail = hview.findViewById(R.id.emailname);

            navtenant.setText(tenant.toUpperCase());
            navemail.setText(email);

            menu = new String[4];
            menu[0] = "Dashboard";
            menu[1] = "Devices";
            menu[2] = "Reports";
            menu[3] = "Logout";
            for (int i = 0; i < menu.length-1; i++)
            {
                navMenu.add(Menu.NONE, i, i, menu[i]);
            }
            navMenu.add(Menu.CATEGORY_CONTAINER,menu.length -1,menu.length-1,menu[menu.length -1]);
            navigationView.setNavigationItemSelectedListener(this);
            if(savedInstanceState == null)
            {
                dashboardFragment();
            }
        }
    }
    private void launchMap()
    {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }
    @Override
    public void onBackPressed()
    {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.drawer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        if (item.getItemId() == R.id.action_logout)
        {
           logout();
        }

        return super.onOptionsItemSelected(item);
    }
    private void logout()
    {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.commit();
        session.logoutUser();
        finishAfterTransition();
    }
    private void deviceListFragment()
    {
        Fragment fragment;
        Bundle bundle = new Bundle();
        bundle.putString("token",token);
        fragment = new DeviceListFragment();
        fragment.setArguments(bundle);
        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        tx.replace(R.id.content_frame, fragment);
        tx.commit();
    }
    private void reportsFragment()
    {
        Fragment fragment = new ReportsFragment();
        Bundle bundle = new Bundle();
        bundle.putString("token",token);
        fragment.setArguments(bundle);
        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        tx.replace(R.id.content_frame, fragment);
        tx.commit();
    }
    private void dashboardFragment()
    {
        Fragment fragment;
        Bundle bundle = new Bundle();
        bundle.putString("token",token);
        fragment = new DashboardFragment();
        fragment.setArguments(bundle);
        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        tx.replace(R.id.content_frame, fragment);
        tx.commit();
    }
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch (menu[id])
        {
            case "Devices":
                deviceListFragment();
                break;
            case "Dashboard":
                dashboardFragment();
                break;
            case "Reports":
                reportsFragment();
                break;
            case "Logout":
                logout();
                return true;
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
