package ssadteam5.vtsapp.SortableTables;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;

import java.util.Comparator;

import de.codecrafters.tableview.SortableTableView;
import de.codecrafters.tableview.model.TableColumnDpWidthModel;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;
import de.codecrafters.tableview.toolkit.SortStateViewProviders;
import de.codecrafters.tableview.toolkit.TableDataRowBackgroundProviders;
import ssadteam5.vtsapp.IdleReport;
import ssadteam5.vtsapp.R;

/**
 * Created by anish on 7/11/17.
 */

public class SortableIdleTable extends SortableTableView<IdleReport.tableText> {
    public SortableIdleTable(final Context context){
        this(context, null);
    }

    public SortableIdleTable(final Context context, final AttributeSet attributes){
        this(context, attributes, android.R.attr.listViewStyle);
    }

    public SortableIdleTable(final Context context, final AttributeSet attributes, final int styleAttributes){
        super(context, attributes, styleAttributes);
        final SimpleTableHeaderAdapter simpleTableHeaderAdapter = new SimpleTableHeaderAdapter(context, R.string.hash, R.string.vehicle,
                R.string.startTime, R.string.endTime, R.string.location, R.string.duration);
        simpleTableHeaderAdapter.setTextColor(ContextCompat.getColor(context, R.color.table_header_text));
        simpleTableHeaderAdapter.setTextSize(15);
        simpleTableHeaderAdapter.setPaddingLeft(20);
        simpleTableHeaderAdapter.setPaddingRight(20);
        setHeaderAdapter(simpleTableHeaderAdapter);
        final int rowColorEven = ContextCompat.getColor(context, R.color.table_data_row_even);
        final int rowColorOdd = ContextCompat.getColor(context, R.color.table_data_row_odd);
        setDataRowBackgroundProvider(TableDataRowBackgroundProviders.alternatingRowColors(rowColorEven, rowColorOdd));
        setHeaderSortStateViewProvider(SortStateViewProviders.brightArrows());

        TableColumnDpWidthModel columnModel = new TableColumnDpWidthModel(context, 6, 200);
        columnModel.setColumnWidth(0, 50);
        columnModel.setColumnWidth(1, 150);
        columnModel.setColumnWidth(2, 200);
        columnModel.setColumnWidth(3, 200);
        columnModel.setColumnWidth(4, 200);
        columnModel.setColumnWidth(5, 200);
        setColumnModel(columnModel);

        setColumnComparator(1, new compareString(1));
        setColumnComparator(2, new compareString(2));
        setColumnComparator(3, new compareString(3));
        setColumnComparator(5, new compareString(5));
    }

    private static class compareString implements Comparator<IdleReport.tableText> {
        private int i;
        public compareString(int idx){i = idx;}
        @Override
        public int compare(final IdleReport.tableText t1, final IdleReport.tableText t2){
            return t1.getString(i).compareTo(t2.getString(i));
        }
    }
}

