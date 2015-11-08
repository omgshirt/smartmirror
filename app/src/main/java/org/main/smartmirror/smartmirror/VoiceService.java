package org.main.smartmirror.smartmirror;

import android.app.Service;
import android.content.Intent;
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

public class VoiceService extends Service {

    private ArrayList<Messenger> mClients = new ArrayList<>();
    private Messenger mMessenger = new Messenger( new IHandler());
    private String mSpokenCommand;
    private SpeechRecognizer mSpeechRecognizer;
    static final int STOP_SPEECH=0;
    static final int START_SPEECH=1;
    static final int RESULT_SPEECH=2;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
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
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mSpeechRecognizer.startListening(intent);
    }

    /**
     * Stops voice recognition, invoked by the calling Activity
     */
    public void stopVoice(){
        mSpeechRecognizer.stopListening();
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

    /**
     * Sends a message back to the Activity that started this service
     */
    public void sendMessage(){
        for(int i=mClients.size()-1; i>0; i--){
            try {
                Bundle bundle = new Bundle();
                bundle.putString("result", getSpokenCommand());
                Message msg = Message.obtain(null, RESULT_SPEECH);
                msg.setData(bundle);
                mClients.get(i).send(msg);
            } catch (RemoteException e) {
                mClients.remove(i);
                e.printStackTrace();
            }
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
            startVoice();
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
                Log.i("SERV: ", arlstMessage.get(0));
                setSpokenCommand(arlstMessage.get(0));
                sendMessage();
            }
        }

        @Override
        public void onPartialResults(Bundle partialResults) {

        }

        @Override
        public void onEvent(int eventType, Bundle params) {

        }
    }
}
