package org.leafcutter.webviewapplication;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class JSoupTextProvider implements TextProvider {
    private static final String TAG = JSoupTextProvider.class.getSimpleName();
    volatile private Document articleDocument;
    volatile private Elements paragraphs;
    volatile private Elements texts;

    public JSoupTextProvider(final String url) throws IOException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    articleDocument = Jsoup.connect(url).get();
                } catch (IOException e) {
                    Log.d(TAG, "Failed to fetch article", e);
                }
                paragraphs = articleDocument.getElementsByTag("p");
                texts = paragraphs.nextAll();
            }
        }).start();
    }

    @Override
    public String nextSpeechUnit() {
        if (texts.size() <= 0) {
            return null;
        }
        Element result = texts.get(0);
        texts.remove(result);
        return result.text();
    }
}
