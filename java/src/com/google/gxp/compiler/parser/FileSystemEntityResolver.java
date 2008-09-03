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

package com.google.gxp.compiler.parser;

import com.google.common.base.Preconditions;
import com.google.gxp.compiler.alerts.AlertSink;
import com.google.gxp.compiler.alerts.InfoAlert;
import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.fs.FileRef;
import com.google.gxp.compiler.fs.FileSystem;

import java.io.IOException;
import java.io.InputStream;

/**
 * An entity resolver implementation that can resolve entities based on an
 * abstract file system that interprets public ids that start with // as
 * file system paths relative to the build system root, and that
 * can resolve System IDs under http://gxp.googlecode.com/svn/trunk/resources/
 * to a resouce available from the class path.
 */
public class FileSystemEntityResolver implements SourceEntityResolver {

  /**
   * prefix for files resolved using the java classloader.
   */
  private static final String EXTERNAL_ENTITY_PREFIX =
      "http://gxp.googlecode.com/svn/trunk/resources/";

  /**
   * prefix for public ids that are resolved relative to the revision control or
   * project root directory
   */
  private static final String SOURCE_ROOT_PUBLIC_ID_PREFIX = "//";

  private final FileSystem fileSystem;

  public FileSystemEntityResolver(FileSystem fileSystem) {
    this.fileSystem = Preconditions.checkNotNull(fileSystem);
  }

  /**
   * This implementation only allows systemIds which are in a predetermined
   * set.
   *
   * @throws IOException if the protocol is recognized but the underlying file
   *     could not be retrieved.
   * @throws UnsupportedExternalEntityException if the protocol isn't recognized
   */
  public InputStream resolveEntity(SourcePosition pos, String publicId, String systemId,
                                   AlertSink alertSink)
      throws IOException {

    if (systemId.startsWith(EXTERNAL_ENTITY_PREFIX)) {
      InputStream stream = resolveEntityFromResource(
          pos, systemId, EXTERNAL_ENTITY_PREFIX.length(), alertSink);
      if (stream != null) {
        return stream;
      }
    } else if (publicId != null &&
               publicId.startsWith(SOURCE_ROOT_PUBLIC_ID_PREFIX)) {
      // If the public id starts with // treat it as a path relative to
      // the build system / project root.
      String relPath = publicId.substring(SOURCE_ROOT_PUBLIC_ID_PREFIX.length());
      if (!"".equals(relPath) && !relPath.startsWith("/")) {
        // convert URI path to local file system conventions
        FileRef file = fileSystem.getRoot().join(relPath);
        alertSink.add(new EntityResolvedNotification(pos, publicId, file.toFilename()));
        return file.openInputStream();
      }
    }

    throw unresolved(pos, publicId, systemId, null);
  }

  private InputStream resolveEntityFromResource(SourcePosition pos, String systemId,
                                                int prefixLength, AlertSink alertSink)
      throws IOException {
    Class<?> cls = getClass();
    String resourceName = "/" + cls.getPackage().getName().replace('.', '/') +
                          "/" + systemId.substring(prefixLength);
    alertSink.add(new EntityResolvedNotification(pos, systemId, resourceName));
    return cls.getResourceAsStream(resourceName);
  }

  private static RuntimeException unresolved(SourcePosition pos, String publicId,
                                             String systemId, Throwable cause) {
    UnsupportedExternalEntityException error =
        new UnsupportedExternalEntityException(pos, publicId, systemId);
    if (cause != null) {
      error.initCause(cause);
    }
    return error;
  }

  /**
   * a notication that an external entity has been resolved to a resource or
   * file.
   */
  public static class EntityResolvedNotification extends InfoAlert {
    private EntityResolvedNotification(SourcePosition entityRefPos, String id, String realPath) {
      super(entityRefPos, "Resolved entity `" + id + "` to `" + realPath + "`");
    }
  }
}
