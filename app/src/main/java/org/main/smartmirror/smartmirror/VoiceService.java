package org.main.smartmirror.smartmirror;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import java.util.ArrayList;

/**
 * Service that runs the Speech Recognition. In charge of receiving and
 * sending information (IPC)
 */
public class VoiceService extends Service {

    private final boolean DEBUG=true;
    private ArrayList<Messenger> mClients = new ArrayList<>();
    private Messenger mMessenger = new Messenger( new IHandler());
    private String mSpokenCommand;
    private SpeechRecognizer mSpeechRecognizer;
    private AudioManager mAudioManager;
    static final int STOP_SPEECH=0;
    static final int START_SPEECH=1;
    static final int RESULT_SPEECH=2;
    static final int END_OF_SPEECH=3;
    //not sure if I need these keep me
    /*
    static final int REGISTER_SERV=3;
    static final int UNREGISTER_SERV=4;
    */

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSpeechRecognizer.destroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mSpeechRecognizer.setRecognitionListener(new SpeechListener());
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
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-us");
        mSpeechRecognizer.startListening(intent);
    }

    /**
     * Stops voice recognition, invoked by the calling Activity
     */
    public void stopVoice(){
        mSpeechRecognizer.stopListening();
    }

    /**
     * Sends a message back to the Activity that started this service
     */
    public void sendMessage(int which){
        Bundle bundle = new Bundle();
        Message msg;
        switch(which) {
            case RESULT_SPEECH:
            bundle.putString("result", getSpokenCommand());
            msg = Message.obtain(null, RESULT_SPEECH);
            msg.setData(bundle);
            try {
                mClients.get(0).send(msg);
            } catch (RemoteException e) {
                mClients.remove(msg);
                e.printStackTrace();
            }
                break;
            case END_OF_SPEECH:
                bundle.putString("visualoff", getSpokenCommand());
                msg = Message.obtain(null, END_OF_SPEECH);
                msg.setData(bundle);
                try {
                    mClients.get(0).send(msg);
                } catch (RemoteException e) {
                    mClients.remove(msg);
                    e.printStackTrace();
                }
                break;

        }
    }

    /**
     * The speech listener that we'll use to capture the user's input
     */
    public class SpeechListener implements RecognitionListener{
        @Override
        public void onReadyForSpeech(Bundle params) {

        }

        @Override
        public void onBeginningOfSpeech() {

        }

        @Override
        public void onRmsChanged(float rmsdB) {

        }

        @Override
        public void onBufferReceived(byte[] buffer) {

        }

        @Override
        public void onEndOfSpeech() {

        }

        @Override
        public void onError(int error) {

        }

        /**
         * Captures the user's input and sends the message back to the activity that started
         * the service
         * @param results the results of the voice
         */
        @Override
        public void onResults(Bundle results) {
            ArrayList<String> arlstMessage = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if(arlstMessage.get(0) != null){
                if(DEBUG)
                    Log.i("LIS", arlstMessage.get(0));
                setSpokenCommand(arlstMessage.get(0));
                sendMessage(RESULT_SPEECH);
            }
        }

        @Override
        public void onPartialResults(Bundle partialResults) {

        }

        @Override
        public void onEvent(int eventType, Bundle params) {

        }
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