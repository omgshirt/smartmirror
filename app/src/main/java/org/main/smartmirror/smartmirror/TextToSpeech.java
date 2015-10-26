package org.main.smartmirror.smartmirror;

import android.content.Context;
import android.speech.tts.UtteranceProgressListener;

import java.util.Locale;
import java.util.Random;

public class TextToSpeech {
    static android.speech.tts.TextToSpeech ttobj;
    Context context;
    static boolean isSpeaking = false;
    static android.speech.tts.TextToSpeech.OnInitListener ttsListner;
    static String textToSpeak;
    public TextToSpeech(Context c) {
        context = c;
        ttsListner = new android.speech.tts.TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status== android.speech.tts.TextToSpeech.SUCCESS) {
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
                    ttobj.speak(textToSpeak, android.speech.tts.TextToSpeech.QUEUE_FLUSH,null, null);

                }
            }
        };
    }
    public void start(final String text){
        textToSpeak = text;
        ttobj = new android.speech.tts.TextToSpeech(context,ttsListner);
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
        // Check preferences for speech frequency
        Random rand = new Random();
        Preferences prefs = Preferences.getInstance();
        if (rand.nextFloat() < prefs.getSpeechFrequency()) {
            start(text);
        }
    }
}
