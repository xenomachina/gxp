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

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.gxp.compiler.Configuration;
import com.google.gxp.compiler.Phase;
import com.google.gxp.compiler.alerts.Alert.Severity;
import com.google.gxp.compiler.alerts.Alert;
import com.google.gxp.compiler.alerts.AlertPolicy;
import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.alerts.common.BadNodePlacementError;
import com.google.gxp.compiler.alerts.common.ProgressAlert;
import com.google.gxp.compiler.alerts.common.SaxAlert;
import com.google.gxp.compiler.base.OutputLanguage;
import com.google.gxp.compiler.codegen.DefaultCodeGeneratorFactory;
import com.google.gxp.compiler.fs.FileRef;
import com.google.gxp.compiler.fs.FileSystem;
import com.google.gxp.compiler.fs.SystemFileSystem;
import com.google.gxp.compiler.i18ncheck.UnextractableContentAlert;

import org.kohsuke.args4j.CmdLineException;
import org.xml.sax.SAXException;

import java.util.*;
import java.io.*;
import junit.framework.TestCase;

import static com.google.gxp.testing.MoreAsserts.*;

/**
 * Tests for {@code GxpcFlags}.
 */
public class GxpcFlagsTest extends TestCase {
  // We use SystemFileSystem because we care about interactions with the
  // working directory.
  private final FileSystem sysFs = SystemFileSystem.INSTANCE;

  private AlertPolicy alertPolicy;

  // Let's use names of UNIX separators for conciseness.
  private static final String COLON = File.pathSeparator;
  private static final String SLASH = File.separator;

  private GxpcFlags createConfig(FileRef defaultDir, String... args)
      throws Exception {
    return new GxpcFlags(sysFs, defaultDir, args);
  }

  private GxpcFlags createConfig(String... args) throws Exception {
    return createConfig(getCwd(), args);
  }

  /**
   * Sanity check: tests that when a flag is repeatedly specified, the last one
   * wins.
   */
  public void testFlagOverriding() throws Exception {
    assertMessageSourceEquals(null,
                              createConfig());

    assertMessageSourceEquals("foo",
                              createConfig("--message_source", "foo"));

    assertMessageSourceEquals("bar",
                              createConfig("--message_source", "foo",
                                           "--message_source", "bar"));
  }

  /**
   * Sanity check: verify that nonexistant flags cause an exception.
   */
  public void testBadFlag() throws Exception {
    try {
      createConfig("--nonexistantflag");
      fail();
    } catch (CmdLineException e) {
      // yay!!!
    }
  }

  public void testHelp() throws Exception {
    GxpcFlags flags = createConfig("--help");
    assertTrue(flags.showHelp());
  }

  private FileRef getCwd() {
    return sysFs.parseFilename(System.getProperty("user.dir"));
  }

  /**
   * Asserts that {@code fnam1} and {@code fnam2} refers to the same underlying
   * file.
   */
  private static void assertReallySameFile(FileRef fnam1, FileRef fnam2) {
    assertEquals(fnam1.toFilename(), fnam2.toFilename());
  }

  private static final Function<FileRef, String> GET_NAME =
      new Function<FileRef, String>() {
        public String apply(FileRef x) {
          return x.getName();
        }
      };

  public void testGetSourceFiles() throws Exception {
    // No sources specified.
    Configuration config = createConfig();
    assertTrue(config.getSourceFiles().isEmpty());

    // Sources specified, but no source path.
    String[] sources = new String[] {
      "com" + SLASH + "google" + SLASH + "foo" + SLASH + "ford.gxp",
      "com" + SLASH + "google" + SLASH + "foo" + SLASH + "zaphod.gxp",
      "com" + SLASH + "google" + SLASH + "bar" + SLASH + "trillian.gxp"
    };
    config = createConfig(getCwd(), sources);
    assertContentsAnyOrder(Iterables.transform(config.getSourceFiles(),
                                               GET_NAME),
                           "/com/google/foo/ford.gxp",
                           "/com/google/foo/zaphod.gxp",
                           "/com/google/bar/trillian.gxp");

    // Same as above, but with different defaultDir.
    config = createConfig(getCwd().join("com/google"), sources);

    assertContentsAnyOrder(Iterables.transform(config.getSourceFiles(),
                                               GET_NAME),
                           "/foo/ford.gxp",
                           "/foo/zaphod.gxp",
                           "/bar/trillian.gxp");

    // Sources specified, with source path.
    config = createConfig(
        "--source", "src_dir_1" + COLON + "src_dir_2",
        "src_dir_1" + SLASH + "ford.gxp",
        "src_dir_1" + SLASH + "zaphod.gxp",
        "src_dir_2" + SLASH + "trillian.gxp");
    assertContentsAnyOrder(Iterables.transform(config.getSourceFiles(),
                                               GET_NAME),
                           "/ford.gxp",
                           "/zaphod.gxp",
                           "/trillian.gxp");

    // Sources specified, with "overlapping" dirs in source path.
    config = createConfig(
        "--source",
        "foo" + SLASH + "bar" + SLASH + "baz"
          + COLON
          + "foo" + SLASH + "bar",
        "foo" + SLASH + "bar" + SLASH + "baz" + SLASH + "quux.gxp",
        "foo" + SLASH + "bar" + SLASH + "zip" + SLASH + "zap.gxp"
        );
    assertContentsAnyOrder(
        Iterables.transform(config.getSourceFiles(), GET_NAME),
        "/quux.gxp",
        "/zip/zap.gxp");

    // Try the opposite order of items in the source path -- we should get the
    // same result.
    config = createConfig(
        "--source",
        "foo" + SLASH + "bar"
          + COLON
          + "foo" + SLASH + "bar" + SLASH + "baz",
        "foo" + SLASH + "bar" + SLASH + "baz" + SLASH + "quux.gxp",
        "foo" + SLASH + "bar" + SLASH + "zip" + SLASH + "zap.gxp"
        );
    assertContentsAnyOrder(
        Iterables.transform(config.getSourceFiles(), GET_NAME),
        "/quux.gxp",
        "/zip/zap.gxp");
  }

  public void testGetOutputLanguages() throws Exception {
    Configuration config = createConfig();
    assertContentsAnyOrder(config.getOutputLanguages());

    config = createConfig("--output_language", "java");
    assertContentsAnyOrder(config.getOutputLanguages(),
                           OutputLanguage.JAVA);

    config = createConfig("--output_language", "java",
                          "--output_language", "xmb");
    assertContentsAnyOrder(config.getOutputLanguages(),
                           OutputLanguage.JAVA, OutputLanguage.XMB);
  }

  public void testGetAllowedOutputFilenames() throws Exception {
    Configuration config = createConfig();
    assertTrue(config.getAllowedOutputFileRefs().isEmpty());
    FileSystem fs;

    config = createConfig(
        getCwd(),
        "--output", "my_out_dir/ford.java",
        "--output", "my_out_dir/zaphod.java");
    assertContentsAnyOrder(
        Iterables.transform(config.getAllowedOutputFileRefs(), GET_NAME),
        "/my_out_dir/ford.java",
        "/my_out_dir/zaphod.java");

    // Same as above, but with different defaultDir.
    config = createConfig(
        getCwd().join("my_out_dir"),
        "--output", "my_out_dir/ford.java",
        "--output", "my_out_dir/zaphod.java");
    assertContentsAnyOrder(
        Iterables.transform(config.getAllowedOutputFileRefs(), GET_NAME),
        "/ford.java",
        "/zaphod.java");

    // The --dir flag "re-bases" the location of output files.
    config = createConfig(
        "--dir", "my_out_dir",
        "--output", "my_out_dir/ford.java",
        "--output", "my_out_dir/zaphod.java");
    assertContentsAnyOrder(
        Iterables.transform(config.getAllowedOutputFileRefs(), GET_NAME),
        "/ford.java",
        "/zaphod.java");
  }

  public void testGetDotPhases() throws Exception {
    Configuration config;

    // Base case.
    config = createConfig();
    assertTrue(config.getDotPhases().isEmpty());

    // One phase.
    config = createConfig("--dot", "reparented");
    assertContentsAnyOrder(config.getDotPhases(),
        Phase.REPARENTED);

    // A few phases.
    config = createConfig("--dot", "reparented",
                          "--dot", "space-collapsed",
                          "--dot", "placeholder-pivoted");
    assertContentsInOrder(config.getDotPhases(),
        Phase.REPARENTED,
        Phase.SPACE_COLLAPSED,
        Phase.PLACEHOLDER_PIVOTED);

    // A few phases, different order (should be sorted).
    config = createConfig("--dot", "placeholder-pivoted",
                          "--dot", "space-collapsed",
                          "--dot", "reparented");
    assertContentsInOrder(config.getDotPhases(),
        Phase.REPARENTED,
        Phase.SPACE_COLLAPSED,
        Phase.PLACEHOLDER_PIVOTED);

    // Wildcard.
    config = createConfig("--dot", "*");
    assertContentsAnyOrder(config.getDotPhases(),
        (Object[]) Phase.values());
  }

  public void testGetNamespaces() throws Exception {
    Configuration config = createConfig();
    // TODO(harryh): test creation of non-standard namespaces
    // assertSame(namespaces, config.getNamespaces());
  }

  public void testGetCodeGeneratorFactory() throws Exception {
    Configuration config = createConfig();
    assertMessageSourceEquals(null, config);
    assertDynamicModeEnabledEquals(false, config);

    config = createConfig("--message_source", "com.google.message.source");
    assertMessageSourceEquals("com.google.message.source", config);
    assertDynamicModeEnabledEquals(false, config);

    config = createConfig("--dynamic");
    assertMessageSourceEquals(null, config);
    assertDynamicModeEnabledEquals(true, config);

    config = createConfig("--message_source", "com.google.message.source",
                          "--dynamic");
    assertMessageSourceEquals("com.google.message.source", config);
    assertDynamicModeEnabledEquals(true, config);
  }

  private void assertMessageSourceEquals(String expected,
                                         Configuration config) {
    DefaultCodeGeneratorFactory codeGenFactory =
        (DefaultCodeGeneratorFactory) config.getCodeGeneratorFactory();
    assertEquals(expected, codeGenFactory.getRuntimeMessageSource());
  }

  private void assertDynamicModeEnabledEquals(boolean expected,
                                              Configuration config) {
    DefaultCodeGeneratorFactory codeGenFactory =
        (DefaultCodeGeneratorFactory) config.getCodeGeneratorFactory();
    assertEquals(expected, codeGenFactory.isDynamicModeEnabled());
  }

  public void testGetDependencyFile() throws Exception {
    Configuration config = createConfig();
    assertNull(config.getDependencyFile());

    config = createConfig(
        "--depend", "foobar.gxd");
    assertEquals(getCwd().join("foobar.gxd"),
                 config.getDependencyFile());

    // location of depend file should not be affected by --dir flag
    config = createConfig(
        "--dir", "my_out_dir",
        "--depend", "foobar.gxd");
    assertEquals(getCwd().join("foobar.gxd"),
                 config.getDependencyFile());
  }

  public void testGetPropertiesFile() throws Exception {
    Configuration config = createConfig();
    assertNull(config.getPropertiesFile());

    config = createConfig(
        "--output_properties",
        "--message_source", "com.google.message.source");
    assertEquals(getCwd().join("com/google/message/source_en.properties"),
                 config.getPropertiesFile());

    // location of properties file should be affected by --dir flat
    config = createConfig(
        "--dir", "/outdir",
        "--output_properties",
        "--message_source", "com.google.message.source");
    assertEquals(sysFs.parseFilename("/outdir/com/google/message/source_en.properties"),
                 config.getPropertiesFile());
  }

  public void testIsVerboseEnabled() throws Exception {
    GxpcFlags config = createConfig();
    assertFalse(config.isVerboseEnabled());

    config = createConfig("--verbose");
    assertTrue(config.isVerboseEnabled());
  }

  public void testIsDebugEnabled() throws Exception {
    Configuration config = createConfig();
    assertFalse(config.isDebugEnabled());

    config = createConfig("--g");
    assertTrue(config.isDebugEnabled());
  }

  private static final SourcePosition SOURCE_POS =
      new SourcePosition("whatever", 1, 2);
  private static final Alert ERROR_ALERT =
      new BadNodePlacementError(SOURCE_POS, "foo", "bar");
  private static final Alert WARNING_ALERT =
      new SaxAlert(SOURCE_POS, Severity.WARNING, new SAXException("!!!"));
  private static final Alert INFO_ALERT =
      new ProgressAlert(SOURCE_POS, "The sky is blue.");
  private static final Alert I18N_ALERT =
      new UnextractableContentAlert(SOURCE_POS, "thing");

  public void testGetWarnFlags_baseCase() throws Exception {
    alertPolicy = createConfig().getAlertPolicy();
    assertEquals(Severity.ERROR, alertPolicy.getSeverity(ERROR_ALERT));
    assertEquals(Severity.WARNING, alertPolicy.getSeverity(WARNING_ALERT));
    assertEquals(Severity.INFO, alertPolicy.getSeverity(INFO_ALERT));
    assertEquals(Severity.INFO, alertPolicy.getSeverity(I18N_ALERT));
  }

  public void testGetWarnFlags_warnI18n() throws Exception {
    alertPolicy = createConfig("--warn", "i18n").getAlertPolicy();
    assertEquals(Severity.ERROR, alertPolicy.getSeverity(ERROR_ALERT));
    assertEquals(Severity.WARNING, alertPolicy.getSeverity(WARNING_ALERT));
    assertEquals(Severity.INFO, alertPolicy.getSeverity(INFO_ALERT));
    assertEquals(Severity.WARNING, alertPolicy.getSeverity(I18N_ALERT));
  }

  public void testGetWarnFlags_warnError() throws Exception {
    alertPolicy = createConfig("--warn", "error").getAlertPolicy();
    assertEquals(Severity.ERROR, alertPolicy.getSeverity(ERROR_ALERT));
    assertEquals(Severity.ERROR, alertPolicy.getSeverity(WARNING_ALERT));
    assertEquals(Severity.INFO, alertPolicy.getSeverity(INFO_ALERT));
    assertEquals(Severity.INFO, alertPolicy.getSeverity(I18N_ALERT));
  }

  public void testGetWarnFlags_warnI18nError() throws Exception {
    alertPolicy = createConfig("--warn", "error",
                               "--warn", "i18n").getAlertPolicy();
    assertEquals(Severity.ERROR, alertPolicy.getSeverity(ERROR_ALERT));
    assertEquals(Severity.ERROR, alertPolicy.getSeverity(WARNING_ALERT));
    assertEquals(Severity.INFO, alertPolicy.getSeverity(INFO_ALERT));
    assertEquals(Severity.ERROR, alertPolicy.getSeverity(I18N_ALERT));

    alertPolicy = createConfig("--warn", "i18n",
                               "--warn", "error").getAlertPolicy();
    assertEquals(Severity.ERROR, alertPolicy.getSeverity(ERROR_ALERT));
    assertEquals(Severity.ERROR, alertPolicy.getSeverity(WARNING_ALERT));
    assertEquals(Severity.INFO, alertPolicy.getSeverity(INFO_ALERT));
    assertEquals(Severity.ERROR, alertPolicy.getSeverity(I18N_ALERT));
  }

  public void testGetWarnFlags_warnI18nErrorWithDuplicates() throws Exception {
    alertPolicy = createConfig("--warn", "i18n",
                               "--warn", "error",
                               "--warn", "i18n").getAlertPolicy();
    assertEquals(Severity.ERROR, alertPolicy.getSeverity(ERROR_ALERT));
    assertEquals(Severity.ERROR, alertPolicy.getSeverity(WARNING_ALERT));
    assertEquals(Severity.INFO, alertPolicy.getSeverity(INFO_ALERT));
    assertEquals(Severity.ERROR, alertPolicy.getSeverity(I18N_ALERT));
  }

  public void testGenErrorFlags_errorI18n() throws Exception {
    alertPolicy = createConfig("--error", "i18n").getAlertPolicy();
    assertEquals(Severity.ERROR, alertPolicy.getSeverity(ERROR_ALERT));
    assertEquals(Severity.WARNING, alertPolicy.getSeverity(WARNING_ALERT));
    assertEquals(Severity.INFO, alertPolicy.getSeverity(INFO_ALERT));
    assertEquals(Severity.ERROR, alertPolicy.getSeverity(I18N_ALERT));
  }

  public void testGenErrorFlags_errorI18nAndwarnI18n() throws Exception {
    alertPolicy = createConfig("--error", "i18n",
                               "--warn", "i18n").getAlertPolicy();
    assertEquals(Severity.ERROR, alertPolicy.getSeverity(ERROR_ALERT));
    assertEquals(Severity.WARNING, alertPolicy.getSeverity(WARNING_ALERT));
    assertEquals(Severity.INFO, alertPolicy.getSeverity(INFO_ALERT));
    assertEquals(Severity.ERROR, alertPolicy.getSeverity(I18N_ALERT));
  }
}
