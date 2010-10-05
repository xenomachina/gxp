// com/google/tutorial/HelloWorldMain.java

package com.google.tutorial;

import java.util.Locale;
import com.google.gxp.base.GxpContext;

public class HelloWorldMain {
  public static void main(String[] args) throws Exception {
    GxpContext gc = new GxpContext(Locale.ENGLISH);
    HelloWorld.write(System.out, gc);
    System.out.println();
  }
}
