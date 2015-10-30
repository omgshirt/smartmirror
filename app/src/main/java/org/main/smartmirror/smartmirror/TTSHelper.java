package org.main.smartmirror.smartmirror;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

public class TTSHelper {
    static TextToSpeech mTextToSpeech;
    Context mContext;
    static boolean mIsSpeaking = false;
    static TextToSpeech.OnInitListener mTextToSpeechListener;
    static String mTextToSpeak;
    public TTSHelper(Context c) {
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
                            Log.i("UTTERANCE_PROGRESS", "onStart called");
                        }

                        @Override
                        public void onDone(String utteranceId) {
                            mIsSpeaking = false;
                            Log.i("UTTERANCE_PROGRESS", "onDone called");
                            stop();
                        }

                        @Override
                        public void onError(String utteranceId) {
                            mIsSpeaking = false;
                        }
                    });
                    // Map passes in the UtteranceProgressListener so we can handle callbacks from the TTS.speak event
                    HashMap<String, String> map = new HashMap<>();
                    map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "messageID");
                    mTextToSpeech.speak(mTextToSpeak, TextToSpeech.QUEUE_ADD, map);
                }
            }
        };
    }

    // TODO: try creating and stashing an instance of TextToSpeech to see if it improves responsiveness
    public void start(final String text){
        mTextToSpeak = text;
        try {
            mTextToSpeech = new TextToSpeech(mContext, mTextToSpeechListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isSpeaking() {
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

    /** Check preferences for speech frequency. If successful, speak text
     *
     * @param text
     */
    public void speakText(String text) {

        Random rand = new Random();
        Preferences prefs = Preferences.getInstance();
        if (rand.nextFloat() < prefs.getSpeechFrequency()) {
            start(text);
        }
    }
}
