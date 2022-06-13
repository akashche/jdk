/*
 * Copyright (c) 2022, Red Hat Inc. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package jdk.installermsi.test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.*;
import static jdk.jpackage.test.TKit.*;

public class Files {

    public static void assertInstalledFiles(Path image, Path installed) {
        try {

            walkFileTree(image, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    FileVisitResult result = super.visitFile(file, attrs);

                    Path rel = image.relativize(file);
                    Path inst = installed.resolve(rel);
                    assertFileExists(inst);
                    assertEquals(java.nio.file.Files.size(inst), java.nio.file.Files.size(file),
                            "files size must be the same, path: [" + rel + "]");

                    return result;
                }
            });

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void assertVendorInstalledFiles(Path installed) throws Exception {
        Path vendorExt1 = installed.resolve("vendor_ext1");
        assertDirectoryExists(vendorExt1);
        Path file1 = vendorExt1.resolve("file1.txt");
        assertFileExists(file1);
        String foo = readString(file1, UTF_8);
        assertEquals("foo", foo, "vendor_ext1/file1.txt");
        Path file2 = vendorExt1.resolve("file2.txt");
        assertFileExists(file2);
        String bar = readString(file2, UTF_8);
        assertEquals("bar", bar, "vendor_ext1/file2.txt");
    }

    public static void copyDirRecursive(Path source, Path target, CopyOption... options) throws IOException {
        walkFileTree(source, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException {
                java.nio.file.Files.createDirectories(target.resolve(source.relativize(dir)));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException {
                java.nio.file.Files.copy(file, target.resolve(source.relativize(file)), options);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
