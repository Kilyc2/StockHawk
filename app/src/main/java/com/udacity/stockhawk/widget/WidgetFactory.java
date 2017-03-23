package com.udacity.stockhawk.widget;


import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Binder;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.utils.CursorUtil;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class WidgetFactory implements RemoteViewsService.RemoteViewsFactory {

    private List<ContentValues> quoteCVs;
    private Context context;
    private final DecimalFormat dollarFormatWithPlus;
    private final DecimalFormat dollarFormat;
    private final DecimalFormat percentageFormat;

    public WidgetFactory(Context context) {
        this.context = context;
        quoteCVs = new ArrayList<>();
        dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus.setPositivePrefix("+$");
        percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
        percentageFormat.setMaximumFractionDigits(2);
        percentageFormat.setMinimumFractionDigits(2);
        percentageFormat.setPositivePrefix("+");
    }

    @Override
    public void onCreate() {
        initData();
    }

    @Override
    public void onDataSetChanged() {
        initData();
    }

    private void initData() {
        quoteCVs.clear();
        long identityToken = Binder.clearCallingIdentity();
        try {
            context.startService(new Intent(context, WidgetService.class));
            Set<String> stockPref = PrefUtils.getStocks(context);
            Set<String> stockCopy = new HashSet<>();
            stockCopy.addAll(stockPref);
            String[] stockArray = stockPref.toArray(new String[stockPref.size()]);

            if (stockArray.length == 0) {
                return;
            }

            ContentResolver contentResolver = context.getContentResolver();

            Iterator<String> iterator = stockCopy.iterator();

            while (iterator.hasNext()) {
                ContentValues quoteCV = new ContentValues();

                final String symbol = iterator.next();

                Cursor cursor = contentResolver.query(Contract.Quote.makeUriForStock(symbol),
                        null, null, null, null);

                if (CursorUtil.isValidCursor(cursor)) {
                    float price = cursor.getFloat(cursor.getColumnIndex(Contract.Quote.COLUMN_PRICE));
                    float change = cursor.getFloat(cursor.getColumnIndex(Contract.Quote.COLUMN_ABSOLUTE_CHANGE));
                    float percentChange = cursor.getFloat(cursor.getColumnIndex(Contract.Quote.COLUMN_PERCENTAGE_CHANGE));

                    quoteCV.put(Contract.Quote.COLUMN_SYMBOL, symbol);
                    quoteCV.put(Contract.Quote.COLUMN_PRICE, price);
                    quoteCV.put(Contract.Quote.COLUMN_PERCENTAGE_CHANGE, percentChange);
                    quoteCV.put(Contract.Quote.COLUMN_ABSOLUTE_CHANGE, change);

                    quoteCVs.add(quoteCV);
                    CursorUtil.closeCursor(cursor);
                }
            }
        } finally {
            Binder.restoreCallingIdentity(identityToken);
        }
    }

    @Override
    public RemoteViews getViewAt(int position) {
        ContentValues value = quoteCVs.get(position);

        RemoteViews remoteView = new RemoteViews(context.getPackageName(),
                R.layout.widget_item_quote);

        remoteView.setTextViewText(R.id.symbol, value.getAsString(Contract.Quote.COLUMN_SYMBOL));
        remoteView.setTextViewText(R.id.price, dollarFormat.format(value.getAsFloat(Contract.Quote.COLUMN_PRICE)));

        float rawAbsoluteChange = value.getAsFloat(Contract.Quote.COLUMN_ABSOLUTE_CHANGE);
        float percentageChange = value.getAsFloat(Contract.Quote.COLUMN_PERCENTAGE_CHANGE);


        if (rawAbsoluteChange > 0) {
            remoteView.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_green);
        } else {
            remoteView.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_red);
        }

        String percentage = percentageFormat.format(percentageChange / 100);

        remoteView.setTextViewText(R.id.change, percentage);
        return remoteView;
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return quoteCVs.size();
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
