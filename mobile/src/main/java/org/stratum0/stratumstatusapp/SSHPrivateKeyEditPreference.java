package org.stratum0.stratumstatusapp;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

/**
 * Created by matthias on 05.03.17.
 */

public class SSHPrivateKeyEditPreference extends DialogPreference {

    SSHKey sshkey;
    EditText textPrivateKey;

    public SSHPrivateKeyEditPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        sshkey = SSHKey.getInstance(context);

        setDialogLayoutResource(R.layout.dialog_edittext_multiline);
        setNegativeButtonText(context.getString(R.string.dialog_button_cancel));
        setPositiveButtonText(context.getString(R.string.dialog_button_save));
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        textPrivateKey = (EditText) view.findViewById(R.id.text_private_key);
        textPrivateKey.setHint(R.string.pref_privkey_multiline_hint);
        updatePrivateKeyEdit();
    }

    private void updatePrivateKeyEdit() {
        textPrivateKey.setText(sshkey.getPrivateKey());
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            sshkey.setPrivateKey(textPrivateKey.getText().toString());
        }
    }
}
