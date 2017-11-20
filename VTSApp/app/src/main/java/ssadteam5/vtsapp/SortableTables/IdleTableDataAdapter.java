package ssadteam5.vtsapp.SortableTables;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import de.codecrafters.tableview.TableDataAdapter;
import ssadteam5.vtsapp.IdleReport;

/**
 * Created by anish on 7/11/17.
 */


public class IdleTableDataAdapter extends TableDataAdapter<IdleReport.tableText> {
    private static final int TEXT_SIZE = 12;

    public IdleTableDataAdapter(final Context mycontext, final List<IdleReport.tableText> mytrip) {
        super(mycontext, mytrip);
    }

    @Override
    public View getCellView(int rowIndex, int columnIndex, ViewGroup parentView) {
        final IdleReport.tableText tableText = getRowData(rowIndex);
        if(columnIndex == 0){
            // To display row number, it will always be in increasing order from 1
            return getRowIndexTextView(rowIndex);
        }
        return getDataTextView(tableText, columnIndex);
    }

    private View getDataTextView(IdleReport.tableText tableText, int j){
        Log.d("indexisthis", j+"");
        String data = tableText.getString(j);
        Log.d("mydata", data+"");
        final TextView textView = new TextView(getContext());
        textView.setText(data);
        textView.setPadding(20, 10, 20, 10);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        textView.setTextSize(TEXT_SIZE);
        textView.setTextColor(getResources().getColor(de.codecrafters.tableview.R.color.primary_dark_material_dark));
        return textView;
    }

    private View getRowIndexTextView(int rowIndex){
        final TextView textView = new TextView(getContext());
        String data = Integer.toString(rowIndex + 1);
        textView.setText(data);
        textView.setPadding(20, 10, 20, 10);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        textView.setTextSize(TEXT_SIZE);
        textView.setTextColor(getResources().getColor(de.codecrafters.tableview.R.color.primary_dark_material_dark));
        return textView;
    }
}