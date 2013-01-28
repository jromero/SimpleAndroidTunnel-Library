package me.jromero.lib.simpleandroidtunnel;

import java.io.IOException;

import me.jromero.lib.simpleandroidtunnel.exception.AuthFailedException;
import me.jromero.lib.simpleandroidtunnel.exception.AuthMethodNotSupportedException;
import me.jromero.lib.simpleandroidtunnel.exception.NotFullyConfiguredException;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.trilead.ssh2.Connection;
import com.trilead.ssh2.ConnectionMonitor;
import com.trilead.ssh2.InteractiveCallback;
import com.trilead.ssh2.LocalPortForwarder;

public class LocalPortForwardingTask extends
        AsyncTask<LocalPortForwardingConfig, Integer, Boolean> implements
        InteractiveCallback {

    public static final String TAG = "LocalPortForwardingTask";
    private ProgressDialog mProgDialog;
    private AlertDialog mAlertDialog;
    private LocalPortForwardingListener mLocalPortForwardingListener;

    public ProgressDialog getProgressDialog() {
        return mProgDialog;
    }

    public void setProgressDialog(ProgressDialog progressDialog) {
        mProgDialog = progressDialog;
    }

    public AlertDialog getAlertDialog() {
        return mAlertDialog;
    }

    public void setAlertDialog(AlertDialog alertDialog) {
        mAlertDialog = alertDialog;
    }

    public LocalPortForwardingListener getLocalPortForwardingListener() {
        return mLocalPortForwardingListener;
    }

    public LocalPortForwardingTask setLocalPortForwardingListener(
            LocalPortForwardingListener listener) {
        mLocalPortForwardingListener = listener;

        return this;
    }

    @Override
    protected void onPreExecute() {
        if (getProgressDialog() != null) {
            getProgressDialog().show();
        }
    }

    /**
     * Store the exception thrown, if any
     */
    Exception mException;
    private LocalPortForwarder mLocalPortForwarder;
    private LocalPortForwardingConfig mPFConfig;
    private Connection mPFConnection;

    public static final int STATUS_CONNECTING = 1;
    public static final int STATUS_CONNECTED = 2;
    public static final int STATUS_AUTHENTICATING = 3;
    public static final int STATUS_AUTHENTICATED = 4;
    public static final int STATUS_ESTABLISHED = 5;

    @Override
    protected Boolean doInBackground(LocalPortForwardingConfig... params) {
        try {
            Log.d(TAG, "Port forwarding process started!");

            mPFConfig = params[0];

            // ensure our configuration is OK
            mPFConfig.ensureConfigured();

            /***
             * Start connection
             */
            String host = mPFConfig.getGatewayHost();
            String user = mPFConfig.getGatewayUser();
            int port = mPFConfig.getGatewayPort();
            String password = mPFConfig.getGatewayPassword();

            mPFConnection = new Connection(host, port);

            if (mLocalPortForwardingListener != null) {
                mPFConnection
                        .addConnectionMonitor(mLocalPortForwardingListener);
            }

            publishProgress(STATUS_CONNECTING);
            mPFConnection.connect();
            publishProgress(STATUS_CONNECTED);

            publishProgress(STATUS_AUTHENTICATING);
            if (mPFConfig.getAuthenticationType().equals(
                    LocalPortForwardingConfig.AUTH_NONE)) {

                /***
                 * None authentication
                 */

                Log.d(TAG, "Authenticate with 'none'");
                if (!mPFConnection.authenticateWithNone(user)) {
                    throw new AuthFailedException(
                            "Failed to authenticate user: " + user);
                }
            } else if (mPFConfig.getAuthenticationType().equals(
                    LocalPortForwardingConfig.AUTH_PASSWORD)) {

                /***
                 * Password authentication
                 */
                Log.d(TAG, "Authenticate with password");
                if (mPFConnection.isAuthMethodAvailable(user,
                        LocalPortForwardingConfig.AUTH_PASSWORD)) {

                    if (!mPFConnection.authenticateWithPassword(user, password)) {
                        throw new AuthFailedException(
                                "Failed to authenticate user: " + user);
                    }
                } else {
                    throw new AuthMethodNotSupportedException(
                            LocalPortForwardingConfig.AUTH_PASSWORD
                                    + " not supported!");
                }

            } else if (mPFConfig.getAuthenticationType().equals(
                    LocalPortForwardingConfig.AUTH_KEYBOARDINTERACTIVE)) {

                /***
                 * Keyboard authentication
                 */
                Log.d(TAG, "Authenticate with keyboard selected");
                if (mPFConnection.isAuthMethodAvailable(user,
                        LocalPortForwardingConfig.AUTH_KEYBOARDINTERACTIVE)) {

                    Log.d(TAG, "Authenticate with keyboard available");
                    if (!mPFConnection.authenticateWithKeyboardInteractive(
                            user, this)) {
                        throw new AuthFailedException(
                                "Failed to authenticate user: " + user);
                    }
                } else {
                    throw new AuthMethodNotSupportedException(
                            LocalPortForwardingConfig.AUTH_PASSWORD
                                    + " not supported!");
                }

            } else if (mPFConfig.getAuthenticationType().equals(
                    LocalPortForwardingConfig.AUTH_KEYBOARDINTERACTIVE)) {

                /***
                 * Keyboard authentication
                 */
                Log.d(TAG, "Authenticate with keyboard selected");
                if (mPFConnection.isAuthMethodAvailable(user,
                        LocalPortForwardingConfig.AUTH_KEYBOARDINTERACTIVE)) {

                    Log.d(TAG, "Authenticate with keyboard available");
                    if (!mPFConnection.authenticateWithKeyboardInteractive(
                            user, this)) {
                        throw new AuthFailedException(
                                "Failed to authenticate user: " + user);
                    }
                } else {
                    throw new AuthMethodNotSupportedException(
                            LocalPortForwardingConfig.AUTH_KEYBOARDINTERACTIVE
                                    + " not supported!");
                }
            }
            publishProgress(STATUS_AUTHENTICATED);

            Log.d(TAG, "Authenticated but let's check connection");
            if (mPFConnection.isAuthenticationComplete()) {
                Log.d(TAG, "Connection authenticated");

                /***
                 * Do port forwarding!
                 */
                mLocalPortForwarder = mPFConnection.createLocalPortForwarder(
                        mPFConfig.getLocalPort(), mPFConfig.getRemoteHost(),
                        mPFConfig.getRemotePort());

                Log.d(TAG, "Port forwarding established");
            } else {
                Log.d(TAG, "connection NOT authenticated");
            }

            return true;
        } catch (NotFullyConfiguredException e) {
            mException = e;
            return false;
        } catch (IOException e) {
            mException = e;
            return false;
        } catch (AuthFailedException e) {
            mException = e;
            return false;
        } catch (AuthMethodNotSupportedException e) {
            mException = e;
            return false;
        }
    }

    @Override
    protected void onProgressUpdate(Integer... status) {
        if (mLocalPortForwardingListener != null) {
            switch (status[0]) {
            case STATUS_CONNECTING:
                mLocalPortForwardingListener.onConnecting();
                break;
            case STATUS_CONNECTED:
                mLocalPortForwardingListener.onConnected(mPFConnection);
                break;
            case STATUS_AUTHENTICATING:
                mLocalPortForwardingListener.onAuthenticating();
                break;
            case STATUS_AUTHENTICATED:
                mLocalPortForwardingListener.onAuthenticated();
                break;
            default:
                break;
            }
        }
    }

    @Override
    protected void onCancelled(Boolean result) {
        if (getProgressDialog() != null) {
            getProgressDialog().dismiss();
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (getProgressDialog() != null) {
            getProgressDialog().dismiss();
        }

        if (result) {
            mLocalPortForwardingListener.onEstablished(mLocalPortForwarder);
        } else {
            mLocalPortForwardingListener.onException(mException);
        }
    }

    @Override
    public String[] replyToChallenge(String name, String instruction,
            int numPrompts, String[] prompt, boolean[] echo) throws Exception {
        String[] responses = new String[numPrompts];
        for (int i = 0; i < numPrompts; i++) {
            // request response from user for each prompt
            if (prompt[i].toLowerCase().contains("password")) {
                responses[i] = mPFConfig.getGatewayPassword();
            }
        }
        return responses;
    }

    public interface LocalPortForwardingListener extends ConnectionMonitor {

        /**
         * Called before a connection is instantiated
         */
        public void onConnecting();

        /**
         * Called after a successful SSH session has been "connected"
         */
        public void onConnected(Connection connection);

        /**
         * Called right before authentication is attempted
         */
        public void onAuthenticating();

        /**
         * Called after a successful login
         */
        public void onAuthenticated();

        /**
         * Called when a connection has been fully established (after
         * authentication)
         * 
         * @param forwarder
         */
        public void onEstablished(LocalPortForwarder forwarder);

        /**
         * Called when an error/exception is encountered
         * 
         * @param e
         */
        public void onException(Throwable e);
    }
}