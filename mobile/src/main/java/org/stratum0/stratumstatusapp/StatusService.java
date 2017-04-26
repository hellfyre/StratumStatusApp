package org.stratum0.stratumstatusapp;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class StatusService extends JobService implements SpaceStatusUpdateListener {

    JobParameters params;

    public StatusService() {
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d("SERVICE.onStartJob", "enter");

        this.params = params;

        SpaceStatusUpdateTask updateTask = new SpaceStatusUpdateTask(this);
        updateTask.addListener(this);
        updateTask.execute();

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d("SERVICE.onStartJob", "enter");
        return true;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("SERVICE.onCreate", "enter");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("SERVICE.onDestroy", "enter");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("SERVICE.onStartCommand", "enter method");
        return START_NOT_STICKY;
    }

    @Override
    public void onPreSpaceStatusUpdate(Context context) {
        Log.d("SERVICE.preStatusUpd", "enter");
    }

    @Override
    public void onPostSpaceStatusUpdate(Context context) {
        Log.d("SERVICE.postStatusUpd", "enter");

        // Update widget
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName("org.stratum0.stratumstatusapp", "org.stratum0.stratumstatusapp.StatusWidget"));

        Intent widgetUpdateIntent = new Intent(context, StatusWidget.class);
        widgetUpdateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        widgetUpdateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        sendBroadcast(widgetUpdateIntent);

        jobFinished(this.params, false);
    }
}
