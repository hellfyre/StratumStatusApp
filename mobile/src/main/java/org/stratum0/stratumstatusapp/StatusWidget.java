package org.stratum0.stratumstatusapp;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 */
public class StatusWidget extends AppWidgetProvider {

    public static final String WIDGET_CLICK_ACTION = "org.stratum0.stratumstatusapp.WIDGET_CLICK_ACTION";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Integer statusLogoResourceID) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.status_widget);
        views.setImageViewResource(R.id.statuswidget_image, statusLogoResourceID);

        // Create (onClick)PendingIntent to handle widget clicks
        Intent clickIntent = new Intent(context, StatusWidget.class);
        clickIntent.setAction(StatusWidget.WIDGET_CLICK_ACTION);
        clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent pendingClickIntent = PendingIntent.getBroadcast(context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.statuswidget_image, pendingClickIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        SpaceStatus.Status status = SpaceStatus.getInstance().getStatus();
        Integer statusLogoResourceID = R.drawable.stratum0_logo;

        if (status.equals(SpaceStatus.Status.OPEN)) {
            statusLogoResourceID = R.drawable.stratum0_logo_offen;
        }
        else if (status.equals(SpaceStatus.Status.CLOSED)) {
            statusLogoResourceID = R.drawable.stratum0_logo_geschlossen;
        }

        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, statusLogoResourceID);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction().equals(StatusWidget.WIDGET_CLICK_ACTION)) {
            context.startActivity(new Intent(context, StatusActivity.class));
        }
    }

}

