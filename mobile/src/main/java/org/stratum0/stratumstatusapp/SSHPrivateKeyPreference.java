package org.stratum0.stratumstatusapp;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by matthias on 05.03.17.
 */

public class SSHPrivateKeyPreference extends DialogPreference {

    File sshFile;
    EditText textPrivateKey;

    public SSHPrivateKeyPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        sshFile = new File(context.getFilesDir(), "ssh_priv_key");

        setDialogLayoutResource(R.layout.pref_ssh_private_key);
        setNegativeButtonText("Nope");
        setPositiveButtonText("Yup");
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        textPrivateKey = (EditText) view.findViewById(R.id.text_private_key);
        updatePrivateKeyEdit();
    }

    private void updatePrivateKeyEdit() {
        try {
            BufferedReader sshFileReader = new BufferedReader(new FileReader(sshFile));
            StringBuilder sshKey = new StringBuilder();
            String line;

            while((line = sshFileReader.readLine()) != null) {
                sshKey.append(line).append("\n");
            }
            if(sshKey.length() > 1) {
                sshKey.deleteCharAt(sshKey.length() - 1);
            }

            sshFileReader.close();

            textPrivateKey.setText(sshKey.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            if (!sshFile.exists()) {
                try {
                    sshFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this.getContext(), "Error saving private key...", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            try {
                FileOutputStream sshFileOutputStream = new FileOutputStream(sshFile);
                sshFileOutputStream.write(textPrivateKey.getText().toString().getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
