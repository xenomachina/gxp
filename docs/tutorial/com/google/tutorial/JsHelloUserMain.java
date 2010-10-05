// com/google/tutorial/JsHelloUserMain.java

package com.google.tutorial;

import java.util.Locale;
import com.google.gxp.base.GxpContext;

public class JsHelloUserMain {
  public static void main(String[] args) throws Exception {
    GxpContext gc = new GxpContext(Locale.ENGLISH, true);
    JsHelloUser.write(System.out, gc, args[0]);
    System.out.println();
  }
}
