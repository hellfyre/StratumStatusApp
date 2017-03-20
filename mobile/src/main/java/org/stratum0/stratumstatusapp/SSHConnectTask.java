package org.stratum0.stratumstatusapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;

/**
 * Created 2016-12-28
 * Author Matthias Uschok <dev@uschok.de>
 */

public class SSHConnectTask extends AsyncTask <String, String, String> {

    private ArrayList<SSHConnectListener> receiverList = new ArrayList<>();
    private Context context;

    public SSHConnectTask(Context context) {
        this.context = context;
    }

    @Override
    protected String doInBackground(String... strings) {

        String operation = new String("close");
        if (strings.length > 0 && !strings[0].isEmpty()) {
            operation = strings[0].toLowerCase();
        }

        publishProgress(operation);

        File sshFile = new File(this.context.getFilesDir(), "ssh_priv_key");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.context);
        String user = prefs.getString("ssh_user_" + operation, "zu");
        String server = prefs.getString("ssh_server", "localhost");

        JSch jsch = new JSch();
        Session sshSession = null;
        try {
            sshSession = jsch.getSession(user, server);
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
        baos.write("Output: ".getBytes(), 0, 8);


        Channel channel = null;
        try {
            sshSession.connect(10000);

            channel = sshSession.openChannel("shell");
            channel.setOutputStream(baos, true);

            channel.connect(10000);

            while(channel.isConnected()) {
                //wait
            }

            Log.d("SSH", baos.toString());

        } catch (JSchException e) {
            Log.d("SSH", "Connect NOT successful");
            e.printStackTrace();
            return "Could not connect";
        }

        Log.d("SSH", "Connect successful");

        return baos.toString();
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);

        String operation = new String("close");
        if (values.length > 0 && !values[0].isEmpty()) {
            operation = values[0].toLowerCase();
        }
        for (SSHConnectListener receiver : receiverList) {
            receiver.onPreSSHConnect(operation);
        }
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        for (SSHConnectListener receiver : receiverList) {
            receiver.onPostSSHConnect(s);
        }
    }

    public void addListener(SSHConnectListener receiver) {
        receiverList.add(receiver);
    }
}

