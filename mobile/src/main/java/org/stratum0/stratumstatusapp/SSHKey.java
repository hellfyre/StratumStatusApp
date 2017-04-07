package org.stratum0.stratumstatusapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStreamReader;

/**
 * Created by matthias on 05.03.17.
 */

public class SSHKey {

    // Singleton instance
    private static final SSHKey instance = new SSHKey();

    public static final int RequestSSHPrivateKeyFileImport = 1;
    public static final int RequestSSHPrivateKeyFileExport = 2;
    public static final int RequestSSHPublicKeyFileExport = 3;
    public static final int RequestPermissionWrite = 4;
    public static final int RequestPermissionRead = 5;

    Context context;
    SharedPreferences preferences;

    private SSHKey() {
        this.context = null;
        this.preferences = null;
    }

    public static SSHKey getInstance(Context context) {
        instance.context = context;

        if (instance.preferences == null) {
            instance.preferences = PreferenceManager.getDefaultSharedPreferences(instance.context);
        }

        return instance;
    }

    public String getPrivateKey() {
        return this.preferences.getString("ssh_private_key", "");
    }

    public String getPublicKey() {
        JSch jsch = new JSch();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] privateKey = preferences.getString("ssh_private_key", "").getBytes();
        String username = preferences.getString("name", "Player 1").replaceAll("\\s", "");

        String hostname = "";
        try {
            Process getprop = Runtime.getRuntime().exec("getprop net.hostname");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(getprop.getInputStream()));
            hostname = bufferedReader.readLine();
            getprop.destroy();
        } catch (java.io.IOException e) {
            Log.e("SSHKey", String.format("Error getting hostname: %s", e.toString()));
        }

        try {
            KeyPair keyPair = KeyPair.load(jsch, privateKey, null);
            keyPair.writePublicKey(outputStream, String.format("%s@%s", username, hostname));
        } catch (JSchException e) {
            e.printStackTrace();
        }

        return outputStream.toString();
    }

    public void setPrivateKey(String privateKey) {
        Log.d("SSHKey", "setPrivateKey " + privateKey);
        SharedPreferences.Editor editor = this.preferences.edit();
        editor.putString("ssh_private_key", privateKey);
        editor.commit();
    }

    public void generatePrivateKey() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        JSch jsch = new JSch();

        try {
            KeyPair keyPair = KeyPair.genKeyPair(jsch, KeyPair.RSA);
            keyPair.writePrivateKey(outputStream);

        } catch (JSchException e) {
            e.printStackTrace();
        }

        SharedPreferences.Editor editor = this.preferences.edit();
        editor.putString("ssh_private_key", outputStream.toString());
        editor.commit();
    }

    public void importPrivateKeyFromFile() {
        Activity activity = (Activity) this.context;
        if(this.context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            activity.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, SSHKey.RequestPermissionRead);
            return;
        }
        Intent fileIntent = new Intent();
        fileIntent.setType("*/*");
        fileIntent.setAction(Intent.ACTION_GET_CONTENT);

        Log.d("FILE", "activity is " + activity.getClass().toString());

        activity.startActivityForResult(fileIntent, SSHKey.RequestSSHPrivateKeyFileImport);
    }

    public void exportPrivateKeyToFile() {
        Activity activity = (Activity) this.context;
        if(this.context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            activity.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, SSHKey.RequestPermissionWrite);
            return;
        }
        Intent fileIntent = new Intent();
        fileIntent.setType("*/*");
        fileIntent.setAction(Intent.ACTION_GET_CONTENT);

        Log.d("FILE", "activity is " + activity.getClass().toString());

        activity.startActivityForResult(fileIntent, SSHKey.RequestSSHPrivateKeyFileExport);
    }

    public void exportPublicKeyToFile() {
        Activity activity = (Activity) this.context;
        if(this.context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            activity.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, SSHKey.RequestPermissionWrite);
            return;
        }
        Intent fileIntent = new Intent();
        fileIntent.setType("*/*");
        fileIntent.setAction(Intent.ACTION_GET_CONTENT);

        Log.d("FILE", "activity is " + activity.getClass().toString());

        activity.startActivityForResult(fileIntent, SSHKey.RequestSSHPublicKeyFileExport);
    }

}
