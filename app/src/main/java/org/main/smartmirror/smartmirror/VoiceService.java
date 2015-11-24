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
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import static android.widget.Toast.makeText;
import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

/**
 * Service that runs the Speech Recognition. In charge of receiving and
 * sending information (IPC)
 */
public class VoiceService extends Service implements RecognitionListener{

    private ArrayList<Messenger> mClients = new ArrayList<>();
    private Messenger mMessenger = new Messenger( new IHandler());
    private String mSpokenCommand;
    private SpeechRecognizer mSpeechRecognizer;
    private boolean mSpeechInitialized;
    static final int STOP_SPEECH=0;
    static final int START_SPEECH=1;
    static final int RESULT_SPEECH=2;
    private String SMARTMIRROR_SEARCH="mirrorSearch";
    private final String GRAMMAR_SEARCH = "grammarSearch";
    private final String MIRROR_KWS = "show";
    private final String PRIMARY_SEARCH = SMARTMIRROR_SEARCH;

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
     * Sets the spoken command
     * @param cmd the command
     */
    public void setSpokenCommand(String cmd){
        mSpokenCommand = cmd;
    }

    /**
     * Returns the spoken command
     * @return the command
     */
    public String getSpokenCommand(){
        return mSpokenCommand;
    }

    /**
     * Starts voice capture, invoked by the calling Activity
     */
    public void startVoice(){
        if(mSpeechInitialized) {
            mSpeechRecognizer.startListening(PRIMARY_SEARCH);
            //mSpeechRecognizer.startListening(SMARTMIRROR_SEARCH);
        }
    }

    public void stopVoice(){
        if (mSpeechInitialized) {
            mSpeechRecognizer.stop();
        }
    }

    /**
     * Sends a message back to the Activity that started this service
     */
    public void sendMessage(){
        // there's a potential that we might get a null String
        // so we avoid it
        if(getSpokenCommand() != null) {
            Bundle bundle = new Bundle();
            // key is result so the calling activity can handle the message
            bundle.putString("result", getSpokenCommand());
            // used for the calling activity to check which message id to check
            Message msg = Message.obtain(null, RESULT_SPEECH);
            msg.setData(bundle);
            try {
                mClients.get(0).send(msg);
            } catch (RemoteException e) {
                mClients.remove(msg);
                e.printStackTrace();
            }
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
            String text = hypothesis.getHypstr();
            hypothesis.delete();
            Log.i("VR", "onPartialResult: " + text);

            /*if (text.equals("set speech frequency")) {
                switchSearch(FREQUENCY_GRAMMAR);
            } else if (text.equals("screen brightness") ||
            text.equals("light brightness") ||
            text.equals("system volume") ||
            text.equals("music volume") ){
                switchSearch(LEVEL_SEARCH);
            }

            */
        }
    }

    /**
     * This callback is called when we stop the recognizer.
     */
    @Override
    public void onResult(Hypothesis hypothesis) {
        if(hypothesis != null) {
            Log.i("VR", "onResult:\"" + hypothesis.getHypstr() + "\"");
            //if (hypothesis.getHypstr().equals(MIRROR_KWS)) return;
            setSpokenCommand(hypothesis.getHypstr());
            sendMessage();
        }
        startVoice();
    }

    /**
     * Method that executes when we first begin speaking
     */
    @Override
    public void onBeginningOfSpeech() {
        Log.i("VR", "onBeginningOfSpeech");
    }

    /**
     * We stop recognizer here to get a final result
     */
    @Override
    public void onEndOfSpeech() {
        Log.i("VR", "onEndOfSpeech()");
        stopVoice();
        //if (!mSpeechRecognizer.getSearchName().equals(MIRROR_KWS))
         //   switchSearch(MIRROR_KWS);
    }

    /**
     * Method that handles the Error
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
        mSpeechRecognizer.stop();

        // If we are not spotting, start listening with timeout (10000 ms or 10 seconds).
        if (searchName.equals(MIRROR_KWS))
            mSpeechRecognizer.startListening(searchName);
        else
            mSpeechRecognizer.startListening(searchName, 5000);
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
                    Toast.makeText(VoiceService.this, "" + result, Toast.LENGTH_SHORT).show();
                }
                else {
                    mSpeechInitialized = true;
                }
            }
        }.execute();
    }

    /**
     * Method that sets up the recognizer
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
                .setKeywordThreshold(1e-2f)

                        // Use context-independent phonetic search, context-dependent is too slow for mobile
                .setBoolean("-allphone_ci", true)

                .getRecognizer();
        mSpeechRecognizer.addListener(this);

        /** In your application you might not need to add all those searches.
         * They are added here for demonstration. You can leave just one.
         */

        // Create grammar-based search for selection between demos
        File smartMirrorcommandList = new File(assetsDir, "smartmirror_keys.gram");
        mSpeechRecognizer.addKeywordSearch(SMARTMIRROR_SEARCH, smartMirrorcommandList);

        mSpeechRecognizer.addKeyphraseSearch(MIRROR_KWS, MIRROR_KWS);

        File smGrammarSearch = new File(assetsDir, "sm-commands.gram");
        mSpeechRecognizer.addGrammarSearch(GRAMMAR_SEARCH, smGrammarSearch);

    }

    /**
     * Class that handles the messages from the binding activity to this service
     * switches between starting and stopping voice capture
     */
    public class IHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case START_SPEECH:
                    mClients.add(msg.replyTo);
                    startVoice();
                    break;
                case STOP_SPEECH:
                    mClients.remove(msg.replyTo);
                    //stopVoice(true);
                    if(mSpeechInitialized)
                        mSpeechRecognizer.cancel();
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }
}
