package org.stratum0.stratumstatusapp;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.ByteArrayOutputStream;
import java.io.File;

/**
 * Created 2016-12-28
 * Author Matthias Uschok <dev@uschok.de>
 */

public class SSHConnectTask extends AsyncTask <Context, Void, Void> {

    @Override
    protected Void doInBackground(Context... contexts) {

        File sshFile = new File(contexts[0].getFilesDir(), "ssh_priv_key");

        JSch jsch = new JSch();
        Session sshSession = null;
        try {
            sshSession = jsch.getSession("hellfyre", "134.169.108.11");
            NullUserInfo nui = new NullUserInfo();
            sshSession.setUserInfo(nui);
        } catch (JSchException e) {
            e.printStackTrace();
        }

        try {
            jsch.addIdentity(sshFile.getPath());
        } catch (JSchException e) {
            e.printStackTrace();
        }

        Log.d("SSH", "Trying to connect...");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        sshSession.setOutputStream(baos);

        try {
            sshSession.connect();

            Log.d("SSH", "Output:");
            String output = new String(baos.toByteArray());
            Log.d("SSH", output);

            sshSession.disconnect();

        } catch (JSchException e) {
            Log.d("SSH", "Connect NOT successful");
            e.printStackTrace();
        }

        Log.d("SSH", "Connect successful \\o/");

        return null;
    }
}

