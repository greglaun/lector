package com.greglaun.lector;

import java.util.List;

public interface TextProvider {
    String END_OF_STREAM = "com.greglaun.eof";
    public static final String WIKI_BASE = "https://en.wikipedia.org/wiki/";


    String provideOneText();
    List<String> provideText(int n);

    boolean fastForwardTo(String place);

    List<String> provideAllTexts();

    String getTitle();

    String getHtml();
}
