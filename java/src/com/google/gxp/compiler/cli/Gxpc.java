/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.gxp.compiler.cli;

import com.google.gxp.compiler.Compiler;
import com.google.gxp.compiler.InvalidConfigException;
import com.google.gxp.compiler.alerts.AlertCounter;
import com.google.gxp.compiler.alerts.AlertSink;
import com.google.gxp.compiler.alerts.PrintingAlertSink;
import com.google.gxp.compiler.fs.FileRef;
import com.google.gxp.compiler.fs.FileSystem;
import com.google.gxp.compiler.fs.SystemFileSystem;

import java.io.IOException;

import org.kohsuke.args4j.CmdLineException;

/**
 * The GXP compiler, "gxpc". The command line interface for generating code and
 * XMB files from GXP files.
 */
public class Gxpc {
  /**
   * Runs compiler based on command line arguments. This method is invoked by
   * the JVM when this class is run from the command line.
   */
  public static void main(String[] args) throws IOException {
    FileSystem sysFs = SystemFileSystem.INSTANCE;
    FileRef cwd = sysFs.parseFilename(System.getProperty("user.dir"));
    int result = main(sysFs, System.err, cwd, args);
    System.exit(result);
  }

  /**
   * (Testable) helper method for running compiler based on command line
   * arguments. Untestable bits (like references to the {@code System} class)
   * should be placed in {@link #main(String[])} and passed to this method as
   * parameters.
   *
   * @param fs the {@code FileSystem} to use for input and output.
   * @param stderr where to print error messages.
   * @param cwd the current working directory
   * @param args command-line arguments.
   */
  static int main(FileSystem fs, final Appendable stderr, FileRef cwd, String... args)
      throws IOException {
    try {
      GxpcFlags config = new GxpcFlags(fs, cwd, args);
      if (config.showHelp()) {
        config.printHelp(stderr);
        return 0;
      }

      if (config.getSourceFiles().isEmpty()) {
        stderr.append("gxpc: no input files\n");
        return 1;
      }
      AlertSink alertSink =  new PrintingAlertSink(config.getAlertPolicy(),
                                                   config.isVerboseEnabled(),
                                                   stderr);
      AlertCounter counter = new AlertCounter(alertSink, config.getAlertPolicy());
      new Compiler(config).call(counter);
      return (counter.getErrorCount() > 0) ? 1 : 0;
    } catch (CmdLineException usageError) {
      stderr.append(usageError.getMessage() + "\n");
      return 1;
    } catch (InvalidConfigException invalidConfigException) {
      stderr.append(invalidConfigException.getMessage() + "\n");
      return 1;
    } catch (LinkageError linkageError) {
      stderr.append(GXPC_LINKAGE_ERROR_BANNER);
      return -1;
    } catch (Throwable throwable) {
      // TODO(laurence): include gxpc version info?
      throwable.printStackTrace();
      stderr.append(GXPC_BUG_BANNER);
      return -1;
    }
  }

  private static String makeStripe(int count) {
    StringBuilder sb = new StringBuilder(count);
    for (int i = 0; i < count; i++) {
      sb.append('*');
    }
    return sb.toString();
  }

  private static final String BANNER_STRIPE = makeStripe(79);

  public static final String GXPC_EMAIL_ADDRESS = "gxpc-eng@google.com";

  public static final String GXPC_BUG_BANNER =
      "\n"
      + BANNER_STRIPE + "\n"
      + "\n"
      + "  Looks like you found a bug in gxpc! Please email " + GXPC_EMAIL_ADDRESS + "\n"
      + "\n"
      + "  Please include the command-line arguments, full output (including the\n"
      + "  stack trace) and the location of your client workspace in your report.\n"
      + "\n"
      + BANNER_STRIPE + "\n";

  public static final String GXPC_LINKAGE_ERROR_BANNER =
      "\n"
      + BANNER_STRIPE + "\n"
      + "\n"
      + "  Looks like there's something wrong with your client workspace!\n"
      + "  Try doing a clean build.\n"
      + "\n"
      + "  If the problem persists, feel free to contact " + GXPC_EMAIL_ADDRESS + "\n"
      + "  for assistance.\n"
      + "\n"
      + BANNER_STRIPE + "\n";
}
