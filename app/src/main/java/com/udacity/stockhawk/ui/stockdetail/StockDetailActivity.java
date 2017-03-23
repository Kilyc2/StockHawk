package com.udacity.stockhawk.ui.stockdetail;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.DefaultAxisValueFormatter;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.StockProvider;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StockDetailActivity extends AppCompatActivity {

    public final static String STOCK_NAME = "stock";

    @BindView(R.id.chart)
    LineChart lineChart;

    private String symbol;

    private float yMax = Float.MIN_VALUE;
    private float yMin = Float.MAX_VALUE;
    private List<Long> xAxisValues = new ArrayList<>();
    private List<Entry> values = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_detail);
        ButterKnife.bind(this);
        symbol = getIntent().getStringExtra(STOCK_NAME);

        Cursor cursor = getContentResolver().query(Contract.Quote.makeUriForStock(symbol),
                null, null, null, null);
        cursor.moveToFirst();
        String history = cursor.getString(cursor.getColumnIndex(Contract.Quote.COLUMN_HISTORY));
        cursor.close();
        getData(history);
        drawGraph();
    }

    private void getData(String history) {
        int xAxisPosition = 0;

        for (String historyValue : history.split("\n")) {
            long x = Long.valueOf(historyValue.split(",")[0]);
            xAxisValues.add(x);
            float y = Float.valueOf(historyValue.split(",")[1]);
            if (y < yMin) {
                yMin = y;
            }
            if (y > yMax) {
                yMax = y;
            }
            values.add(new Entry(xAxisPosition, y));
            xAxisPosition++;
        }
    }

    private void drawGraph() {
        LineDataSet set1 = new LineDataSet(values, symbol);

        List<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);
        LineData data = new LineData(dataSets);

        lineChart.getDescription().setEnabled(false);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.enableGridDashedLine(10f, 10f, 0f);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                Date date = new Date(xAxisValues.get((int)value));
                return new SimpleDateFormat("yyyy-MM-dd").format(date).toString();
            }
        });

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.removeAllLimitLines();
        leftAxis.setAxisMaximum(yMax);
        leftAxis.setAxisMinimum(yMin - 10f);
        leftAxis.setDrawZeroLine(true);

        leftAxis.setDrawLimitLinesBehindData(true);

        lineChart.getAxisRight().setEnabled(false);

        lineChart.setData(data);

        lineChart.animateX(2500);
    }
}
