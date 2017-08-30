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
    volatile private Document doc;
    volatile private Elements paragraphs;
    volatile private Elements texts;

    public JSoupTextProvider(final String url) throws IOException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    doc = Jsoup.connect(url).get();
                } catch (IOException e) {
                    Log.d(TAG, "Failed to fetch article", e);
                }
                // Remove elements from the navboxes
                doc = removeUnwanted(doc);
                paragraphs = doc.getElementsByTag("p");
            }
        }).start();
    }

    private Document removeUnwanted(Document doc) {
        // TODO: Pull these out as XML strings.
        doc.select("table.infobox").remove();
        doc.select("table.navbox-inner").remove();
        doc.select("table.wikitable").remove();
        doc.select("div.mw-normal-catlinks").remove();
        doc.select("table.vertical-navbox").remove();
        doc.select("[href*=cite]").remove(); // Remove citations
        return doc;
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
        int n = Math.min(m, paragraphs.size() - 1);
        ArrayList<String> result = new ArrayList<>();
        synchronized (paragraphs) {
            for (int i = 0; i < n; i++) {
                result.add(paragraphs.get(0).text());
                paragraphs.remove(0);
            }
        }
        return result;
    }

    @Override
    public List<String> provideAllTexts() {
        return provideText(paragraphs.size());
    }
}
