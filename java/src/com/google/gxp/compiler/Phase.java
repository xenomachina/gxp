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

package com.google.gxp.compiler;

import com.google.gxp.compiler.base.Forest;
import com.google.gxp.compiler.base.Node;

/**
 * The compilation phases, in order.
 */
public enum Phase {
  PARSED {
    @Override
    public Forest<? extends Node> getForest(
        CompilationUnit compilationUnit) {
      return compilationUnit.getParseTree();
    }
  },
  IF_EXPANDED {
    @Override
    public Forest<? extends Node> getForest(
        CompilationUnit compilationUnit) {
      return compilationUnit.getIfExpandedTree();
    }
  },
  REPARENTED {
    @Override
    public Forest<? extends Node> getForest(
        CompilationUnit compilationUnit) {
      return compilationUnit.getReparentedTree();
    }
  },
  BOUND {
    @Override
    public Forest<? extends Node> getForest(
        CompilationUnit compilationUnit) {
      return compilationUnit.getBoundTree();
    }
  },
  SPACE_COLLAPSED {
    @Override
    public Forest<? extends Node> getForest(
        CompilationUnit compilationUnit) {
      return compilationUnit.getSpaceCollapsedTree();
    }
  },
  PLACEHOLDER_INSERTED {
    @Override
    public Forest<? extends Node> getForest(
        CompilationUnit compilationUnit) {
      return compilationUnit.getPlaceholderInsertedTree();
    }
  },
  ESCAPED {
    @Override
    public Forest<? extends Node> getForest(
        CompilationUnit compilationUnit) {
      return compilationUnit.getEscapedTree();
    }
  },
  VALIDATED {
    @Override
    public Forest<? extends Node> getForest(
        CompilationUnit compilationUnit) {
      return compilationUnit.getValidatedTree();
    }
  },
  CONTENT_FLATTENED {
    @Override
    public Forest<? extends Node> getForest(
        CompilationUnit compilationUnit) {
      return compilationUnit.getContentFlattenedTree();
    }
  },
  PLACEHOLDER_PIVOTED {
    @Override
    public Forest<? extends Node> getForest(
        CompilationUnit compilationUnit) {
      return compilationUnit.getPlaceholderPivotedTree();
    }
  },
  I18N_CHECKED {
    @Override
    public Forest<? extends Node> getForest(
        CompilationUnit compilationUnit) {
      return compilationUnit.getI18nCheckedTree();
    }
  },
  MESSAGE_EXTRACTED {
    @Override
    public Forest<? extends Node> getForest(
        CompilationUnit compilationUnit) {
      return compilationUnit.getMessageExtractedTree();
    }
  };

  public abstract Forest<? extends Node> getForest(
      CompilationUnit compilationUnit);
}
