package org.stratum0.stratumstatusapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created 2013-09-30
 * Author Matthias Uschok <dev@uschok.de>
 */
public class SpaceStatusUpdateTask extends AsyncTask <Void, Void, SpaceStatus.Status> {

    private ArrayList<SpaceStatusUpdateListener> receiverList = new ArrayList<>();
    private SpaceStatus status;
    private Context context;

    public SpaceStatusUpdateTask(Context context) {
        this.context = context;
        status = SpaceStatus.getInstance();
    }

    @Override
    protected SpaceStatus.Status doInBackground(Void... voids) {

        String result = "";
        SpaceStatus.Status isOpen = SpaceStatus.Status.UNKNOWN;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.context);
        URL statusUrl;

        try {
            statusUrl = new URL(prefs.getString("api_url", "http://status.stratum0.org") + "/status.json");
            HttpURLConnection connection = (HttpURLConnection) statusUrl.openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(5000);
            if(connection.getResponseCode() == 200) {
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = br.readLine()) != null) {
                    result += line;
                }
            }
            connection.disconnect();
        } catch (MalformedURLException e) {
            Log.e(this.getClass().getName(), e.getLocalizedMessage());
            return status.getStatus();
        } catch (IOException e) {
            Log.e(this.getClass().getName(), e.getLocalizedMessage());
            return status.getStatus();
        }

        Log.d(this.getClass().getName(), "Result: " + result);

        try {
            JSONObject jsonRoot = new JSONObject(result);
            JSONObject spaceStatus = jsonRoot.getJSONObject("state");

            Calendar lastChange = GregorianCalendar.getInstance();
            lastChange.setTimeInMillis(spaceStatus.getLong("lastchange") * 1000);
            Calendar since = GregorianCalendar.getInstance();
            since.setTimeInMillis(spaceStatus.getLong("ext_since") * 1000);

            isOpen = spaceStatus.getBoolean("open") ? SpaceStatus.Status.OPEN : SpaceStatus.Status.CLOSED;

            synchronized (this) {
                status.update(isOpen, spaceStatus.getString("trigger_person"), lastChange, since);
            }

            /*
            Log.d(this.getClass().getName(), "UpdateTask: Open?  " + status.getStatus());
            Log.d(this.getClass().getName(), "UpdateTask: Opened by: " + status.getOpenedBy());
            Log.d(this.getClass().getName(), "UpdateTask: Open since: " + status.getSince());
            */

        } catch (JSONException e) {
            Log.d(this.getClass().getName(), "Error creating JSON object: " + e);
            synchronized (this) {
                Calendar now = GregorianCalendar.getInstance();
                status.update(SpaceStatus.Status.UNKNOWN, "", now, now);
            }
        }

        return status.getStatus();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        for (SpaceStatusUpdateListener receiver : receiverList) {
            receiver.onPreSpaceStatusUpdate(context);
        }
    }

    @Override
    protected void onPostExecute(SpaceStatus.Status status) {
        super.onPostExecute(status);
        for (SpaceStatusUpdateListener receiver : receiverList) {
            receiver.onPostSpaceStatusUpdate(context);
        }
    }

    public void addListener(SpaceStatusUpdateListener receiver) {
        receiverList.add(receiver);
    }

}
