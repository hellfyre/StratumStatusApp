package org.stratum0.stratumstatusapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import static org.stratum0.stratumstatusapp.SpaceStatus.getInstance;

public class StatusActivity extends AppCompatActivity implements SpaceStatusListener {

    SpaceStatus status = getInstance();
    TextView textStatus = (TextView) findViewById(R.id.text_status);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
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

    @Override
    public void onPreSpaceStatusUpdate(Context context) {
        //TODO: do something
    }

    @Override
    public void onPostSpaceStatusUpdate(Context context) {
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
        curStatusBuilder.append(status.getSince());
        curStatusBuilder.append(System.getProperty("line.separator"));

        curStatusBuilder.append("Last Update: ");
        curStatusBuilder.append(status.getLastUpdate());
        curStatusBuilder.append(System.getProperty("line.separator"));

        curStatusBuilder.append("Last Change: ");
        curStatusBuilder.append(status.getLastChange());

        textStatus.setText(curStatusBuilder);
    }
}
