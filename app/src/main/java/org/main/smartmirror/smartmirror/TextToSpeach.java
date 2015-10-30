package org.main.smartmirror.smartmirror;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.util.Locale;
import java.util.Random;

public class TextToSpeach {
    static TextToSpeech mTextToSpeech;
    Context mContext;
    static boolean mIsSpeaking = false;
    static TextToSpeech.OnInitListener mTextToSpeechListener;
    static String mTextToSpeak;
    public TextToSpeach(Context c) {
        mContext = c;
        mTextToSpeechListener = new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status== TextToSpeech.SUCCESS) {
                    mIsSpeaking = true;
                    mTextToSpeech.setLanguage(Locale.UK);

                    mTextToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onStart(String utteranceId) {

                        }

                        @Override
                        public void onDone(String utteranceId) {
                            mIsSpeaking = false;
                            stop();
                        }

                        @Override
                        public void onError(String utteranceId) {
                            mIsSpeaking = false;
                        }
                    });

                    //mTextToSpeech.speak(mTextToSpeak, TextToSpeech.QUEUE_FLUSH, null, null);
                    // method for backwards compatibility
                    mTextToSpeech.speak(mTextToSpeak, TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        };
    }
    public void start(final String text){
        mTextToSpeak = text;
        mTextToSpeech = new TextToSpeech(mContext, mTextToSpeechListener);
    }

    public boolean IsSpeaking() {
        return mIsSpeaking;
    }

    public void stop(){
        if(mTextToSpeech !=null){
            mTextToSpeech.stop();
            mTextToSpeech.shutdown();
            mIsSpeaking = false;
            mTextToSpeech = null;
        }
    }
    public void speakText(String text) {
        // Check preferences for speech frequency
        Random rand = new Random();
        Preferences prefs = Preferences.getInstance();
        if (rand.nextFloat() < prefs.getSpeechFrequency()) {
            start(text);
        }
    }
}
