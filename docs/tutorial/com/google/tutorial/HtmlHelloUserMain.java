// com/google/tutorial/HtmlHelloUserMain.java

package com.google.tutorial;

import java.util.Locale;
import com.google.gxp.base.GxpContext;

public class HtmlHelloUserMain {
  public static void main(String[] args) throws Exception {
    GxpContext gc = new GxpContext(Locale.ENGLISH, true);
    HtmlHelloUser.write(System.out, gc, args[0]);
    System.out.println();
  }
}
