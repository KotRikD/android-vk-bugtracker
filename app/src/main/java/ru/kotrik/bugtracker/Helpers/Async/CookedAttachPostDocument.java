package ru.kotrik.bugtracker.Helpers.Async;

import android.content.Context;
import android.os.AsyncTask;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Map;

import ru.kotrik.bugtracker.Authdata;
import ru.kotrik.bugtracker.Helpers.Callback;
import ru.kotrik.bugtracker.Helpers.Exceptions.AccessDeniedBtException;
import ru.kotrik.bugtracker.Helpers.Exceptions.NoInternetException;

public class CookedAttachPostDocument extends AsyncTask<String, Void, Document> {

    private Callback mCallback;
    private String mUrl;
    private Map<String, String> cookies;
    private Context context;
    public Connection doc;

    public CookedAttachPostDocument(Context ctx, Callback cb, String url){
        this.mCallback = cb;
        this.mUrl = url;
        this.context = ctx;
        this.cookies = Authdata.getCookies(context);
        this.doc = Jsoup.connect(mUrl).ignoreContentType(true).cookies(cookies);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mCallback.onPreExecute();
    }


    @Override
    protected Document doInBackground(String... strings) {
        Document doc2 = null;
        try {
            doc2 = doc.post();
            mCallback.onBackground();
        } catch(IOException e) {
            mCallback.onError(new NoInternetException());
            this.cancel(true);
        }
        return doc2;
    }


    @Override
    protected void onPostExecute(Document document) {
        super.onPostExecute(document);
        if (document.select("div.message_page_body").text().length() > 0){
            mCallback.onError(new AccessDeniedBtException());
        }
        else {
            mCallback.onSuccess(document);
        }
    }
}
