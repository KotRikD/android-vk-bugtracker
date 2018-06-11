package ru.kotrik.bugtracker.Helpers.Async;

import android.content.Context;
import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Map;

import ru.kotrik.bugtracker.Authdata;
import ru.kotrik.bugtracker.Helpers.Callback;
import ru.kotrik.bugtracker.Helpers.Exceptions.AccessDeniedBtException;
import ru.kotrik.bugtracker.Helpers.Exceptions.NoInternetException;

public class CookedPostDocument extends AsyncTask<String, Void, Document> {

    private Callback mCallback;
    private String mUrl;
    private Map<String, String> cookies;
    private Map<String, String> data;
    private Context context;

    public CookedPostDocument(Context ctx, Callback cb, String url, Map<String, String> data){
        this.mCallback = cb;
        this.mUrl = url;
        this.context = ctx;
        this.data = data;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mCallback.onPreExecute();
        this.cookies = Authdata.getCookies(context);
    }


    @Override
    protected org.jsoup.nodes.Document doInBackground(String... strings) {
        org.jsoup.nodes.Document doc = null;
        try {
            doc = Jsoup.connect(mUrl).ignoreContentType(true).cookies(cookies).data(data).post();
            mCallback.onBackground();
        } catch(IOException e) {
            mCallback.onError(new NoInternetException());
            this.cancel(true);
        }
        return doc;
    }


    @Override
    protected void onPostExecute(org.jsoup.nodes.Document document) {
        super.onPostExecute(document);
        if (document.select("div.message_page_body").text().length() > 0){
            mCallback.onError(new AccessDeniedBtException());
        }
        else {
            mCallback.onSuccess(document);
        }
    }
}
