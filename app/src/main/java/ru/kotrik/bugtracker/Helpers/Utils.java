package ru.kotrik.bugtracker.Helpers;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.logging.ConsoleHandler;

/**
 * Created by kotoriku on 27.03.2018.
 */

public class Utils {
    public static <T> String getLastElement(final Iterator<String> elements) {

        String lastElement = elements.next();

        while(elements.hasNext()) {
            lastElement=elements.next();
        }

        return lastElement;
    }

    public static int dpAsPixel(Context ctx, int indp) {
        float scale = ctx.getResources().getDisplayMetrics().density;
        int dpAsPixels = (int) (indp*scale + 0.5f);
        return dpAsPixels;
    }

    public static CharSequence trimTrailingWhitespace(CharSequence source) {

        if(source == null)
            return "";

        int i = source.length();

        // loop back to the first non-whitespace character
        while(--i >= 0 && Character.isWhitespace(source.charAt(i))) {
        }

        return source.subSequence(0, i+1);
    }


    public static String getPath(Context context, Uri uri) throws URISyntaxException {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = { "_data" };
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
            }
        }
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }        return null;
    }


}
