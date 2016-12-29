package org.stratum0.stratumstatusapp;

import com.jcraft.jsch.UserInfo;

public class NullUserInfo implements UserInfo {

    @Override
    public String getPassphrase() {
        return "";
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public boolean promptPassword(String message) {
        return true;
    }

    @Override
    public boolean promptPassphrase(String message) {
        return true;
    }

    @Override
    public boolean promptYesNo(String message) {
        return true;
    }

    @Override
    public void showMessage(String message) {
    }
}
