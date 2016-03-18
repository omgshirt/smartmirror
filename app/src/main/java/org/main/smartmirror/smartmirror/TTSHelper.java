package org.main.smartmirror.smartmirror;

import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

@SuppressWarnings("deprecation")
public class TTSHelper {
    private MainActivity mActivity;

    private static TextToSpeech mTextToSpeech = null;
    private static boolean mIsSpeaking = false;
    private static boolean mTtsInitialized = false;
    private static TextToSpeech.OnInitListener mTextToSpeechListener;
    private int messageId = 0;


    public TTSHelper(MainActivity c) {
        mActivity = c;

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

                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mActivity.stopSpeechRecognition();
                                }
                            });
                        }

                        @Override
                        public void onDone(String utteranceId) {
                            mIsSpeaking = false;
                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mActivity.startSpeechRecognition();
                                }
                            });
                        }

                        @Override
                        public void onError(String utteranceId) {
                            mIsSpeaking = false;
                            mActivity.startSpeechRecognition(); // seems we would want to start speech if TTS encounters an error...
                        }
                    });
                    mTtsInitialized = true;
                }
            }
        };

        mTextToSpeech = new TextToSpeech(mActivity, mTextToSpeechListener);
    }


    /**
     * Initialize a TTS engine if necessary, then speak the text.
     *
     * @param text string to say
     */
    public void start(final String text) {

        if (mTtsInitialized) {
            // Map passes in the UtteranceProgressListener so we can handle callbacks from the TTS.speak event
            HashMap<String, String> map = new HashMap<>();
            map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, Integer.toString(messageId++));

            mTextToSpeech.speak(text, TextToSpeech.QUEUE_ADD, map);
            pauseSpeech(500);
        } else {
            Log.i("TextToSpeech", "not initialized");
        }
    }

    public boolean isSpeaking() {
        return mIsSpeaking;
    }

    public void stop() {
        if (mTextToSpeech != null && mTtsInitialized) {
            mTextToSpeech.stop();
            mIsSpeaking = false;
        }
    }

    /**
     * Clean up any resources and kill the TTS engine
     */
    public void destroy() {
        if (mTextToSpeech != null) {
            mTextToSpeech.stop();
            mTextToSpeech.shutdown();
            mIsSpeaking = false;
            mTextToSpeech = null;
            mTtsInitialized = false;
            mActivity = null;
        }
    }

    /**
     * Plays silence for the given duration. Adds to speech queue.
     *
     * @param duration duration in MS
     */
    public void pauseSpeech(int duration) {
        mTextToSpeech.playSilence(duration, TextToSpeech.QUEUE_ADD, null);
    }
}
