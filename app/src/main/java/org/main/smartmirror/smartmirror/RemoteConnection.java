package org.main.smartmirror.smartmirror;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class RemoteConnection {

    public static final String SERVER_STARTED = "server started";
    private Handler mUpdateHandler;
    private RemoteServer mServer;
    private RemoteControlClient mRemoteControlClient;
    private MainActivity mActivity;

    private static final String TAG = "RemoteConnection";

    private Socket mSocket;
    private int mPort = -1;

    public RemoteConnection(MainActivity activity, Handler handler) {
        mActivity = activity;
        mUpdateHandler = handler;
        mServer = new RemoteServer(handler);
    }

    public void tearDown() {
        if (mRemoteControlClient != null) {
            mServer.tearDown();
            mRemoteControlClient.tearDown();
        }
    }

    public void stopServer(){
        if (mServer != null) {
            mServer.tearDown();
        }
    }

    public void stopRemoteClient(){
        if (mRemoteControlClient !=null) {
            mRemoteControlClient.tearDown();
        }
    }

    public void connectToServer(InetAddress address, int port) {
        mRemoteControlClient = new RemoteControlClient(address, port);
    }

    // Send a message to the receiver
    public void sendMessage(String msg) {
        if (mRemoteControlClient != null) {
            mRemoteControlClient.sendMessage(msg);
        }
    }

    public int getLocalPort() {
        return mPort;
    }

    public void setLocalPort(int port) {
        mPort = port;
    }

    public InetAddress getAddress() {
        return mServer.mServerSocket.getInetAddress();
    }


    public synchronized void updateMessages(String msg, boolean local) {
        Log.e(TAG, "Updating message: " + msg);

        Bundle messageBundle = new Bundle();
        messageBundle.putString("msg", msg);

        Message message = new Message();
        message.setData(messageBundle);
        mUpdateHandler.sendMessage(message);

    }

    private synchronized void setSocket(Socket socket) {
        Log.d(TAG, "setSocket :: " + socket);
        if (socket == null) {
            Log.d(TAG, "Setting a null socket.");
        }
        if (mSocket != null) {
            if (mSocket.isConnected()) {
                try {
                    Log.d(TAG, "closing socket :: " + socket);
                    mSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        mSocket = socket;
    }

    private Socket getSocket() {
        return mSocket;
    }

    private void showRemoteIconAndToast(final String toastMessage, final boolean showIcon){
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mActivity.showToast(toastMessage, Toast.LENGTH_SHORT);
                mActivity.showRemoteIcon(showIcon);
            }
        });
    }

    // Remote Server starts a server socket to listen for incoming connections.
    private class RemoteServer {
        ServerSocket mServerSocket = null;
        Thread mThread = null;

        public RemoteServer(Handler handler) {
            mThread = new Thread(new ServerThread());
            mThread.start();
        }

        public void tearDown() {
            mThread.interrupt();
            try {
                mServerSocket.close();
            } catch (IOException ioe) {
                Log.e(TAG, "Error when closing server socket.");
            }
        }

        class ServerThread implements Runnable {

            @Override
            public void run() {

                try {
                    // Since discovery will happen via Nsd, we don't need to care which port is
                    // used.  Just grab an available one  and advertise it via Nsd.
                    mServerSocket = new ServerSocket(0);
                    setLocalPort(mServerSocket.getLocalPort());
                    Log.d(TAG, "mServerSocket port :: " + mServerSocket.getLocalPort());
                    Log.d(TAG, "mServerSocket addr :: " + mServerSocket.getInetAddress());
                    // send message confirming serverSocket is set
                    updateMessages(SERVER_STARTED, true);
                    while (!Thread.currentThread().isInterrupted()) {
                        Log.d(TAG, "ServerSocket Created, awaiting connection");
                        setSocket(mServerSocket.accept());
                        Log.d(TAG, "ServerSocket Connected :: " + getSocket());
                        showRemoteIconAndToast(mActivity.getResources().getString(R.string.remote_connected), true);
                        //if (mRemoteControlClient == null) {
                            int port = mSocket.getPort();
                            InetAddress address = mSocket.getInetAddress();
                            connectToServer(address, port);
                            Log.d(TAG, "new Client :: " + mSocket.toString());
                        //}
                    }
                    Log.e(TAG, "ServerSocket closed");
                } catch (IOException e) {
                    Log.e(TAG, "Error creating ServerSocket: ", e);
                    e.printStackTrace();
                }
            }
        }
    }

    private class RemoteControlClient {

        private InetAddress mAddress;
        private int mPort;

        private final String CLIENT_TAG = "SmartRemoteClient";

        private Thread mSendThread;
        private Thread mRecThread;

        public RemoteControlClient(InetAddress address, int port) {

            Log.d(TAG, "Creating RemoteClient :: " + address.toString());
            this.mAddress = address;
            this.mPort = port;

            mSendThread = new Thread(new SendingThread());
            mSendThread.start();
        }

        class SendingThread implements Runnable {

            BlockingQueue<String> mMessageQueue;
            private int QUEUE_CAPACITY = 10;

            public SendingThread() {
                mMessageQueue = new ArrayBlockingQueue<String>(QUEUE_CAPACITY);
            }

            @Override
            public void run() {
                try {
                    if (getSocket() == null) {
                        setSocket(new Socket(mAddress, mPort));
                        Log.d(TAG, "Client-side socket initialized :: " + getSocket());
                    } else {
                        Log.d(TAG, "Socket already initialized. skipping!");
                    }

                    mRecThread = new Thread(new ReceivingThread());
                    mRecThread.start();

                } catch (UnknownHostException e) {
                    Log.d(TAG, "Initializing socket failed, UHE", e);
                } catch (IOException e) {
                    Log.d(TAG, "Initializing socket failed, IOE.", e);
                }

                while (true) {
                    try {
                        String msg = mMessageQueue.take();
                        sendMessage(msg);
                    } catch (InterruptedException ie) {
                        Log.d(TAG, "Message sending loop interrupted, exiting");
                    }
                }
            }
        }

        class ReceivingThread implements Runnable {

            @Override
            public void run() {

                BufferedReader input;
                try {
                    input = new BufferedReader(new InputStreamReader(
                            mSocket.getInputStream()));
                    while (!Thread.currentThread().isInterrupted()) {

                        String messageStr = null;
                        messageStr = input.readLine();
                        if (messageStr != null) {
                            Log.d(TAG, "Read from the stream: " + messageStr);
                            updateMessages(messageStr, false);
                        } else {
                            Log.d(TAG, "Input is null");
                            break;
                        }
                    }
                    input.close();
                    Log.i(TAG, "receive thread stopped");
                    //showRemoteIconAndToast(mActivity.getResources().getString(R.string.remote_disconnected),
                    //        false);
                } catch (SocketException se) {
                    Log.e(TAG, "Socket Exception: ", se);
                } catch (IOException e) {
                    Log.e(TAG, "Server loop error: ", e);
                    tearDown();
                }
                showRemoteIconAndToast(mActivity.getResources().getString(R.string.remote_disconnected),
                        false);
            }
        }

        public void tearDown() {
            try {
                getSocket().close();
                mSocket = null;
            } catch (IOException ioe) {
                Log.e(TAG, "Error when closing server socket.");
            }
            mRemoteControlClient = null;
        }

        public void sendMessage(String msg) {
            try {
                Socket socket = getSocket();
                if (socket == null) {
                    Log.d(TAG, "Socket is null, wtf?");
                } else if (socket.getOutputStream() == null) {
                    Log.d(TAG, "Socket output stream is null, wtf?");
                }

                PrintWriter out = new PrintWriter(
                        new BufferedWriter(
                                new OutputStreamWriter(getSocket().getOutputStream())), true);
                out.println(msg);
                out.flush();
                // leaving this in, though the mirror is not (currently) sending any messages to remote
                //updateMessages(msg, true);
            } catch (UnknownHostException e) {
                Log.d(TAG, "Unknown Host", e);
            } catch (IOException e) {
                Log.d(TAG, "I/O Exception", e);
            } catch (Exception e) {
                Log.d(TAG, "Error3", e);
            }
            Log.d(TAG, "Client sent message: " + msg);
        }
    }
}
