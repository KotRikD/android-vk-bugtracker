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
import ru.kotrik.bugtracker.VKFragments.FragmentProducts;

public class CookedDocument  extends AsyncTask<String, Void, Document> {

    private Callback mCallback;
    private String mUrl;
    private Map<String, String> cookies;
    private Context context;

    public CookedDocument(Context ctx, Callback cb, String url){
        this.mCallback = cb;
        this.mUrl = url;
        this.context = ctx;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mCallback.onPreExecute();
        this.cookies = Authdata.getCookies(context);
    }


    @Override
    protected Document doInBackground(String... strings) {
        Document doc = null;
        try {
            doc = Jsoup.connect(mUrl).cookies(cookies).ignoreContentType(true).get();
            mCallback.onBackground();
        } catch(IOException e) {
            e.printStackTrace();
            mCallback.onError(new NoInternetException());
            this.cancel(true);
        }
        return doc;
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
