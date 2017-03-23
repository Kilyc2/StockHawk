package com.udacity.stockhawk.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.utils.CursorUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class StockWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
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

        List<ContentValues> quoteCVs = new ArrayList<>();

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
    }
}
