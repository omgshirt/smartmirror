package org.main.smartmirror.smartmirror;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import java.util.Locale;

public class tts {
    static TextToSpeech ttobj;
    Context context;
    static boolean isSpeaking = false;
    static TextToSpeech.OnInitListener ttsListner;
    static String textToSpeak;
    public tts(Context c) {
        context = c;
        ttsListner = new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status== TextToSpeech.SUCCESS) {
                    isSpeaking = true;
                    ttobj.setLanguage(Locale.UK);

                    ttobj.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onStart(String utteranceId) {

                        }
                        @Override
                        public void onDone(String utteranceId) {
                            isSpeaking=false; stop();
                        }

                        @Override
                        public void onError(String utteranceId) {
                            isSpeaking = false;
                        }
                    });
                    ttobj.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH,null, null);

                }
            }
        };
    }
    public void start(final String text){
        textToSpeak = text;
        ttobj = new TextToSpeech(context,ttsListner);
    }

    public boolean isSpeaking() {
        return isSpeaking;
    }

    public void stop(){
        if(ttobj !=null){
            ttobj.stop();
            ttobj.shutdown();
            isSpeaking = false;
            ttobj = null;
        }
    }
    public void speakText(String text){
        start(text);
    }
}
