package org.stratum0.stratumstatusapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.EditText;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;
import com.jcraft.jsch.Session;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

/**
 * Created 2016-12-28
 * Author Matthias Uschok <dev@uschok.de>
 */

class SSHConnectTask extends AsyncTask <String, String, String> {

    private ArrayList<SSHConnectListener> receiverList = new ArrayList<>();
    private Context context;
    private SharedPreferences prefs;
    private JSch jsch;
    private boolean dialogDone;

    private byte[] privateKey;
    private byte[] privateKeyPassphrase;

    SSHConnectTask(Context context) {
        this.context = context;
        this.prefs = PreferenceManager.getDefaultSharedPreferences(this.context);
        this.privateKey = this.prefs.getString("ssh_private_key", "").getBytes();
        this.jsch = new JSch();
    }

    @Override
    protected String doInBackground(String... strings) {

        while (!this.dialogDone) {}

        String operation = "close";
        if (strings.length > 0 && !strings[0].isEmpty()) {
            operation = strings[0].toLowerCase();
        }

        publishProgress(operation);

        String user = prefs.getString("ssh_user_" + operation, "zu");
        String server = prefs.getString("ssh_server", "localhost");

        Session sshSession;
        try {
            sshSession = this.jsch.getSession(user, server);
            NullUserInfo userInfo = new NullUserInfo();
            sshSession.setUserInfo(userInfo);
        } catch (JSchException e) {
            return e.getLocalizedMessage();
        }

        try {
            if (this.privateKeyPassphrase != null) {
                Log.d(this.getClass().getName(), "Using passphrase");
            }
            else {
                Log.d(this.getClass().getName(), "Passphrase is null");
            }
            this.jsch.addIdentity("id_rsa", this.privateKey, null, this.privateKeyPassphrase);
        } catch (JSchException e) {
            return e.getLocalizedMessage();
        }

        Log.d(this.getClass().getName(), "Trying to connect...");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write("Output: ".getBytes(), 0, 8);


        Channel channel;
        try {
            sshSession.connect(10000);

            channel = sshSession.openChannel("shell");
            channel.setOutputStream(baos, true);

            channel.connect(10000);

            while(channel.isConnected()) {
                //wait
            }

            Log.d(this.getClass().getName(), baos.toString());

        } catch (JSchException e) {
            Log.d(this.getClass().getName(), "Connect NOT successful");
            return String.format("Could not connect: %s", e.getLocalizedMessage());
        }

        Log.d(this.getClass().getName(), "Connect successful");

        return baos.toString();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        this.dialogDone = false;

        try {
            KeyPair keyPair = KeyPair.load(this.jsch, this.privateKey, null);

            if (keyPair.isEncrypted()) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this.context);
                alertDialogBuilder.setView(R.layout.dialog_edittext_passphrase);
                alertDialogBuilder.setTitle(R.string.pref_privkey_dialog_title);
                alertDialogBuilder.setMessage(R.string.pref_privkey_dialog_message);
                alertDialogBuilder.setPositiveButton(this.context.getString(R.string.dialog_button_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AlertDialog alertDialog = (AlertDialog) dialog;
                        EditText passphrasefield = (EditText) alertDialog.findViewById(R.id.text_passphrase);
                        SSHConnectTask.this.privateKeyPassphrase = passphrasefield.getText().toString().getBytes();
                        SSHConnectTask.this.dialogDone = true;
                    }
                }).setNegativeButton(this.context.getString(R.string.dialog_button_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SSHConnectTask.this.dialogDone = true;
                    }
                });

                alertDialogBuilder.create().show();
            }
            else {
                this.privateKeyPassphrase = null;
                this.dialogDone = true;
            }
        } catch (JSchException e) {
            for (SSHConnectListener receiver : receiverList) {
                receiver.onPreSSHConnectError(e.getLocalizedMessage());
            }
            Log.e(this.getClass().getName(), e.getLocalizedMessage());
        }
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);

        String operation = "close";
        if (values.length > 0 && !values[0].isEmpty()) {
            operation = values[0].toLowerCase();
        }
        for (SSHConnectListener receiver : receiverList) {
            receiver.onSSHProgressUpdate(operation);
        }
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        for (SSHConnectListener receiver : receiverList) {
            receiver.onPostSSHConnect(s);
        }
    }

    void addListener(SSHConnectListener receiver) {
        receiverList.add(receiver);
    }
}

