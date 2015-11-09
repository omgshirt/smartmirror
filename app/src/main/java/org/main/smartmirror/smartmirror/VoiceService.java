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
public class VoiceService extends Service implements RecognitionListener {

    private final boolean DEBUG=true;
    private ArrayList<Messenger> mClients = new ArrayList<>();
    private Messenger mMessenger = new Messenger( new IHandler());
    private String mSpokenCommand;
    private SpeechRecognizer mSpeechRecognizer;
    static final int STOP_SPEECH=0;
    static final int START_SPEECH=1;
    static final int RESULT_SPEECH=2;
    //not sure if I need these keep me
    /*
    static final int REGISTER_SERV=3;
    static final int UNREGISTER_SERV=4;
    */

    /* Named searches allow to quickly reconfigure the decoder */
    private static final String KWS_SEARCH = "wakeup";
    private static final String FORECAST_SEARCH = "forecast";
    private static final String DIGITS_SEARCH = "digits";
    private static final String PHONE_SEARCH = "phones";
    private static final String MENU_SEARCH = "menu";

    /* Keyword we are looking for to activate menu */
    private static final String KEYPHRASE = "lucy";
    private HashMap<String, Integer> mCaptions;

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

    @Override
    public void onCreate() {
        super.onCreate();
        mCaptions = new HashMap<String, Integer>();
        mCaptions.put(KWS_SEARCH, R.string.kws_caption);
        mCaptions.put(MENU_SEARCH, R.string.menu_caption);
        mCaptions.put(DIGITS_SEARCH, R.string.digits_caption);
        mCaptions.put(PHONE_SEARCH, R.string.phone_caption);
        mCaptions.put(FORECAST_SEARCH, R.string.forecast_caption);
        Toast.makeText(VoiceService.this, "Preparing the recognizer", Toast.LENGTH_SHORT).show();
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
        mSpeechRecognizer.startListening(KWS_SEARCH);
    }

    /**
     * Stops voice recognition, invoked by the calling Activity
     */
    public void stopVoice(){
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
        if (hypothesis == null)
            return;

        String text = hypothesis.getHypstr();
        if (text.equals(KEYPHRASE))
            switchSearch(MENU_SEARCH);
        else if (text.equals(DIGITS_SEARCH))
            switchSearch(DIGITS_SEARCH);
        else if (text.equals(PHONE_SEARCH))
            switchSearch(PHONE_SEARCH);
        else if (text.equals(FORECAST_SEARCH))
            switchSearch(FORECAST_SEARCH);
        else
            Toast.makeText(VoiceService.this, text, Toast.LENGTH_SHORT).show();
    }

    /**
     * This callback is called when we stop the recognizer.
     */
    @Override
    public void onResult(Hypothesis hypothesis) {
        if (hypothesis != null) {
            String text = hypothesis.getHypstr();
            makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
            if(!text.contains("Lucy".toLowerCase())) {
                setSpokenCommand(text);
                sendMessage();
            }
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
        if (!mSpeechRecognizer.getSearchName().equals(KWS_SEARCH))
            switchSearch(KWS_SEARCH);
    }

    private void switchSearch(String searchName) {
        stopVoice();

        // If we are not spotting, start listening with timeout (10000 ms or 10 seconds).
        if (searchName.equals(KWS_SEARCH))
            mSpeechRecognizer.startListening(searchName);
        else
            mSpeechRecognizer.startListening(searchName, 10000);

        String caption = getResources().getString(mCaptions.get(searchName));
        Toast.makeText(VoiceService.this, caption, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onError(Exception error) {
        Log.i("ERR", error.getMessage());
    }

    @Override
    public void onTimeout() {
        switchSearch(KWS_SEARCH);
    }

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
            } else {
                switchSearch(KWS_SEARCH);
            }
        }
    }.execute();
    }

    private void setupRecognizer(File assetsDir) throws IOException {
        // The recognizer can be configured to perform multiple searches
        // of different kind and switch between them

        mSpeechRecognizer = defaultSetup()
                .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))

                        // To disable logging of raw audio comment out this call (takes a lot of space on the device)
                .setRawLogDir(assetsDir)

                        // Threshold to tune for keyphrase to balance between false alarms and misses
                .setKeywordThreshold(1e-45f)

                        // Use context-independent phonetic search, context-dependent is too slow for mobile
                .setBoolean("-allphone_ci", true)

                .getRecognizer();
        mSpeechRecognizer.addListener(this);

        /** In your application you might not need to add all those searches.
         * They are added here for demonstration. You can leave just one.
         */

        // Create keyword-activation search.
        mSpeechRecognizer.addKeyphraseSearch(KWS_SEARCH, KEYPHRASE);

        // Create grammar-based search for selection between demos
        File menuGrammar = new File(assetsDir, "menu.gram");
        mSpeechRecognizer.addGrammarSearch(MENU_SEARCH, menuGrammar);

        // Create grammar-based search for digit recognition
        File digitsGrammar = new File(assetsDir, "digits.gram");
        mSpeechRecognizer.addGrammarSearch(DIGITS_SEARCH, digitsGrammar);

        // Create language model search
        File languageModel = new File(assetsDir, "weather.dmp");
        mSpeechRecognizer.addNgramSearch(FORECAST_SEARCH, languageModel);

        // Phonetic search
        File phoneticModel = new File(assetsDir, "en-phone.dmp");
        mSpeechRecognizer.addAllphoneSearch(PHONE_SEARCH, phoneticModel);

    }

    // Handles the messages from Main to this service
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
