package org.main.smartmirror.smartmirror;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.Nullable;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class VoiceService extends Service {

    protected static final int START=1;
    protected static final int STOP=2;
    private String mSpeechInput;
    private SpeechRecognizer mSpeechRecognizer;
    private Intent mRecognizerIntent;
    private Messenger mMessenger = new Messenger(new IHandler(this));
    private CountDownTimer mCountDown = new CountDownTimer(15000,1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            Log.i("CNT", ""+(millisUntilFinished/1000));
        }

        @Override
        public void onFinish() {
            Message msngr = Message.obtain(null, STOP);
            try {
                mMessenger.send(msngr);
                msngr = Message.obtain(null, START);
                mMessenger.send(msngr);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }
    };

    public class IHandler extends Handler {
        private WeakReference<VoiceService> mTarget;

        /**
         * Constructor for the IHandler that handles the messages passed
         * to the the Service
         * @param target
         */
        public IHandler(VoiceService target){
            mTarget = new WeakReference<VoiceService>(target);
        }

        @Override
        public void handleMessage(Message msg) {
            VoiceService target = mTarget.get();
            switch(msg.what){
                case START:
                    target.mSpeechRecognizer.startListening(target.mRecognizerIntent);
                    break;
                case STOP:
                    target.mSpeechRecognizer.stopListening();
                    break;
            }
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mSpeechRecognizer.setRecognitionListener(new SpeechListener());
        mRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL ,RecognizerIntent.EXTRA_PARTIAL_RESULTS);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mSpeechRecognizer == null)
            mSpeechRecognizer.destroy();
        mCountDown.cancel();
    }

    private void setSpeechInput(String s){
        mSpeechInput = s;
    }

    public String getSpeechInput(){
        return mSpeechInput;
    }

    public class SpeechListener implements RecognitionListener{

        @Override
        public void onReadyForSpeech(Bundle params) {
            mCountDown.start();
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

        @Override
        public void onResults(Bundle results) {
            
            Log.i("DEB", "" + results.getStringArrayList(RecognizerIntent.EXTRA_RESULTS));
        }

        @Override
        public void onPartialResults(Bundle partialResults) {

        }

        @Override
        public void onEvent(int eventType, Bundle params) {

        }
    }
}
