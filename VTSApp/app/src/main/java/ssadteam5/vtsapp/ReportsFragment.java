package ssadteam5.vtsapp;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class ReportsFragment extends Fragment {
    public static ReportsFragment newInstance() {
        return new ReportsFragment();
    }

    private View view;
    private String token;
    private Spinner spinner;
    private final List<String> list = new ArrayList<>();
    private final List<String> listVal = new ArrayList<>();

    private EditText tvDisplayDate1;
    private EditText tvDisplayDate2;

    private int year;
    private int month;
    private int day;
    private Boolean flag1 = false;
    private Boolean flag2 = false;
    private String startdate = "";
    private String enddate = "";
    private static final int DATE_DIALOG_ID = 999;
    private UserData userData;
    private StringBuilder sdate;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_reports, container, false);
        token = getArguments().getString("token");
        userData = new UserData(getActivity().getApplicationContext());

        FetchDevNo mFetchTask = new FetchDevNo(token);
        mFetchTask.execute((Void) null);
        setCurrentDateOnView();
        addListenerOnButton();
        Button btnsub = view.findViewById(R.id.btnsub);
        EditText changeDate2 = view.findViewById(R.id.e2);
        changeDate2.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent)
            {
                if (id == R.id.btnsub || id == EditorInfo.IME_NULL) {
                    InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    getReports();
                    return true;
                }
                return false;
            }
        });
        btnsub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                getReports();
            }
        });
        return view;
    }

    private void getReports()
    {
//        String vehicle = spinner.getSelectedItem().toString();
        String vehicle = listVal.get(spinner.getSelectedItemPosition());

        Intent intent = new Intent(getActivity(), Reports.class);
        intent.putExtra("token", token);
        intent.putExtra("vehicle", vehicle);
        startdate = tvDisplayDate1.getText().toString();
        enddate = tvDisplayDate2.getText().toString();
        try
        {
            String start = startdate.substring(5);
            String end = enddate.substring(5);
            String yearstart = startdate.substring(0, 4);
            String yearend = enddate.substring(0, 4);
            String monthstart = start.substring(0,start.indexOf("-"));
            String monthend = end.substring(0,end.indexOf("-"));
            String datestart = start.substring(start.indexOf("-") + 1);
            String dateend = end.substring(end.indexOf("-") + 1);
            String finalmonthstart;
            if(monthstart.length()==1) {
                finalmonthstart = "0" + monthstart;
            }
            else {
                finalmonthstart = monthstart;
            }
            String finalmonthend;
            if(monthend.length()==1) {
                finalmonthend = "0" + monthend;
            }
            else {
                finalmonthend = monthend;
            }
            String finaldatestart;
            if(datestart.length()==1) {
                finaldatestart = "0" + datestart;
            }
            else {
                finaldatestart = datestart;
            }
            String finaldateend;
            if(dateend.length()==1) {
                finaldateend = "0" + dateend;
            }
            else {
                finaldateend = dateend;
            }
            startdate = yearstart+"-"+ finalmonthstart +"-"+ finaldatestart;
            enddate = yearend+"-"+ finalmonthend +"-"+ finaldateend;
            intent.putExtra("startdate", startdate);
            intent.putExtra("enddate", enddate);
            startActivity(intent);
        }
        catch (Exception e)
        {
            Toast.makeText(getActivity(), "Invalid Date Format", Toast.LENGTH_LONG).show();
        }
    }

    private void setCurrentDateOnView() {

        tvDisplayDate1 = view.findViewById(R.id.e1);
        tvDisplayDate2 = view.findViewById(R.id.e2);
        final Calendar c = Calendar.getInstance();
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day = c.get(Calendar.DAY_OF_MONTH);
        if ((month + 1) / 10 == 0) {
            if (day / 10 == 0) {
                sdate = new StringBuilder().append(year).append("-").append("0").append(month + 1).append("-").append("0").append(day);
            } else {
                sdate = new StringBuilder().append(year).append("-").append("0").append(month + 1).append("-").append(day);
            }
        } else {
            if (day / 10 == 0) {
                sdate = new StringBuilder().append(year).append("-").append(month + 1).append("-").append("0").append(day);
            } else {
                sdate = new StringBuilder().append(year).append("-").append(month + 1).append("-").append(day);
            }
        }

        tvDisplayDate1.setText(sdate);
        tvDisplayDate2.setText(sdate);

        startdate = tvDisplayDate1.getText().toString();
        enddate = tvDisplayDate2.getText().toString();

    }

    private void addListenerOnButton() {

        Button btnChangeDate1 = view.findViewById(R.id.btnChangeDate1);
        Button btnChangeDate2 = view.findViewById(R.id.btnChangeDate2);

        btnChangeDate1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                flag1 = true;
                flag2 = false;
                onCreateDialog().show();
            }


        });
        btnChangeDate2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                flag2 = true;
                flag1 = false;
                onCreateDialog().show();

            }

        });

    }

    private Dialog onCreateDialog() {
        switch (ReportsFragment.DATE_DIALOG_ID) {
            case DATE_DIALOG_ID:
                return new DatePickerDialog(getActivity(), datePickerListener,
                        year, month, day);
        }
        return null;
    }

    private final DatePickerDialog.OnDateSetListener datePickerListener
            = new DatePickerDialog.OnDateSetListener() {

        // when dialog box is closed, below method will be called.
        public void onDateSet(DatePicker view, int selectedYear,
                              int selectedMonth, int selectedDay) {
            year = selectedYear;
            month = selectedMonth;
            day = selectedDay;

            // set selected date into textview
            if (flag1) {
                if ((month + 1) / 10 == 0) {
                    if (day / 10 == 0) {
                        sdate = new StringBuilder().append(year).append("-").append("0").append(month + 1).append("-").append("0").append(day);
                    } else {
                        sdate = new StringBuilder().append(year).append("-").append("0").append(month + 1).append("-").append(day);
                    }
                } else {
                    if (day / 10 == 0) {
                        sdate = new StringBuilder().append(year).append("-").append(month + 1).append("-").append("0").append(day);
                    } else {
                        sdate = new StringBuilder().append(year).append("-").append(month + 1).append("-").append(day);
                    }
                }
                tvDisplayDate1.setText(sdate);
            }
            if (flag2) {
                if ((month + 1) / 10 == 0) {
                    if (day / 10 == 0) {
                        sdate = new StringBuilder().append(year).append("-").append("0").append(month + 1).append("-").append("0").append(day);
                    } else {
                        sdate = new StringBuilder().append(year).append("-").append("0").append(month + 1).append("-").append(day);
                    }
                } else {
                    if (day / 10 == 0) {
                        sdate = new StringBuilder().append(year).append("-").append(month + 1).append("-").append("0").append(day);
                    } else {
                        sdate = new StringBuilder().append(year).append("-").append(month + 1).append("-").append(day);
                    }
                }
                tvDisplayDate2.setText(sdate);
            }
            startdate = tvDisplayDate1.getText().toString();
            enddate = tvDisplayDate2.getText().toString();
            flag1 = false;
            flag2 = false;
        }
    };


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Reports");
    }

    public class FetchDevNo extends AsyncTask<Void, Void, Boolean> {
        private final String mToken;

        FetchDevNo(String token) {
            mToken = token;
        }

        @Override
        protected void onPreExecute() {
            spinner = view.findViewById(R.id.spinner);

        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                if (!userData.isDataFetched()) {
                    userData.fetchData();
                }
                String response = userData.getResponse().get(UserData.KEY_RESPONSE);
                JSONObject obj = new JSONObject(response);
                JSONArray arr = obj.getJSONArray("deviceDTOS");
                list.clear();
                for (int i = 0; i < arr.length(); i++) {
                    try {
                        JSONObject ob = arr.getJSONObject(i);
                        JSONObject hello = new JSONObject(ob.getString("vehicleDetailsDO"));
                        String number = hello.getString("vehicleNumber");

                        list.add(number);
                        listVal.add(ob.getString("name"));
                    }
                    catch (Exception e) {
//                        e.printStackTrace();
                        JSONObject obn = arr.getJSONObject(i);
                        String name = obn.getString("name");
                        list.add(name);
                        listVal.add(obn.getString("name"));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return true;
        }

        protected void onPostExecute(final Boolean success) {
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getActivity(), R.layout.simple_spinner_item, list);
            dataAdapter.setDropDownViewResource(R.layout.simple_spinner_item);
            spinner.setAdapter(dataAdapter);
        }
    }


}
