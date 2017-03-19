package org.stratum0.stratumstatusapp;

import android.content.Context;
import android.widget.Toast;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by matthias on 05.03.17.
 */

public class SSHKey {

    // Singleton instance
    private static final SSHKey instance = new SSHKey();

    public static final int RequestSSHPrivateKeyFileImport = 1;
    public static final int RequestSSHPublicKeyFileExport = 2;
    public static final int RequestPermissionWrite = 3;
    public static final int RequestPermissionRead = 4;

    Context context;
    File sshPrivateKeyFile;

    private SSHKey() {
        this.context = null;
        this.sshPrivateKeyFile = null;
    }

    public static SSHKey getInstance(Context context) {
        instance.context = context;

        if (instance.sshPrivateKeyFile == null) {
            instance.sshPrivateKeyFile = new File(instance.context.getFilesDir(), "ssh_priv_key");
        }

        return instance;
    }

    public String readPrivateKey() {
        StringBuilder sshKey = new StringBuilder();

        try {
            BufferedReader sshFileReader = new BufferedReader(new FileReader(this.sshPrivateKeyFile));
            String line;

            while((line = sshFileReader.readLine()) != null) {
                sshKey.append(line).append("\n");
            }
            if(sshKey.length() > 1) {
                sshKey.deleteCharAt(sshKey.length() - 1);
            }

            sshFileReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sshKey.toString();
    }

    public String readPublicKey() {
        JSch jsch = new JSch();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            KeyPair keyPair = KeyPair.load(jsch, sshPrivateKeyFile.getAbsolutePath());
            keyPair.writePublicKey(baos, "blubb");
        } catch (JSchException e) {
            e.printStackTrace();
        }

        return baos.toString();
    }

    public void writePrivateKey(String privateKey) {
        if (!this.sshPrivateKeyFile.exists()) {
            try {
                this.sshPrivateKeyFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this.context, "Error saving private key...", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        try {
            FileOutputStream sshFileOutputStream = new FileOutputStream(this.sshPrivateKeyFile);
            sshFileOutputStream.write(privateKey.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void generatePrivateKey() {
        JSch jsch = new JSch();
        try {
            KeyPair keyPair = KeyPair.genKeyPair(jsch, KeyPair.RSA);
            keyPair.writePrivateKey(this.sshPrivateKeyFile.getPath());

        } catch (JSchException | IOException e) {
            e.printStackTrace();
        }
    }


}
