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

    private final boolean DEBUG=true;
    private ArrayList<Messenger> mClients = new ArrayList<>();
    private Messenger mMessenger = new Messenger( new IHandler());
    private String mSpokenCommand;
    private SpeechRecognizer mSpeechRecognizer;
    private HashMap<String,Integer> captions;
    static final int STOP_SPEECH=0;
    static final int START_SPEECH=1;
    static final int RESULT_SPEECH=2;
    private String KEYPHRASE="mirror";
    private String KWS_SEARCH="wake";
    private String SMARTMIRROR_SEARCH="mirror";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSpeechRecognizer.shutdown();
    }

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
        captions = new HashMap<String, Integer>();
        captions.put(KWS_SEARCH, R.string.kws_caption);
        captions.put(SMARTMIRROR_SEARCH, R.string.smartmirror_caption);
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
//        if(mSpeechRecognizer != null)
            mSpeechRecognizer.startListening(SMARTMIRROR_SEARCH);
    }

    /**
     * Stops voice recognition, invoked by the calling Activity
     */
    public void stopVoice(){
//        if(mSpeechRecognizer != null)
            mSpeechRecognizer.stop();
    }

    /**
     * Sends a message back to the Activity that started this service
     */
    public void sendMessage(){
        Bundle bundle = new Bundle();
        bundle.putString("result", getSpokenCommand());
        Message msg = Message.obtain(null, RESULT_SPEECH);
        msg.setData(bundle);
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
        /*if (hypothesis == null)
            return;*/
    }

    /**
     * This callback is called when we stop the recognizer.
     */
    @Override
    public void onResult(Hypothesis hypothesis) {
        if(hypothesis != null) {
            Log.i("onResult", hypothesis.getHypstr());
            setSpokenCommand(hypothesis.getHypstr());
            sendMessage();
        }
    }

    @Override
    public void onBeginningOfSpeech() {

    }

    /**
     * We stop recognizer here to get a final result
     */
    @Override
    public void onEndOfSpeech() {
        stopVoice();
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
//        mSpeechRecognizer.removeListener(this);
    }

    /**
     * Method that handles the initializeation of the dictionary
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
//                    switchSearch(KWS_SEARCH);
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
                .setKeywordThreshold(1e-20f)

                        // Use context-independent phonetic search, context-dependent is too slow for mobile
                .setBoolean("-allphone_ci", true)

                .getRecognizer();
        mSpeechRecognizer.addListener(this);

        /** In your application you might not need to add all those searches.
         * They are added here for demonstration. You can leave just one.
         */

//        mSpeechRecognizer.addKeyphraseSearch(KWS_SEARCH, KEYPHRASE);

        // Create grammar-based search for selection between demos
        File smartMirrorcommandList = new File(assetsDir, "smartmirror_keys.gram");
        mSpeechRecognizer.addKeywordSearch(SMARTMIRROR_SEARCH, smartMirrorcommandList);

    }

    /**
     * Method that switches the search based on the keyphrase said
     * @param searchName the keyword that we just said
     */
    public void switchSearch(String searchName){
        stopVoice();
        if(searchName.equals(KWS_SEARCH))
            mSpeechRecognizer.startListening(searchName);
        else
            mSpeechRecognizer.startListening(searchName, 1000);

        String caption = getResources().getString(captions.get(searchName));
        Log.i("SWITCH", caption);
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
                    stopVoice();
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }
}
