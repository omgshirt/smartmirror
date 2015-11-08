package org.main.smartmirror.smartmirror;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.util.Log;
import java.util.ArrayList;

public class SpeechListener implements RecognitionListener {
    private Context mContext;
    private CountDownTimer mCountDownTimer = new CountDownTimer(10000,1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            Log.i("TIME:", "" + (millisUntilFinished/1000));
        }

        @Override
        public void onFinish() {
            if(((MainActivity)mContext).getSpeakingStatus())
                mCountDownTimer.start();
            else {
                mCountDownTimer.cancel();
                ((MainActivity) mContext).launchVoice();
            }
        }
    };



    public SpeechListener(Context ctx) {
        mContext=ctx;
    }

    @Override
    public void onReadyForSpeech(Bundle params) {
        mCountDownTimer.start();
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
        ArrayList<String> speech = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if(speech.get(0) != null) {
            ((MainActivity) mContext).speechResult(speech.get(0));
            Log.i("BLAH", "" + speech.get(0));
        }
        mCountDownTimer.start();
    }

    @Override
    public void onPartialResults(Bundle partialResults) {

    }

    @Override
    public void onEvent(int eventType, Bundle params) {

    }
}