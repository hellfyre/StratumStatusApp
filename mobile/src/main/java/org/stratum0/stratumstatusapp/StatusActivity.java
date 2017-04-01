package org.stratum0.stratumstatusapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import static org.stratum0.stratumstatusapp.SpaceStatus.getInstance;

public class StatusActivity extends Activity implements SpaceStatusUpdateListener, SpaceStatusChangeListener, SSHConnectListener {

    SpaceStatus status = getInstance();
    Button buttonOpen;
    Button buttonClose;
    TextView textStatus;
    TextView testSSH;
    ImageView imageStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        buttonOpen = (Button) findViewById(R.id.button_open);
        buttonClose = (Button) findViewById(R.id.button_close);
        textStatus = (TextView) findViewById(R.id.text_status);
        testSSH = (TextView) findViewById(R.id.text_ssh);
        imageStatus = (ImageView) findViewById(R.id.image_status);

        updateStatus();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_status, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings: {
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateStatus() {
        SpaceStatusUpdateTask updateTask = new SpaceStatusUpdateTask(this);
        updateTask.addListener(this);
        updateTask.execute();
    }

    public void changeStatus(SpaceStatus.Status status, String openBy) {
        if (openBy.isEmpty()) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            openBy = prefs.getString("name", getString(R.string.pref_name_default));
        }

        StringBuilder queryString = new StringBuilder("/update?");
        if (status == SpaceStatus.Status.OPEN) {
            queryString.append("open=true&by=");
            queryString.append(openBy);
        }
        else if (status == SpaceStatus.Status.CLOSED) {
            queryString.append("open=false");
        }

        SpaceStatusChangeTask changeTask = new SpaceStatusChangeTask(this);
        changeTask.addListener(this);
        changeTask.execute(queryString.toString());
    }

    public void buttonUpdateStatus(View view) {
        updateStatus();
    }

    public void buttonOpenClose(View view) {
        SpaceStatus.Status status = SpaceStatus.Status.UNKNOWN;

        if (view.getId() == R.id.button_open) {
            status = SpaceStatus.Status.OPEN;

            if (this.status.getStatus() == SpaceStatus.Status.OPEN) {
                AlertDialog.Builder nameDialogBuilder = new AlertDialog.Builder(this);
                nameDialogBuilder
                        .setTitle(getString(R.string.button_openas_title))
//                        .setMessage("Gimme name!!")
                        .setView(R.layout.dialog_edittext_singleline)
                        .setNegativeButton(getString(R.string.dialog_button_cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {}
                        })
                        .setPositiveButton(getString(R.string.dialog_button_ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                AlertDialog d = (AlertDialog) dialog;
                                EditText textStatusName = (EditText) d.findViewById(R.id.text_status_name);
                                changeStatus(SpaceStatus.Status.OPEN, textStatusName.getText().toString());
                            }
                        });

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                AlertDialog nameDialog = nameDialogBuilder.create();
                nameDialog.show();

                EditText textStatusName = (EditText) nameDialog.findViewById(R.id.text_status_name);
                textStatusName.setText(prefs.getString("name", getString(R.string.pref_name_default)));

                return;
            }
        }
        else if (view.getId() == R.id.button_close) {
            status = SpaceStatus.Status.CLOSED;
        }

        changeStatus(status, "");
    }

    public void sshDoorOpenClose(View view) {
        String operation = "open";
        if (view.getId() == R.id.button_ssh_close) {
            operation = "close";
        }

        SSHConnectTask sshConnectTask = new SSHConnectTask(this);
        sshConnectTask.addListener(this);
        sshConnectTask.execute(operation);
    }

    @Override
    public void onPreSpaceStatusUpdate(Context context) {
        textStatus.setText(getString(R.string.text_updating ));
    }

    @Override
    public void onPostSpaceStatusUpdate(Context context) {
        int buttonOpenStringID = R.string.button_open_title;
        switch (status.getStatus()) {
            case OPEN:
                imageStatus.setImageResource(R.drawable.stratum0_logo_offen);
                buttonOpenStringID = R.string.button_openas_title;
                buttonClose.setEnabled(true);
                break;
            case CLOSED:
                imageStatus.setImageResource(R.drawable.stratum0_logo_geschlossen);
                buttonClose.setEnabled(false);
                break;
            case UNKNOWN:
                imageStatus.setImageResource(R.drawable.stratum0_logo);
                buttonClose.setEnabled(true);
                break;
        }

        textStatus.setText(buildStatusText(status));
        buttonOpen.setText(buttonOpenStringID);

        // Update widget
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName("org.stratum0.stratumstatusapp", "org.stratum0.stratumstatusapp.StatusWidget"));

        Intent widgetUpdateIntent = new Intent(context, StatusWidget.class);
        widgetUpdateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        widgetUpdateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        widgetUpdateIntent.putExtra("source", "postSpaceStatusUpdate");
        sendBroadcast(widgetUpdateIntent);
    }

    private String buildStatusText(SpaceStatus status) {
        StringBuilder curStatusBuilder = new StringBuilder();
        curStatusBuilder.append("Status: ");

        switch (status.getStatus()) {
            case OPEN:
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                curStatusBuilder.append("Open");
                curStatusBuilder.append(System.getProperty("line.separator"));

                curStatusBuilder.append("Opened by: ");
                curStatusBuilder.append(status.getOpenedBy());
                curStatusBuilder.append(System.getProperty("line.separator"));

                curStatusBuilder.append("Since: ");
                curStatusBuilder.append(df.format(status.getSince().getTime()));
                curStatusBuilder.append(System.getProperty("line.separator"));

                curStatusBuilder.append("Last Update: ");
                curStatusBuilder.append(df.format(status.getLastUpdate().getTime()));
                curStatusBuilder.append(System.getProperty("line.separator"));

                curStatusBuilder.append("Last Change: ");
                curStatusBuilder.append(df.format(status.getLastChange().getTime()));
                break;
            case CLOSED:
                curStatusBuilder.append("Closed");
                break;
            case UNKNOWN:
                curStatusBuilder.append("Unknown");
                break;
        }

        return curStatusBuilder.toString();
    }

    @Override
    public void onPreSSHConnect(String operation) {
        switch (operation) {
            case "open":
                testSSH.setText("Opening door...");
                break;
            case "close":
                testSSH.setText("Closing door...");
                break;
        }
    }

    @Override
    public void onPostSSHConnect(String result) {
        testSSH.setText("Door says: " + result);
    }

    @Override
    public void onPostSpaceStatusChange(Context context, Integer statusCode) {
        if (statusCode == 200) {
            updateStatus();
        }
    }
}
