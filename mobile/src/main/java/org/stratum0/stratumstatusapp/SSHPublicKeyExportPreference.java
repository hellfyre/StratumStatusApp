package org.stratum0.stratumstatusapp;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;

/**
 * Created by matthias on 05.03.17.
 */

public class SSHPublicKeyExportPreference extends Preference {

    public SSHPublicKeyExportPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onClick() {
        super.onClick();

        SSHKey sshKey = SSHKey.getInstance(this.getContext());
        sshKey.exportPublicKeyToFile();
    }

}
