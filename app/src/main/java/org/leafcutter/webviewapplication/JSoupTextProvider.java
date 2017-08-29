package org.leafcutter.webviewapplication;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
            }
        }).start();
    }

    @Override
    public String provideOneText() {
        if (paragraphs.size() <= 0) {
            return null;
        }
        Element result = paragraphs.get(0);
        paragraphs.remove(result);
        return result.text();
    }

    @Override
    public List<String> provideText(int m) {
        int n = Math.min(m, paragraphs.size());
        ArrayList<String> result = new ArrayList<>();
        synchronized (paragraphs) {
            for (int i = 0; i < n; i++) {
                result.add(paragraphs.get(i).text());
                paragraphs.remove(i);
            }
        }
        return result;
    }

    @Override
    public List<String> provideAllTexts() {
        return provideText(paragraphs.size());
    }
}
