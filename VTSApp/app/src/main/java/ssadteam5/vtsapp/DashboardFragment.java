package ssadteam5.vtsapp;

import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment
{
    private View view;
    private SwipeRefreshLayout swipeLayout;
//    private RecyclerView recyclerView;
    private VehicleCardAdapter vehicleCardAdapter;
    private List<VehicleCard> vehicleCardList;
    private DeviceFetchTask mFetchTask;
    private TableLayout tableLayout;
    private UserData userData;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        vehicleCardList = new ArrayList<>();
        vehicleCardAdapter = new VehicleCardAdapter(getContext(),vehicleCardList);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(vehicleCardAdapter);
        userData = new UserData(getActivity().getApplicationContext());
        createSummaryTable();
        vehicleCardAdapter.notifyDataSetChanged();

        mFetchTask = new DeviceFetchTask();
        mFetchTask.execute((Void) null);

        swipeLayout = view.findViewById(R.id.swipeRefreshDashboard);
        swipeLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        userData.destroyResponse();
                        vehicleCardList.clear();
                        mFetchTask = new DeviceFetchTask();
                        mFetchTask.execute((Void) null);
                    }
                }
        );
        return view;
    }

    @Override
    public void onViewCreated(View view,@Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Dashboard");
    }

    private void createSummaryTable()
    {
        tableLayout = view.findViewById(R.id.summary);
        TextView heading = new TextView(getActivity());
        heading.setText("Summary");
        heading.setTextAppearance(getActivity(), R.style.TextAppearance_AppCompat_Large);
        heading.setPadding(50,20,50,20);
        tableLayout.addView(heading);
        tableLayout.addView(createRow("Active Vehicles"));
        tableLayout.addView(createRow("Inactive Vehicles"));
        tableLayout.addView(createRow("Total Distance Covered"));
        tableLayout.addView(createRow("Alerts Generated"));

    }
    private TableRow createRow(String a)
    {
        TableRow tr = new TableRow(getActivity());
        TextView tv1 = new TextView(getActivity());
        tv1.setTypeface(null, Typeface.BOLD);
        tv1.setText(a);
        tr.addView(tv1);
        TextView tv2 = new TextView(getActivity());
        tv2.setText("-");
        tr.addView(tv2);
        tr.setPadding(50, 20, 50, 20);
        return tr;
    }
    public class DeviceFetchTask extends AsyncTask<Void, Void, Boolean>
    {
        DeviceFetchTask() {
        }
        @Override
        protected Boolean doInBackground(Void... params)
        {
            Log.d("Fragment", "Dashboard");
            try {
                if(!userData.isDataFetched())
                {
                    userData.fetchData();
                }
                String response = userData.getResponse().get(UserData.KEY_RESPONSE);
                JSONObject obj=new JSONObject(response);
                JSONArray arr=obj.getJSONArray("deviceDTOS");
                for(int i=0;i<arr.length();i++)
                {
                    JSONObject ob=arr.getJSONObject(i);
                    VehicleCard vehicleCard = new VehicleCard(ob.getString("name"));
                    vehicleCardList.add(vehicleCard);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return true;
        }
        @Override
        protected void onPostExecute(final Boolean success)
        {
            if(getActivity() != null)
            {
                TableRow row = (TableRow) tableLayout.getChildAt(1);
                TextView textView = (TextView)row.getChildAt(1);
                textView.setText(""+vehicleCardAdapter.getItemCount());
                vehicleCardAdapter.notifyDataSetChanged();
                swipeLayout.setRefreshing(false);
            }
        }

        @Override
        protected void onCancelled()
        {
        }
    }
}
