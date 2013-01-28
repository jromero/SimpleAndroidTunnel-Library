package me.jromero.lib.simpleandroidtunnel;

import me.jromero.lib.simpleandroidtunnel.exception.NotFullyConfiguredException;

public class LocalPortForwardingConfig {

    public static final String AUTH_NONE = "none";
    public static final String AUTH_PUBLICKEY = "publickey";
    public static final String AUTH_PASSWORD = "password";
    public static final String AUTH_KEYBOARDINTERACTIVE = "keyboard-interactive";

    private String mAuthenticationType;
    private String mGatewayUser;
    private String mGatewayPassword;
    private String mGatewayHost;
    private int mGatewayPort;

    private int mLocalPort;
    private String mRemoteHost;
    private int mRemotePort;

    /**
     * None authentication constructor
     * 
     * @param gatewayUser
     * @param gatewayHost
     */
    public LocalPortForwardingConfig(String gatewayHost, int gatewayPort,
            String gatewayUser, int localPort, String remoteHost, int remotePort) {
        setGatewayHost(gatewayHost);
        setGatewayPort(gatewayPort);
        setGatewayUser(gatewayUser);
        setLocalPort(localPort);
        setRemoteHost(remoteHost);
        setRemotePort(remotePort);
        setAuthenticationType(AUTH_NONE);
    }

    /**
     * Password authentication constructor
     * 
     * @param gatewayUser
     * @param gatewayHost
     * @param gatewayPassword
     */
    public LocalPortForwardingConfig(String gatewayHost, int gatewayPort,
            String gatewayUser, String gatewayPassword, int localPort,
            String remoteHost, int remotePort) {
        setGatewayHost(gatewayHost);
        setGatewayPort(gatewayPort);
        setGatewayUser(gatewayUser);
        setGatewayPassword(gatewayPassword);
        setLocalPort(localPort);
        setRemoteHost(remoteHost);
        setRemotePort(remotePort);
        setAuthenticationType(AUTH_PASSWORD);
    }

    public void ensureConfigured() throws NotFullyConfiguredException {
        if (mAuthenticationType == null || mAuthenticationType.equals("")) {
            throw new NotFullyConfiguredException(
                    "Authentication Type is missing!");
        }

        if (mGatewayHost == null || mGatewayHost.equals("")) {
            throw new NotFullyConfiguredException("Gateway host is missing!");
        }

        if (mGatewayUser == null || mGatewayUser.equals("")) {
            throw new NotFullyConfiguredException("Gateway user is missing!");
        }

        if (mAuthenticationType.equals(AUTH_PASSWORD)) {
            if (mGatewayPassword == null || mGatewayPassword.equals("")) {
                throw new NotFullyConfiguredException(
                        "Gateway password is missing!");
            }
        }

        if (mRemoteHost == null || mRemoteHost.equals("")) {
            throw new NotFullyConfiguredException("Remote host is missing!");
        }
    }

    public String getAuthenticationType() {
        return mAuthenticationType;
    }

    public void setAuthenticationType(String authenticationType) {
        mAuthenticationType = authenticationType;
    }

    public String getGatewayUser() {
        return mGatewayUser;
    }

    public void setGatewayUser(String gatewayUser) {
        mGatewayUser = gatewayUser;
    }

    public String getGatewayPassword() {
        return mGatewayPassword;
    }

    public void setGatewayPassword(String gatewayPassword) {
        mGatewayPassword = gatewayPassword;
    }

    public String getGatewayHost() {
        return mGatewayHost;
    }

    public void setGatewayHost(String gatewayHost) {
        mGatewayHost = gatewayHost;
    }

    public int getGatewayPort() {
        return mGatewayPort;
    }

    public void setGatewayPort(int gatewayPort) {
        mGatewayPort = gatewayPort;
    }

    public int getLocalPort() {
        return mLocalPort;
    }

    public void setLocalPort(int localPort) {
        mLocalPort = localPort;
    }

    public String getRemoteHost() {
        return mRemoteHost;
    }

    public void setRemoteHost(String remoteHost) {
        mRemoteHost = remoteHost;
    }

    public int getRemotePort() {
        return mRemotePort;
    }

    public void setRemotePort(int remotePort) {
        mRemotePort = remotePort;
    }

}
