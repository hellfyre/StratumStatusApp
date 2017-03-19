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

public class SSHKeyExportPreference extends Preference {

    public SSHKeyExportPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onClick() {
        super.onClick();

        SSHKey sshKey = SSHKey.getInstance(this.getContext());
        sshKey.exportPublicKeyToFile();
    }
}
