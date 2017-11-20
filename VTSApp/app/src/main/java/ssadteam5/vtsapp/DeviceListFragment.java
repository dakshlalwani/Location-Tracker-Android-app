package ssadteam5.vtsapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DeviceListFragment extends Fragment
{
    private SwipeRefreshLayout swipeLayout;
    private DeviceFetchTask mFetchTask;
    private DeviceListAdapter DeviceListAdapter;
    private List<VehicleCard> vehicleCardList;
    private UserData userData;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_device_list, container, false);
        userData = new UserData(getActivity().getApplicationContext());


        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        vehicleCardList = new ArrayList<>();
        DeviceListAdapter = new DeviceListAdapter(getContext(),vehicleCardList);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getContext(), 1);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(DeviceListAdapter);
        DeviceListAdapter.notifyDataSetChanged();

        mFetchTask = new DeviceFetchTask();
        mFetchTask.execute((Void) null);

        swipeLayout = view.findViewById(R.id.swipeRefreshDeviceList);
        swipeLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        // This method performs the actual data-refresh operation.
                        // The method calls setRefreshing(false) when it's finished.
                        vehicleCardList.clear();
                        userData.destroyResponse();
                        mFetchTask = new DeviceFetchTask();
                        mFetchTask.execute((Void) null);
                    }
                }
        );
        return view;
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Devices");
    }
    public class DeviceFetchTask extends AsyncTask<Void, Void, Boolean>
    {
        DeviceFetchTask() {
        }
        @Override
        protected Boolean doInBackground(Void... params)
        {
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
                    JSONObject ob = arr.getJSONObject(i);
                    String name = ob.getString("name");
                    String account = ob.getString("account");
                    String description = ob.getString("description");
                    JSONObject vehicleDetails;
                    JSONObject driverDetails;
                    try
                    {
                        vehicleDetails = ob.getJSONObject("vehicleDetailsDO");
                    }
                    catch (JSONException e)
                    {
                        vehicleDetails = new JSONObject("{}");
                    }
                    try
                    {
                        driverDetails = ob.getJSONObject("driverDetailsDO");
                    }
                    catch (JSONException e)
                    {
                        driverDetails = new JSONObject("{}");
                    }
                    VehicleCard vehicleCard = new VehicleCard(name, account, description,vehicleDetails,driverDetails);
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
                DeviceListAdapter.notifyDataSetChanged();
                swipeLayout.setRefreshing(false);
            }
        }

        @Override
        protected void onCancelled()
        {
        }
    }



}


