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

import java.nio.file.*;
import java.util.List;

import static support.Assert.*;
import static support.Extend.createInstaller;
import static support.FindPaths.*;
import static support.Install.*;
import static support.Uninstall.uninstallPackage;

/**
 * @test
 * @requires (os.family == "windows")
 */

public class ExtendScriptTest {

    public static void main(String[] args) throws Exception {
        Path scratchImagesDir = Path.of("images");
        Files.createDirectory(scratchImagesDir);
        Path instBuildDir = scratchImagesDir.resolve("instbuild");
        Files.createDirectory(instBuildDir);

        transformDescriptor(instBuildDir);
        Path jdkMsi = createInstaller(scratchImagesDir, instBuildDir);

        installPackage(jdkMsi, List.of("ADDLOCAL=ALL"));
        try {

            assertPath("installed/bin");
            assertPath("installed/bin/java.exe");
            assertPath("installed/bin/server/jvm.dll");
            assertPath("installed/lib/modules");
            assertPath("installed/conf");
            assertPath("installed/include");
            assertPath("installed/jmods");
            assertPath("installed/legal");
            assertPath("installed/lib");
            assertPath("installed/vendor_ext1");

        } finally {
            uninstallPackage(jdkMsi);
        }
    }

    private static void transformDescriptor(Path instBuildDir) throws Exception {
        Path denoExe = findDenoExe();
        Path srcRoot = findSrcRoot();
        Path jdkXml = srcRoot.resolve("make/data/installermsi/jdk.xml");
        Path extendScript = srcRoot.resolve("test/jdk/installermsi/extend/ExtendScript.js");
        Path jdkWxs = instBuildDir.resolve("jdk.wxs");
        int status = new ProcessBuilder(
                denoExe.toAbsolutePath().toString(),
                "run",
                "-A",
                extendScript.toAbsolutePath().toString(),
                jdkXml.toAbsolutePath().toString(),
                jdkWxs.toAbsolutePath().toString()
        ).directory(instBuildDir.toFile()).inheritIO().start().waitFor();
        if (0 != status) {
            throw new RuntimeException("Error running Deno, please check that 'deno.exe' is present in PATH");
        }
    }
}
