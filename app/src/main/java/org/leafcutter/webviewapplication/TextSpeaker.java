package org.leafcutter.webviewapplication;

import android.content.Context;
import android.speech.tts.TextToSpeech;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TextSpeaker implements TextToSpeech.OnUtteranceCompletedListener {
    private static int MAX_TEXTS_IN_BUFFER = 10;

    private TextProvider provider;
    private volatile boolean speaking;
    private final CountDownLatch speechReady = new CountDownLatch(1);
    private final TextToSpeech tts;
    Queue<String> buffer = new ArrayDeque<>();
    Queue<String> mirrorQueue = new ArrayDeque<>(); // A Queue to mirror the queue state of the tts engine
    Executor speechExecutor = Executors.newSingleThreadExecutor();

    public TextSpeaker(Context context) {
        tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                speechReady.countDown();
            }
        });
    }

    private void queueForSpeaking(String text) {
        mirrorQueue.add(text);
        tts.speak(text, TextToSpeech.QUEUE_ADD, null);
    }

    public void startSpeaking(final TextProvider provider) {
        this.provider = provider;
        speaking = true;
        speechExecutor.execute(new MainSpeechLoop());
    }

    public void stopSpeaking() {
        if (speaking) {
            tts.playSilentUtterance(0, TextToSpeech.QUEUE_FLUSH, null);
            speaking = false;
            tts.stop();
        }
    }

    public TextProvider getProvider() {
        return provider;
    }

    public void setProvider(TextProvider provider) {
        this.provider = provider;
    }

    public String nextSpeechUnit() {
        String speechUnit;
        if (buffer.size() > 0) {
            speechUnit = buffer.poll();
        } else {
            speechUnit = provider.provideOneText();
        }
        return speechUnit;
    }

    @Override
    public void onUtteranceCompleted(String utteranceId) {
        mirrorQueue.poll();
        if (mirrorQueue.isEmpty()) {
            stopSpeaking();
        }
    }

    private class MainSpeechLoop implements Runnable {
        @Override
        public void run() {
            while (speaking) {
                if (buffer.isEmpty()) {
                    buffer.addAll(provider.provideText(MAX_TEXTS_IN_BUFFER));
                }
                String textToSpeak = nextSpeechUnit();
                if (textToSpeak != null) {
                    queueForSpeaking(textToSpeak);
                }
            }
        }
    }
}
