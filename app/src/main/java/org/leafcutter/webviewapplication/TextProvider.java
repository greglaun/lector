package org.leafcutter.webviewapplication;

import java.util.List;

public interface TextProvider {
    String END_OF_STREAM = "com.leafcutter.eof";

    String provideOneText();
    List<String> provideText(int n);

    boolean fastForwardTo(String place);

    List<String> provideAllTexts();

    String getTitle();

    String getHtml();
}
