package org.main.smartmirror.smartmirror;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import java.util.Locale;
import java.util.Random;

public class TTSHelper {
    static TextToSpeech mTextToSpeech;
    Context mContext;
    static boolean mIsSpeaking = false;
    static TextToSpeech.OnInitListener mTextToSpeechListner;
    static String mTextToSpeak;
    public TTSHelper(Context c) {
        mContext = c;
        mTextToSpeechListner = new TextToSpeech.OnInitListener() {
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
                            Stop();
                        }

                        @Override
                        public void onError(String utteranceId) {
                            mIsSpeaking = false;
                        }
                    });
                    // method for backwards compatibility
                    mTextToSpeech.speak(mTextToSpeak, TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        };
    }
    public void Start(final String text){
        mTextToSpeak = text;
        mTextToSpeech = new TextToSpeech(mContext, mTextToSpeechListner);
    }

    public boolean IsSpeaking() {
        return mIsSpeaking;
    }

    public void Stop(){
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
            Start(text);
        }
    }

    public void SpeakText(String text){
        Start(text);
    }
}
