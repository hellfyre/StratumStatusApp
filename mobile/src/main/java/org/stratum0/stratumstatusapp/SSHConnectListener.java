package org.stratum0.stratumstatusapp;

/**
 * Created 2013-09-29
 * Author Matthias Uschok <dev@uschok.de>
 */
interface SSHConnectListener {
    void onPreSSHConnectError(String error);
    void onSSHProgressUpdate(String operation);
    void onPostSSHConnect(String result);
}
