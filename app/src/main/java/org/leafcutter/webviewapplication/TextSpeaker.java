package org.leafcutter.webviewapplication;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.speech.tts.TextToSpeech;

import java.util.concurrent.CountDownLatch;

public class TextSpeaker {
    private static final int OP_SPEAK_MSG = 123456;
    private static final java.lang.String MSG_TO_SPEAK = "message.to.speak";
    private TextProvider provider;
    private final Thread looperThread;
    private final Handler speechHandler;
    private boolean speaking;
    private final CountDownLatch speechReady = new CountDownLatch(1);
    private final TextToSpeech tts;

    public TextSpeaker(Context context) {
        this.looperThread = new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        Looper.loop();
                    }
                }
        );
        speechHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case OP_SPEAK_MSG:
                        speakText(msg.getData().getString(MSG_TO_SPEAK));
                        break;
                    default:
                        super.handleMessage(msg);
                }
            }
        };
        tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                speechReady.countDown();
            }
        });
    }

    private void speakText(String text) {
        tts.speak(text, TextToSpeech.QUEUE_ADD, null);
    }

    public TextProvider getProvider() {
        return provider;
    }

    public void setProvider(TextProvider provider) {
        this.provider = provider;
    }

    public void startSpeaking(final TextProvider provider) {
        this.provider = provider;
        if (!looperThread.isAlive()) {
            looperThread.start();
        }
        speaking = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message msg;
                Bundle msgBundle = new Bundle();
                String textToSpeak = provider.nextSpeechUnit();
                while (textToSpeak != null) {
                    msg = speechHandler.obtainMessage(OP_SPEAK_MSG);
                    msgBundle.putString(MSG_TO_SPEAK, textToSpeak);
                    msg.setData(msgBundle);
                    speechHandler.sendMessage(msg);
                    textToSpeak = provider.nextSpeechUnit();
                }
            }
        }).run();
    }

    public void stopSpeaking() {
        if (speaking) {
            speechHandler.removeMessages(OP_SPEAK_MSG);
        }
    }

}
