// com/google/tutorial/HtmlHelloWorldMain.java

package com.google.tutorial;

import java.util.Locale;
import com.google.gxp.base.GxpContext;

public class HtmlHelloWorldMain {
  public static void main(String[] args) throws Exception {
    GxpContext gc = new GxpContext(Locale.ENGLISH, true);
    HtmlHelloWorld.write(System.out, gc);
    System.out.println();
  }
}
