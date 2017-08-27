package org.leafcutter.webviewapplication;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

public class JSoupTextProvider implements TextProvider {
    private final Document articleDocument;
    private final Elements paragraphs;

    public JSoupTextProvider(String url) throws IOException {
        articleDocument = Jsoup.connect(url).get();
        paragraphs = articleDocument.getElementsByTag("p");
    }

    @Override
    public String nextSpeechUnit() {
        return paragraphs.next().text();
    }
}
