package org.main.smartmirror.smartmirror;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;

import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

/**
 * Service that runs the Speech Recognition. In charge of receiving and
 * sending information (IPC)
 */
public class VoiceService extends Service implements RecognitionListener {

    private final String TAG = "VR";
    private ArrayList<Messenger> mClients = new ArrayList<>();
    private Messenger mMessenger = new Messenger(new IHandler());
    private SpeechRecognizer mSpeechRecognizer;
    private boolean mSpeechInitialized;
    public static final int STOP_SPEECH = 0;
    public static final int START_SPEECH = 1;
    public static final int RESULT_SPEECH = 2;
    public static final int INIT_SPEECH = 3;
    public static final int CANCEL_SPEECH = 4;
    public static final int SHOW_ICON = 5;
    public static final int HIDE_ICON = 6;
    public static final int NORMAL_COMMAND_LIST = 7;
    public static final int MUSIC_COMMAND_LIST = 8;
    private final String NORMAL_KWS = "smartmirror_keys";
    private final String MUSIC_KWS = "music_keys";

    private HashSet<String> normalSearchSet = new HashSet<>();
    private HashSet<String> musicSearchSet = new HashSet<>();
    private HashSet<String> currentSearchSet = new HashSet<>();

    // searchMode tracks the current dictionary
    private String searchMode = NORMAL_KWS;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSpeechRecognizer != null)
            mSpeechRecognizer.shutdown();
    }

    /**
     * Method that returns a binder for the calling Activity to bind and access this service
     *
     * @param intent the current intent
     * @return the binder
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    /**
     * Set up all the things
     */
    @Override
    public void onCreate() {
        super.onCreate();
        mSpeechInitialized = false;
        initializeDictionary();
    }

    /**
     * Starts voice capture, invoked by the calling Activity
     */
    public void startVoice() {
        if (mSpeechInitialized) {
            // issue a short delay
            Log.i(TAG, "startVoice. Search mode :: " + searchMode);
            mSpeechRecognizer.startListening(searchMode);
        } else {
            Log.e(TAG, "startVoice. Speech not initialized!");
        }
    }

    /**
     * Stops voice capture
     */

    public void stopVoice() {
        if (mSpeechInitialized) {
            mSpeechRecognizer.stop();
        }
    }

    /**
     * Handles the speech results and prepares them to send them to calling activity
     *
     * @param message    the voice capture
     * @param resultType the result type
     */
    public void speechResults(String message, int resultType) {
        Bundle bundle = new Bundle();
        // key is result so the calling activity can handle the message
        bundle.putString("result", message);
        // used for the calling activity to check which message id to check
        Message msg = Message.obtain(null, resultType);
        msg.setData(bundle);
        // send the message
        sendMessage(msg);
    }

    /**
     * Sends a message back to the Activity that started this service
     */
    public void sendMessage(Message msg) {
        try {
            mClients.get(0).send(msg);
        } catch (RemoteException e) {
            mClients.remove(msg);
            e.printStackTrace();
        }
    }

    /**
     * In partial result we get quick updates about current hypothesis. In
     * keyword spotting mode we can react here, in other modes we need to wait
     * for final result in onResult.
     */
    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis != null) {
            String text = hypothesis.getHypstr().trim();
            Log.i(TAG, "onPartialResult: \"" + text + "\"");
            findCommandInText(text);
        }
    }

    /**
     * Look for recognized commands, starting with the first word returned from the VR.
     * Also checks two word pairs (n-1) + (n) from end back towards front of string
     * If a command is found, cancel voice listener and return that command.
     *
     * @param text string to evaluate
     * @return true if the string contains a recognized command
     */
    public boolean findCommandInText(String text) {

        String[] candidates = text.split("\\s+");
        for (int i = candidates.length - 1; i >= 0; i--) {
        //for(int i = 0; i < candidates.length; i++) {
            String candidate = "";

            for (int j = i; j < candidates.length; j++) {
                candidate = (candidate + " " + candidates[j]).trim();
                Log.i(TAG, "looking for \"" + candidate + "\"");

                if (currentSearchSet.contains(candidate)) {
                    Log.i(TAG, "found command: " + candidate);
                    cancelAndSendResult(candidate);
                    return true;
                }
            }
        }
        return false;
    }

    public void cancelAndSendResult(String text) {
        //Log.i(TAG, "cancelling voice. Result: \"" + text + "\"");
        mSpeechRecognizer.cancel();
        speechResults(text, RESULT_SPEECH);
        startVoice();
    }

    /**
     * This callback is called when we stop the recognizer.
     */
    @Override
    public void onResult(Hypothesis hypothesis) {
        if (hypothesis != null) {
            String hyp = hypothesis.getHypstr().trim();
            if (currentSearchSet.contains(hyp)) {
                Log.i(TAG, "onResult: \"" + hyp + "\"");
                speechResults(hyp, RESULT_SPEECH);
            }
        } else {
            Log.i(TAG, "onResult(), hypothesis null");
        }
        startVoice();
    }

    /**
     * Method that executes when we first begin speaking
     */
    @Override
    public void onBeginningOfSpeech() {
        //Log.i(TAG, "onBeginningOfSpeech");
    }

    /**
     * We stop recognizer here to get a final result
     */
    @Override
    public void onEndOfSpeech() {
        //Log.i(TAG, "onEndOfSpeech()");
        stopVoice();
    }

    /**
     * Method that handles the Error
     *
     * @param error the error
     */
    @Override
    public void onError(Exception error) {
        Log.i("ERR", error.getMessage());
    }

    @Override
    public void onTimeout() {

    }

    private void switchSearch(String searchName) {
        searchMode = searchName;
        currentSearchSet = (searchMode.equals(NORMAL_KWS)) ? normalSearchSet : musicSearchSet;
        stopVoice();
        startVoice();
    }

    /**
     * Method that handles the initialization of the dictionary
     */
    public void initializeDictionary() {
        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(VoiceService.this);
                    File assetDir = assets.syncAssets();
                    setupRecognizer(assetDir);
                } catch (IOException e) {
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception result) {
                if (result != null) {
                    Toast.makeText(VoiceService.this, result.toString(), Toast.LENGTH_SHORT).show();
                } else {
                    mSpeechInitialized = true;
                    if (mClients.size() > 0)
                        startVoice();
                }
            }
        }.execute();
    }

    /**
     * Method that sets up the recognizer
     *
     * @param assetsDir the asset directory on the device
     * @throws IOException
     */
    private void setupRecognizer(File assetsDir) throws IOException {
        // The recognizer can be configured to perform multiple searches
        // of different kind and switch between them

        mSpeechRecognizer = defaultSetup()
                .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))

                        // Threshold to tune for keyphrase to balance between false alarms and misses
                .setKeywordThreshold(1e-10f)

                        // Use context-independent phonetic search, context-dependent is too slow for mobile
                .setBoolean("-allphone_ci", true)

                .getRecognizer();
        mSpeechRecognizer.addListener(this);

        // List of phrases to match against
        File normalCommandList = new File(assetsDir, "smartmirror_keys.gram");
        mSpeechRecognizer.addKeywordSearch(NORMAL_KWS, normalCommandList);
        normalSearchSet = createCommandSet(normalCommandList);
        currentSearchSet = normalSearchSet;

        // Add music keyword list. This is the short list of commands recognized while music is streaming.
        File musicCommandList = new File(assetsDir, "music_keys.gram");
        mSpeechRecognizer.addKeywordSearch(MUSIC_KWS, musicCommandList);
        musicSearchSet = createCommandSet(musicCommandList);
    }


    /**
     * Create a list of commands from file
     * @param file input .gram file to evaluate
     * @return HashSet of commands
     */
    private HashSet<String> createCommandSet(File file) {
        HashSet<String> result = new HashSet<>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ( (line = br.readLine()) != null) {
                int index = line.indexOf('/');
                result.add(line.substring(0, index));
            }
            br.close();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }

        return result;
    }

    /**
     * Class that handles the messages from the binding activity to this service
     * switches between starting and stopping voice capture
     */
    public class IHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            Log.i(TAG, "command :: " + msg.toString());
            switch (msg.what) {
                case INIT_SPEECH:
                    mClients.add(msg.replyTo);
                    break;
                case START_SPEECH:
                    //mClients.add(msg.replyTo);
                    startVoice();
                    break;
                case CANCEL_SPEECH:
                case STOP_SPEECH:
                    // We may want to discriminate between these options. Stop should process
                    // any audio in the queue, while cancel throws out any pending results.
                    //mClients.remove(msg.replyTo);
                    if (mSpeechInitialized)
                        mSpeechRecognizer.cancel();
                    break;
                case NORMAL_COMMAND_LIST:
                    switchSearch(NORMAL_KWS);
                    break;
                case MUSIC_COMMAND_LIST:
                    switchSearch(MUSIC_KWS);
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }
}
