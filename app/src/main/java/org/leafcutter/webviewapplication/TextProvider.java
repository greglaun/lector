package org.leafcutter.webviewapplication;

import java.util.List;

public interface TextProvider {
    String provideOneText();
    List<String> provideText(int n);
    List<String> provideAllTexts();
}
