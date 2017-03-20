package org.stratum0.stratumstatusapp;

import android.content.Context;

/**
 * Created 2013-09-29
 * Author Matthias Uschok <dev@uschok.de>
 */
public interface SSHConnectListener {
    void onPreSSHConnect(String operation);
    void onPostSSHConnect(String result);
}
