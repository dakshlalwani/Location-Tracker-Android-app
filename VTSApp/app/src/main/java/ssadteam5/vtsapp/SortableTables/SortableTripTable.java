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
import ssadteam5.vtsapp.R;
import ssadteam5.vtsapp.TripReport;

/**
 * Created by anish on 3/11/17.
 */
public class SortableTripTable extends SortableTableView<TripReport.tableText> {
    public SortableTripTable(final Context context){
        this(context, null);
    }

    public SortableTripTable(final Context context, final AttributeSet attributes){
        this(context, attributes, android.R.attr.listViewStyle);
    }

    public SortableTripTable(final Context context, final AttributeSet attributes, final int styleAttributes){
        super(context, attributes, styleAttributes);
        final SimpleTableHeaderAdapter simpleTableHeaderAdapter = new SimpleTableHeaderAdapter(context, R.string.hash, R.string.registration,
                R.string.startTime, R.string.endTime, R.string.startLocation, R.string.endLocation, R.string.duration, R.string.distance,
                R.string.maxSpeed);
        simpleTableHeaderAdapter.setTextColor(ContextCompat.getColor(context, R.color.table_header_text));
        simpleTableHeaderAdapter.setTextSize(15);
        simpleTableHeaderAdapter.setPaddingLeft(20);
        simpleTableHeaderAdapter.setPaddingRight(20);
        setHeaderAdapter(simpleTableHeaderAdapter);
        final int rowColorEven = ContextCompat.getColor(context, R.color.table_data_row_even);
        final int rowColorOdd = ContextCompat.getColor(context, R.color.table_data_row_odd);
        setDataRowBackgroundProvider(TableDataRowBackgroundProviders.alternatingRowColors(rowColorEven, rowColorOdd));
        setHeaderSortStateViewProvider(SortStateViewProviders.brightArrows());

        TableColumnDpWidthModel columnModel = new TableColumnDpWidthModel(context, 9, 200);
        columnModel.setColumnWidth(0, 50);
        columnModel.setColumnWidth(1, 150);
        columnModel.setColumnWidth(2, 200);
        columnModel.setColumnWidth(3, 200);
        columnModel.setColumnWidth(4, 200);
        columnModel.setColumnWidth(5, 200);
        columnModel.setColumnWidth(6, 200);
        columnModel.setColumnWidth(7, 175);
        columnModel.setColumnWidth(8, 200);
        setColumnModel(columnModel);

        setColumnComparator(1, new compareString(1));
        setColumnComparator(2, new compareString(2));
        setColumnComparator(3, new compareString(3));
        setColumnComparator(6, new compareString(6));
        setColumnComparator(7, new compareFloat(7));
        setColumnComparator(8, new compareFloat(8));
    }

    private static class compareString implements Comparator<TripReport.tableText>{
        private int i;
        public compareString(int idx){i = idx;}
        @Override
        public int compare(final TripReport.tableText t1, final TripReport.tableText t2){
            return t1.getString(i).compareTo(t2.getString(i));
        }
    }
    private static class compareFloat implements Comparator<TripReport.tableText>{
        private int i;
        public compareFloat(int idx){i = idx;}
        @Override
        public int compare(final TripReport.tableText t1, final TripReport.tableText t2){
            Float a = Float.parseFloat(t1.getString(i));
            Float b = Float.parseFloat(t2.getString(i));
            return a < b ? -1 : a > b ? +1 : 0;
        }
    }
}
