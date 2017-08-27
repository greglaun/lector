package org.leafcutter.webviewapplication;

public class TextSpeaker {
    private TextProvider provider;

    public TextSpeaker(TextProvider provider) {
        this.provider = provider;
    }

    public TextProvider getProvider() {
        return provider;
    }

    public void setProvider(TextProvider provider) {
        this.provider = provider;
    }

    public void stopSpeaking() {

    }

    public void startSpeaking() {
        
    }
}
