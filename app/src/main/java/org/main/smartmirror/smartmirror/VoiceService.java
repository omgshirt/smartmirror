package org.main.smartmirror.smartmirror;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.speech.RecognitionService;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

import javax.net.ssl.SSLPeerUnverifiedException;

/**
 * Created by Luis on 10/27/2015.
 */
public class VoiceService extends RecognitionService {

    private String mSpokenCommand;
    private SpeechRecognizer mSpeechRecognizer;
    private IBinder mIBinder = new LocalBinder();

    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(VoiceService.this, "Start!", Toast.LENGTH_SHORT).show();
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "eng-US");
        mSpeechRecognizer.startListening(intent);
    }

    @Override
    protected void onStartListening(Intent recognizerIntent, Callback listener) {
        String str=null;
        ArrayList data = recognizerIntent.getStringArrayListExtra(SpeechRecognizer.RESULTS_RECOGNITION);
        for(int i=0; i<data.size(); i++){
            Log.d("Listener","results: " + data.get(i));
            str+=data.get(i);
        }
        if(str != null){
            mSpokenCommand = str;
        }
        Log.d("VOICE LISTENER", str);
    }

    @Override
    protected void onStopListening(Callback listener) {
        mSpeechRecognizer.stopListening();
    }

    @Override
    protected void onCancel(Callback listener) {
        mSpeechRecognizer.cancel();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(VoiceService.this, "Stop!", Toast.LENGTH_SHORT).show();
    }

    //TODO COmmunicate with calling Activity
   /* public IBinder onBind(Intent intent){
        return mIBinder;
    }*/

    public String getSpeech(){
        return mSpokenCommand;
    }

    public class LocalBinder extends Binder {
        VoiceService getService() {
            // Return this instance of VoiceService so clients can call public methods
            return VoiceService.this;
        }
    }
}
