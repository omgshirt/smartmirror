package org.main.smartmirror.smartmirror;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Luis on 10/27/2015.
 */
public class VoiceService extends Service {

    private AudioManager mAudioManager;
    private String mSpokenCommand;
    private SpeechRecognizer mSpeechRecognizer;
    private RecognitionListener mRecognitionListener;
    private Context mContext;
    private boolean mFirstRun;
    private IBinder mIVoiceServiceBinder = new VoiceBinder();

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mFirstRun = true;
        toggleBeep(false);
        Toast.makeText(VoiceService.this, "Start!", Toast.LENGTH_SHORT).show();
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(mContext);
        mRecognitionListener = new SpeechRecognitionListener();
        launchSpeech();
    }

    private void toggleBeep(boolean flag){
        if(flag == true && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
            mAudioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
        }
    }

    private void launchSpeech(){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        mSpeechRecognizer.setRecognitionListener(mRecognitionListener);
        mSpeechRecognizer.startListening(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mIVoiceServiceBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSpeechRecognizer.destroy();
        Toast.makeText(VoiceService.this, "Stop!", Toast.LENGTH_SHORT).show();
    }

    public String getSpeech(){
        return mSpokenCommand;
    }

    public class VoiceBinder extends Binder{
        public VoiceService getService(){
            return VoiceService.this;
        }
    }

    public class SpeechRecognitionListener implements RecognitionListener {
        @Override
        public void onError(int error) {
            if(error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT || error == SpeechRecognizer.ERROR_NO_MATCH){
//                launchSpeech();
            }
        }

        @Override
        public void onResults(Bundle results) {
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if(matches != null)
                mSpokenCommand=matches.get(0);
            Log.d("SPOKEN_COMMAND: ", matches.get(0));
            launchSpeech();
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
        public void onPartialResults(Bundle partialResults) {

        }

        @Override
        public void onEvent(int eventType, Bundle params) {

        }

        @Override
        public void onReadyForSpeech(Bundle params) {

        }
    }
}
