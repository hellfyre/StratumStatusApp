package org.stratum0.stratumstatusapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;
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
import java.io.OutputStreamWriter;
import java.util.HashMap;

/**
 * Created 2016-12-29
 * Author Matthias Uschok <dev@uschok.de>
 */

public class SSHPrivateKeyDialog extends Activity {

    public static final int RequestFileIntent = 1;
    public static final int RequestPermissionWrite = 2;
    public static final int RequestPermissionRead = 3;

    File sshFile;
    EditText privkeyMultiline;
    Button privkeyImportFromFile;
    Button privkeyExportToFile;
    Button privkeyGenerate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pref_privkey);

        sshFile = new File(getFilesDir(), "ssh_priv_key");
        privkeyMultiline = (EditText) findViewById(R.id.pref_privkey_multiline);
        privkeyImportFromFile = (Button) findViewById(R.id.pref_privkey_importfromfile);
        privkeyExportToFile = (Button) findViewById(R.id.pref_privkey_exporttofile);
        privkeyGenerate = (Button) findViewById(R.id.pref_privkey_generate);

        privkeyImportFromFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                importKeyFromFile();
            }
        });

        privkeyExportToFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exportPrivateKey();
            }
        });

        privkeyGenerate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generatePrivateKey();
            }
        });

        updatePrivateKeyEdit();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (!sshFile.exists()) {
            try {
                sshFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error saving private key...", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        try {
            FileOutputStream sshFileOutputStream = new FileOutputStream(sshFile);
            sshFileOutputStream.write(privkeyMultiline.getText().toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RequestFileIntent && resultCode != 0) {
            Uri fileUri = data.getData();
            File importFile = new File(fileUri.getPath());

            Log.d("SSH", "Trying to open file " + fileUri.getPath());

            try {
                BufferedReader sshImportFileReader = new BufferedReader(new FileReader(importFile));
                StringBuilder sshImportedKey = new StringBuilder();
                String line;

                while((line = sshImportFileReader.readLine()) != null) {
                    sshImportedKey.append(line).append("\n");
                }
                sshImportedKey.deleteCharAt(sshImportedKey.length()-1);

                sshImportFileReader.close();

                Log.d("SSH", "Read file. Contents: " + sshImportedKey.toString());

                privkeyMultiline.setText(sshImportedKey.toString());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void exportPrivateKey() {
        File extStorage = Environment.getExternalStorageDirectory();
        StringBuilder extFileName = new StringBuilder(extStorage.getPath());
        extFileName.append("/s0privateSSHkey");

        try {
            File extFile = new File(extFileName.toString());
            extFile.createNewFile();

            FileOutputStream extOutStream = new FileOutputStream(extFile);
            OutputStreamWriter extFileOut = new OutputStreamWriter(extOutStream);
            extFileOut.write(privkeyMultiline.getText().toString());
            extFileOut.write("\n");
            extFileOut.close();
            extOutStream.close();

            Toast.makeText(this, "Wrote file to " + extFileName.toString(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void generatePrivateKey() {
        JSch jsch = new JSch();
        try {
            KeyPair keyPair = KeyPair.genKeyPair(jsch, KeyPair.RSA);
            keyPair.writePrivateKey(sshFile.getPath());

            updatePrivateKeyEdit();
        } catch (JSchException | IOException e) {
            e.printStackTrace();
        }
    }

    private void importKeyFromFile() {
        if(this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, RequestPermissionRead);
            return;
        }
        Intent fileIntent = new Intent();
        fileIntent.setType("*/*");
        fileIntent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(fileIntent, RequestFileIntent);
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

            privkeyMultiline.setText(sshKey.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        HashMap<String,Integer> permissionMap = new HashMap<>();
        for(int i = 0; i<permissions.length; i++) {
            permissionMap.put(permissions[i], grantResults[i]);
        }

        switch (requestCode) {
            case RequestPermissionRead:
                if(permissionMap.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    importKeyFromFile();
                }
                break;
        }
    }
}
