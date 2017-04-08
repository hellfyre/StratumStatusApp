package org.stratum0.stratumstatusapp;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;

/**
 * Created by matthias on 05.03.17.
 */

public class SSHPrivateKeyGeneratePreference extends DialogPreference {
    public SSHPrivateKeyGeneratePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogMessage(context.getString(R.string.text_confirm_generate_key));
        setPositiveButtonText(context.getString(R.string.dialog_button_ok));
        setNegativeButtonText(context.getString(R.string.dialog_button_cancel));
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            SSHKey sshKey = SSHKey.getInstance(this.getContext());
            sshKey.generatePrivateKey();
        }
    }
}
