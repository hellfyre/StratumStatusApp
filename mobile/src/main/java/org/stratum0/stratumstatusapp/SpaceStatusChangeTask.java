package org.stratum0.stratumstatusapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

//import static org.stratum0.statuswidget.GlobalVars.appWidgetIds;

/**
 * Created by Matthias Uschok <dev@uschok.de> on 2013-10-06.
 */
class SpaceStatusChangeTask extends AsyncTask <String, Void, Integer> {

    private ArrayList<SpaceStatusChangeListener> receiverList = new ArrayList<>();
    private Context context;

    SpaceStatusChangeTask(Context context) {
        this.context = context;
    }

    @Override
    protected Integer doInBackground(String... strings) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.context);
        URL statusUrl;
        int statusCode = 0;

        try {
            statusUrl = new URL(prefs.getString("api_url", "http://status.stratum0.org") + strings[0]);
            HttpURLConnection connection = (HttpURLConnection) statusUrl.openConnection();
            statusCode = connection.getResponseCode();
        } catch (MalformedURLException e) {
            Log.e(this.getClass().getName(), "Update request: malformed URL.", e);
        } catch (IOException e) {
            Log.e(this.getClass().getName(), "Update request: could not connect to server.", e);
        }

        return statusCode;
    }

    /*@Override
    protected void onPreExecute() {
        super.onPreExecute();
        for (SpaceStatusChangeListener receiver : receiverList) {
            receiver.onPreSpaceStatusChange(context);
        }
    }*/

    @Override
    protected void onPostExecute(Integer statusCode) {
        super.onPostExecute(statusCode);
        for (SpaceStatusChangeListener receiver : receiverList) {
            receiver.onPostSpaceStatusChange(context, statusCode);
        }
    }

    void addListener(SpaceStatusChangeListener receiver) {
        receiverList.add(receiver);
    }

}
