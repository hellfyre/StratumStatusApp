package org.stratum0.stratumstatusapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;

/**
 * Created by matthias on 05.03.17.
 */

public class SSHPrivateKeyImportPreference extends Preference {

    public SSHPrivateKeyImportPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onClick() {
        super.onClick();

        Activity activity = (Activity) this.getContext();
        if(this.getContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            activity.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, SSHKey.RequestPermissionRead);
            return;
        }
        Intent fileIntent = new Intent();
        fileIntent.setType("*/*");
        fileIntent.setAction(Intent.ACTION_GET_CONTENT);

        Log.d("FILE", "activity is " + activity.getClass().toString());

        activity.startActivityForResult(fileIntent, SSHKey.RequestSSHPrivateKeyFileImport);
    }

}