package org.stratum0.stratumstatusapp;

import android.content.Context;
import android.content.Intent;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created 2016-12-29
 * Author Matthias Uschok <dev@uschok.de>
 */

public class SSHPrivateKeyDialog extends DialogPreference {

    File sshFile;
    EditText privkeyMultiline;
    Button privkeyImportFromFile;
    Button privkeyExportToFile;
    Button privkeyGenerate;

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        Context context = getContext();
        sshFile = new File(context.getFilesDir(), "ssh_priv_key");
        privkeyMultiline = (EditText) view.findViewById(R.id.pref_privkey_multiline);
        privkeyImportFromFile = (Button) view.findViewById(R.id.pref_privkey_importfromfile);
        privkeyExportToFile = (Button) view.findViewById(R.id.pref_privkey_exporttofile);
        privkeyGenerate = (Button) view.findViewById(R.id.pref_privkey_generate);

        privkeyGenerate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generatePrivateKey();
            }
        });

        updatePrivateKeyEdit();

    }

    public SSHPrivateKeyDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            Context context = getContext();

            if (!sshFile.exists()) {
                try {
                    sshFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast errorToast = new Toast(context);
                    errorToast.setText("Error saving private key...");
                    errorToast.show();
                    return;
                }
            }

            try {
                FileOutputStream sshFileOutputStream = new FileOutputStream(sshFile);
                sshFileOutputStream.write(privkeyMultiline.getText().toString().getBytes());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void generatePrivateKey() {
        JSch jsch = new JSch();
        try {
            KeyPair keyPair = KeyPair.genKeyPair(jsch, KeyPair.RSA);
            keyPair.writePrivateKey(sshFile.getPath());

            updatePrivateKeyEdit();
        } catch (JSchException e) {
            e.printStackTrace();
            return;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void importKeyFromFile() {
        Intent fileIntent = new Intent();
        fileIntent.setType("*/*");
        fileIntent.setAction(Intent.ACTION_GET_CONTENT);

            
    }

    private void updatePrivateKeyEdit() {
        try {
            BufferedReader sshFileReader = new BufferedReader(new FileReader(sshFile));
            StringBuilder sshKey = new StringBuilder();
            String line;

            while((line = sshFileReader.readLine()) != null) {
                sshKey.append(line).append("\n");
            }
            sshKey.deleteCharAt(sshKey.length()-1);

            sshFileReader.close();

            privkeyMultiline.setText(sshKey.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
