package org.main.smartmirror.smartmirror;

/**
 * Created by Brian on 11/3/2015.
 */

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * A simple server socket that accepts a connection
 */
public class RemoteServerAsyncTask extends AsyncTask<Void, Void, String> {
    private Context mContext;

    /**
     * @param context
     */
    public RemoteServerAsyncTask(Context context) {
        mContext = context;
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            // Create a server socket and wait for incoming message
            ServerSocket serverSocket = new ServerSocket(MainActivity.PORT);
            serverSocket.setReuseAddress(true);
            Socket client = serverSocket.accept();
            ObjectInputStream objectInputStream = new ObjectInputStream(client.getInputStream());
            String command = objectInputStream.readObject().toString();
            serverSocket.close();
            return command;
        } catch (IOException e) {
            Log.e("Wifi", "IO message " + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
    /*
     * (non-Javadoc)
     * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
     */
    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            // show the fragment
            //Log.d("Wifi", "Server: done");
            //Toast.makeText(mContext, result, Toast.LENGTH_SHORT).show();
            ((MainActivity)mContext).displayView(result);
            // now that we're done, create a new server socket
            new RemoteServerAsyncTask(mContext).execute();
        }
    }
    /*
     * (non-Javadoc)
     * @see android.os.AsyncTask#onPreExecute()
     */
    @Override
    protected void onPreExecute() {
        //Log.i("Wifi", "Server: started");
    }
}