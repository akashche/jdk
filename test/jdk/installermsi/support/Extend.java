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

package support;

import java.nio.file.Files;
import java.nio.file.Path;

import static support.Copy.copyDir;
import static support.FindPaths.findJdkImageDir;
import static support.FindPaths.findWixDir;

public class Extend {
    public static Path createInstaller(Path scratchImagesDir, Path instBuildRoot) throws Exception {
        Path wixDir = findWixDir();
        Path candleExe = wixDir.resolve("bin/candle.exe");
        if (!Files.exists(candleExe)) {
            candleExe = wixDir.resolve("candle.exe");
            if (!Files.exists(candleExe)) {
                throw new RuntimeException("Unable to find 'candle' utility," +
                        " WiX directory: [" + wixDir.toAbsolutePath() + "]");
            }
        }
        Path lightExe = wixDir.resolve("bin/light.exe");
        if (!Files.exists(lightExe)) {
            lightExe = wixDir.resolve("light.exe");
            if (!Files.exists(lightExe)) {
                throw new RuntimeException("Unable to find 'light' utility," +
                        " WiX directory: [" + wixDir.toAbsolutePath() + "]");
            }
        }

        Path jdkImage = findJdkImageDir();
        copyDir(jdkImage, scratchImagesDir.resolve("jdk"));
        Path instRes = jdkImage.getParent().resolve("installermsi/resources");
        copyDir(instRes, instBuildRoot.resolve("resources"));
        Files.move(instBuildRoot.resolve("vendor_ext1"), scratchImagesDir.resolve("vendor_ext1"));

        Path jdkWxs = instBuildRoot.resolve("jdk.wxs");
        int candleStatus = new ProcessBuilder(
                candleExe.toAbsolutePath().toString(),
                "-nologo",
                "-arch", "x64",
                jdkWxs.toAbsolutePath().toString()
        ).directory(instBuildRoot.toFile()).inheritIO().start().waitFor();
        if (0 != candleStatus) {
            throw new RuntimeException("Error running WiX candle utility");
        }

        Path jdkWixobj = instBuildRoot.resolve("jdk.wixobj");
        int lightStatus = new ProcessBuilder(
                lightExe.toAbsolutePath().toString(),
                "-nologo",
                "-sw1076",
                "-ext", "WixUIExtension",
                "-ext", "WixUtilExtension",
                jdkWixobj.toAbsolutePath().toString()
        ).directory(instBuildRoot.toFile()).inheritIO().start().waitFor();
        if (0 != lightStatus) {
            throw new RuntimeException("Error running WiX light utility");
        }

        return instBuildRoot.resolve("jdk.msi");
    }
}
