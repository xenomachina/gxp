// Copyright 2008 Google Inc. All rights reserved.

package com.google.gxp.compiler.i18ncheck;

import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.alerts.WarningAlert;
import com.google.gxp.compiler.base.Node;

/**
 * {@link WarningAlert} indicating an unnecessary nomsg tag or prefix
 */
public class UnnecessaryNomsgWarning extends WarningAlert {
  public UnnecessaryNomsgWarning(SourcePosition pos, String displayName) {
    super(pos, displayName + " is unnecessary and should be removed.");
  }

  public UnnecessaryNomsgWarning(Node node) {
    this(node.getSourcePosition(), node.getDisplayName());
  }
}
