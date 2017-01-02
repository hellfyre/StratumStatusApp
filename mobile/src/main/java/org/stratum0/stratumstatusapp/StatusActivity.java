package org.stratum0.stratumstatusapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.text.DateFormat;

import static org.stratum0.stratumstatusapp.SpaceStatus.getInstance;

public class StatusActivity extends Activity implements SpaceStatusListener {

    SpaceStatus status = getInstance();
    TextView textStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        textStatus = (TextView) findViewById(R.id.text_status);
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

    public void updateStatus(View view) {
        SpaceStatusUpdateTask updateTask = new SpaceStatusUpdateTask(this);
        updateTask.addListener(this);
        updateTask.execute();
    }

    public void sshTest(View view) {

        Context ctx = view.getContext();
        SSHConnectTask t = new SSHConnectTask();
        t.execute(ctx);


    }

    @Override
    public void onPreSpaceStatusUpdate(Context context) {
        //TODO: do something
    }

    @Override
    public void onPostSpaceStatusUpdate(Context context) {
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
        StringBuilder curStatusBuilder = new StringBuilder();
        curStatusBuilder.append("Status: ");
        switch (status.getStatus()) {
            case OPEN:
                curStatusBuilder.append("Open");
                break;
            case CLOSED:
                curStatusBuilder.append("Closed");
                break;
            case UNKNOWN:
                curStatusBuilder.append("Unknown");
                break;
        }
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

        textStatus.setText(curStatusBuilder);
    }
}
