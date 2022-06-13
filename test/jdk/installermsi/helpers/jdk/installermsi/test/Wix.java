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

import jdk.jpackage.test.Executor;

import java.nio.file.Files;
import java.nio.file.Path;

import static jdk.installermsi.test.Files.copyDirRecursive;
import static jdk.installermsi.test.FindPaths.findWixDir;

public class Wix {

    public static Path createExtendedInstaller(Path buildRoot, Path scratchImagesDir,
                                               Path instBuildRoot, Path jdkWxs) throws Exception {
        Path wixDir = findWixDir();
        Path candleExe = wixDir.resolve("bin/candle.exe");
        if (!java.nio.file.Files.exists(candleExe)) {
            candleExe = wixDir.resolve("candle.exe");
            if (!java.nio.file.Files.exists(candleExe)) {
                throw new RuntimeException("Unable to find 'candle' utility," +
                        " WiX directory: [" + wixDir.toAbsolutePath() + "]");
            }
        }
        Path lightExe = wixDir.resolve("bin/light.exe");
        if (!java.nio.file.Files.exists(lightExe)) {
            lightExe = wixDir.resolve("light.exe");
            if (!java.nio.file.Files.exists(lightExe)) {
                throw new RuntimeException("Unable to find 'light' utility," +
                        " WiX directory: [" + wixDir.toAbsolutePath() + "]");
            }
        }

        Path jdkImage = buildRoot.resolve("images/jdk");
        copyDirRecursive(jdkImage, scratchImagesDir.resolve("jdk"));
        Path instRes = jdkImage.getParent().resolve("installermsi/resources");
        copyDirRecursive(instRes, instBuildRoot.resolve("resources"));
        Files.move(instBuildRoot.resolve("vendor_ext1"), scratchImagesDir.resolve("vendor_ext1"));

        Executor.of(
                candleExe.normalize().toAbsolutePath().toString(),
                "-nologo",
                "-arch", "x64",
                jdkWxs.normalize().toAbsolutePath().toString()
        ).setDirectory(instBuildRoot).execute();

        Path jdkWixobj = instBuildRoot.resolve("jdk.wixobj");
        Executor.of(
                lightExe.normalize().toAbsolutePath().toString(),
                "-nologo",
                "-sw1076",
                "-ext", "WixUIExtension",
                "-ext", "WixUtilExtension",
                jdkWixobj.normalize().toAbsolutePath().toString()
        ).setDirectory(instBuildRoot).execute();

        return instBuildRoot.resolve("jdk.msi");
    }
}
