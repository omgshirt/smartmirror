package org.main.smartmirror.smartmirror;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

@SuppressWarnings("deprecation")
public class TTSHelper{
    private Context mContext;

    private static TextToSpeech mTextToSpeech = null;
    private static boolean mIsSpeaking = false;
    private static boolean mTtsInitialized = false;
    private static TextToSpeech.OnInitListener mTextToSpeechListener;
    private static String mTextToSpeak;
    private int messageId = 0;

    public TTSHelper(Context c) {
        mContext = c;
        mTextToSpeechListener = new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    Log.i("TTS", "TTS initialized");
                    mTextToSpeech.setLanguage(Locale.UK);
                    mTextToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onStart(String utteranceId) {
                            mIsSpeaking = true;
                            ((MainActivity)mContext).stopSpeechRecognition();
                        }

                        @Override
                        public void onDone(String utteranceId){
                            ((MainActivity)mContext).startSpeechRecognition();
                            mIsSpeaking = false;
                            //stop();  // stop() discards other phrases in queue...
                        }

                        @Override
                        public void onError(String utteranceId) {
                            ((MainActivity)mContext).stopSpeechRecognition();
                            mIsSpeaking = false;
                        }
                    });
                    mTtsInitialized = true;
                    speak();
                }
            }
        };
        mTextToSpeech = new TextToSpeech(mContext, mTextToSpeechListener);
    }

    /** Check preferences for speech frequency. If successful, say the text
     *
     * @param text string to say
     */
    public void speakText(String text) {
        Random rand = new Random();
        Preferences prefs = Preferences.getInstance();
        if (rand.nextFloat() < prefs.getSpeechFrequency()) {
            start(text);
        }
    }

    /**
     * Initialize a TTS engine if necessary, then speak the text.
     * This will bypass the Preferences setting for speech frequency.
     * @param text string to say
     */
    public void start(final String text){
        mTextToSpeak = text;
        if (!mTtsInitialized) {
            Log.i("TextToSpeech", "not initialized");
            return;
        }

        if (mTextToSpeech == null) {
            Log.i("TextToSpeech", "not initialized");
            try {
                mTextToSpeech = new TextToSpeech(mContext, mTextToSpeechListener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else
            speak();
    }

    private void speak() {
        // Map passes in the UtteranceProgressListener so we can handle callbacks from the TTS.speak event
        HashMap<String, String> map = new HashMap<>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, Integer.toString(messageId++));
        mTextToSpeech.speak(mTextToSpeak, TextToSpeech.QUEUE_ADD, map);
    }

    public boolean isSpeaking() {
        return mIsSpeaking;
    }

    public void stop(){
        if (mTextToSpeech != null && mTtsInitialized) {
            mTextToSpeech.stop();
            mIsSpeaking = false;
        }
    }

    /**
     * Clean up any resources and kill the TTS engine
     *
     */
    public void destroy() {
        if(mTextToSpeech != null){
            mTextToSpeech.stop();
            mTextToSpeech.shutdown();
            mIsSpeaking = false;
            mTextToSpeech = null;
            mTtsInitialized = false;
        }
    }

    /**
     * Plays silence for the given duration. Adds to speech queue.
     * @param duration duration in MS
     */
    public void pause(int duration) {
        mTextToSpeech.playSilence(duration, TextToSpeech.QUEUE_ADD, null);
    }
}
