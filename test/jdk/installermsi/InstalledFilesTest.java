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

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.Files.*;
import static support.Assert.*;
import static support.FindPaths.findJdkImageDir;
import static support.Install.install;
import static support.Uninstall.uninstall;

/**
 * @test
 * @requires (os.family == "windows")
 */

public class InstalledFilesTest {

    public static void main(String[] args) throws Exception {
        install("ADDLOCAL=jdk");
        try {

            Path jdkImageDir = findJdkImageDir();
            Path installedDir = Path.of("installed");
            verifyInstalled(jdkImageDir, installedDir);

        } finally {
            uninstall();
        }
    }

    private static void verifyInstalled(Path image, Path installed) throws Exception {
        walkFileTree(image, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                FileVisitResult result = super.visitFile(file, attrs);
                if (file.toString().endsWith(".pdb") || file.toString().endsWith(".map")) {
                    return result;
                }

                Path rel = image.relativize(file);
                Path inst = installed.resolve(rel);

                assertThat("file exists, path: [" + rel + "]", exists(inst) && isRegularFile(inst));
                assertThat("files size is the same, path: [" + rel + "]", size(file) == size(inst));

                return result;
            }
        });
    }
}
