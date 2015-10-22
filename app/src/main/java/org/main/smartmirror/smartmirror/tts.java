package org.main.smartmirror.smartmirror;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import java.util.Locale;

public class Tts {
    static TextToSpeech mTtObject;
    Context mContext;
    static boolean mIsSpeaking = false;
    static TextToSpeech.OnInitListener mTtsListner;
    static String mTextToSpeak;
    public Tts(Context c) {
        mContext = c;
        mTtsListner = new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status== TextToSpeech.SUCCESS) {
                    mIsSpeaking = true;
                    mTtObject.setLanguage(Locale.UK);

                    mTtObject.setOnUtteranceProgressListener(new UtteranceProgressListener() {
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
                    mTtObject.speak(mTextToSpeak, TextToSpeech.QUEUE_FLUSH, null, null);

                }
            }
        };
    }
    public void Start(final String text){
        mTextToSpeak = text;
        mTtObject = new TextToSpeech(mContext, mTtsListner);
    }

    public boolean IsSpeaking() {
        return mIsSpeaking;
    }

    public void Stop(){
        if(mTtObject !=null){
            mTtObject.stop();
            mTtObject.shutdown();
            mIsSpeaking = false;
            mTtObject = null;
        }
    }
    public void SpeakText(String text){
        Start(text);
    }
}
