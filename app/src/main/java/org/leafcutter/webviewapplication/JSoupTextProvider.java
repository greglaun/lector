package org.leafcutter.webviewapplication;

import android.net.Uri;
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
    private String title;
    private String html;

    public JSoupTextProvider(String html) {
        Document doc = Jsoup.parse(html);
        prepareDocument(doc);
    }

    public JSoupTextProvider(final Uri url) throws IOException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    doc = Jsoup.connect(url.toString()).get();
                } catch (IOException e) {
                    Log.d(TAG, "Failed to fetch article", e);
                }
                prepareDocument(doc);
            }
        }).start();
    }

    public void prepareDocument(Document document) {
        // Remove elements from the navboxes
        doc = removeUnwanted(document);
        title = retrieveTitle(doc);
        html = doc.html();
        paragraphs = doc.getElementsByTag("p");
    }

    private String retrieveTitle(Document doc) {
        return doc.title().replace(" - Wikipedia", "");
    }

    private Document removeUnwanted(Document doc) {
        // TODO: Pull these out as XML strings.
        doc.select("table.infobox").remove(); // Many types of navboxes and infoboxes
        doc.select("table.navbox-inner").remove();
        doc.select("table.wikitable").remove();
        doc.select("div.mw-normal-catlinks").remove();
        doc.select("table.vertical-navbox").remove();
        doc.select("span.IPA").remove(); // Phonetic pronunciation
        doc.select("[href*=Pronunciation_respelling_key]").remove(); // Pronunciation
        doc.select("[href*=cite]").remove(); // In-text citations
        return doc;
    }

    @Override
    public String provideOneText() {
        if (paragraphs.size() <= 0) {
            return END_OF_STREAM;
        }
        Element result = paragraphs.get(0);
        paragraphs.remove(result);
        return result.text();
    }

    @Override
    public List<String> provideText(int m) {
        int n = Math.min(m, paragraphs.size() - 1);
        ArrayList<String> result = new ArrayList<>();
        String tmp;
        synchronized (paragraphs) {
            for (int i = 0; i < n; i++) {
                tmp = popText(paragraphs);
                result.add(tmp);

            }
        }
        result.add(END_OF_STREAM);
        return result;
    }

    private String popText(Elements elementList) {
        String tmp;
        tmp = elementList.get(0).text();
        elementList.remove(0);
        return tmp;
    }

    @Override
    public boolean fastForwardTo(String place) {
        if (paragraphs.size() == 0) {
            return false;
        }
        Elements parCopy = new Elements(paragraphs);
        String test = popText(parCopy);
        while (!test.equals(place)) {
            test = popText(parCopy);
        }
        if (parCopy.size() > 0) {
            paragraphs = parCopy;
            return true;
        }
        return false;
    }

    @Override
    public List<String> provideAllTexts() {
        return provideText(paragraphs.size());
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getHtml() {
        return html;
    }

}
