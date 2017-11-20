package ssadteam5.vtsapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import ssadteam5.vtsapp.SortableTables.IdleTableDataAdapter;
import ssadteam5.vtsapp.SortableTables.SortableIdleTable;


public class IdleReport extends Fragment {
    private List<IdleReport.tableText> trip = new ArrayList<>();
    private ProgressDialog Dialog;
    private Activity mActivity;
    private Context mContext;
    private IdleTableDataAdapter idleTableDataAdapter;
    private SortableIdleTable sortableIdleTable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }
    @Override
    public void onDestroy()
    {
        if(Dialog.isShowing()) Dialog.dismiss();
        super.onDestroy();
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.idle_report, container, false);
        sortableIdleTable = (SortableIdleTable) view.findViewById(R.id.sortableIdle);
        mActivity = getActivity();
        mContext = getContext();
        Dialog = new ProgressDialog(mActivity);
        Bundle bundle = getArguments();
        if (bundle != null) {
            IdleFetchTask mFetchTask = new IdleFetchTask();
            mFetchTask.execute((Void) null);
        }
        return view;
    }

    public class IdleFetchTask extends AsyncTask<Void, Void, Boolean> {
        IdleFetchTask(){}
        @Override
        protected void onPreExecute() {
            Log.d("Fragment", "IdleReport");

            Dialog.setTitle("Finalising Data...");
            Dialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                JSONArray jsonArray = new JSONArray(getArguments().getString("resp"));
                int counti = 0;
                int k = 1, flagi = 0, i;
                long millis, second, minute, hour;
                String engst, Starttime = "", Endtime, locstart = "0.000000" + "," + "0.000000", locend = "0.000000" + "," + "0.000000", timedur, st, et, sti, eti, temp;
                for (i = 0; i < jsonArray.length(); i++) {
                    JSONObject ob = jsonArray.getJSONObject(i);
                    engst = ob.getString("EngineStatus");
                    if (Objects.equals(engst, "OFF")) {
                        counti = 1;
                        if (flagi == 0) {
                            Starttime = ob.getString("GPSTimestamp");
                            locstart = ob.getString("Latitude") + "," + ob.getString("Longitude");
                            flagi = 1;
                        }
                    } else if (Objects.equals(engst, "ON") && counti == 1) {
                        counti = 0;
                        flagi = 0;
                        JSONObject obj1 = jsonArray.getJSONObject(i - 1);
                        Endtime = obj1.getString("GPSTimestamp");
                        sti = Starttime.substring(Starttime.indexOf(0) + 1, Starttime.indexOf("T")) + " " + Starttime.substring(Starttime.indexOf("T") + 1, Starttime.indexOf("Z"));
                        eti = Endtime.substring(Endtime.indexOf(0) + 1, Endtime.indexOf("T")) + " " + Endtime.substring(Endtime.indexOf("T") + 1, Endtime.indexOf("Z"));
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date date1 = format.parse(sti);
                        Date date2 = format.parse(eti);
                        millis = java.lang.Math.abs(date2.getTime() - date1.getTime());
                        second = (millis / 1000) % 60;
                        minute = (millis / (1000 * 60)) % 60;
                        hour = (millis / (1000 * 60 * 60));
                        timedur = String.format("%02d:%02d:%02d", (hour), minute, second);
                        st = Starttime.substring(Starttime.indexOf(0) + 1, Starttime.indexOf("T")) + "   " + Starttime.substring(Starttime.indexOf("T") + 1, Starttime.indexOf("Z"));
                        et = Endtime.substring(Endtime.indexOf(0) + 1, Endtime.indexOf("T")) + "   " + Endtime.substring(Endtime.indexOf("T") + 1, Endtime.indexOf("Z"));
                        if ((date2.getTime() - date1.getTime()) < 0) {
                            temp = st;
                            st = et;
                            et = temp;
                        }
                        tableText mynewtext = new tableText();
                        mynewtext.setString(String.valueOf(k), 0);
                        mynewtext.setString(obj1.getString("DeviceId"), 1);
                        mynewtext.setString(st, 2);
                        mynewtext.setString(et, 3);
                        mynewtext.setString(locstart, 4);
                        mynewtext.setString(timedur, 5);
                        mynewtext.setCounti(counti);
                        mynewtext.setFlagi(flagi);
                        trip.add(mynewtext);
                        k++;
                    }
                }
                if (flagi == 1) {
                    JSONObject obj1 = jsonArray.getJSONObject(i - 1);
                    Endtime = obj1.getString("GPSTimestamp");
                    sti = Starttime.substring(Starttime.indexOf(0) + 1, Starttime.indexOf("T")) + " " + Starttime.substring(Starttime.indexOf("T") + 1, Starttime.indexOf("Z"));
                    eti = Endtime.substring(Endtime.indexOf(0) + 1, Endtime.indexOf("T")) + " " + Endtime.substring(Endtime.indexOf("T") + 1, Endtime.indexOf("Z"));
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date date1 = format.parse(sti);
                    Date date2 = format.parse(eti);
                    millis = java.lang.Math.abs(date2.getTime() - date1.getTime());
                    second = (millis / 1000) % 60;
                    minute = (millis / (1000 * 60)) % 60;
                    hour = (millis / (1000 * 60 * 60));
                    timedur = String.format("%02d:%02d:%02d", (hour), minute, second);
                    st = Starttime.substring(Starttime.indexOf(0) + 1, Starttime.indexOf("T")) + "   " + Starttime.substring(Starttime.indexOf("T") + 1, Starttime.indexOf("Z"));
                    et = Endtime.substring(Endtime.indexOf(0) + 1, Endtime.indexOf("T")) + "   " + Endtime.substring(Endtime.indexOf("T") + 1, Endtime.indexOf("Z"));
                    if ((date2.getTime() - date1.getTime()) < 0) {
                        temp = st;
                        st = et;
                        et = temp;
                    }
                    tableText mynewtext = new tableText();
                    mynewtext.setString(String.valueOf(k), 0);
                    mynewtext.setString(obj1.getString("DeviceId"), 1);
                    mynewtext.setString(st, 2);
                    mynewtext.setString(et, 3);
                    mynewtext.setString(locstart, 4);
                    mynewtext.setString(timedur, 5);
                    mynewtext.setCounti(counti);
                    mynewtext.setFlagi(flagi);
                    trip.add(mynewtext);
                }
            } catch (JSONException | ParseException e) {
                e.printStackTrace();
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (trip.size() == 0) {
                Toast.makeText(getActivity(), "No Idle Reports available for selected dates. Please select different dates", Toast.LENGTH_LONG).show();
            }
            if(sortableIdleTable != null) {
                idleTableDataAdapter = new IdleTableDataAdapter(getContext(), trip);
                idleTableDataAdapter.notifyDataSetChanged();
                sortableIdleTable.setDataAdapter(idleTableDataAdapter);
            }
            if(Dialog.isShowing())
                Dialog.hide();

        }
    }

    public class tableText {
        private final String[] text = new String[6];
        private int flagi, counti;

        public tableText() {
        }

        public void setString(String s, int idx) {
            text[idx] = s;
        }

        public void setFlagi(int flag) {
            flagi = flag;
        }

        public void setCounti(int count) {
            counti = count;
        }

        public String getString(int idx) {
            return text[idx];
        }

        public int getFlag() {
            return flagi;
        }

        public int getCount() {
            return counti;
        }
    }
}
