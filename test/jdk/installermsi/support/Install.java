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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static support.FindPaths.findInstaller;
import static support.Format.formatCommandLine;

public class Install {

    public static void install(String... options) throws Exception {
        Optional<Path> msiPath = findInstaller();
        if (msiPath.isEmpty()) {
            throw new Exception("Cannot find 'images/msi-installer/jdk.msi', run 'make installer-msi' before running this test");
        }
        installPackage(msiPath.get(), Arrays.asList(options));
    }

    public static void installPackage(Path msiPath, List<String> options) throws Exception {
        ArrayList<String> cline = new ArrayList<>(Arrays.asList(
                System.getenv("WINDIR") + "/system32/msiexec.exe",
                "/q",
                "/i",
                msiPath.toString(),
                "/l*v",
                "install.log",
                "INSTALLDIR=" + Paths.get("installed").toAbsolutePath().toString()
        ));
        cline.addAll(options);

        System.out.println("Spawning install process, command line: [" + formatCommandLine(cline) + "]");
        int code = new ProcessBuilder(cline)
                .inheritIO()
                .start()
                .waitFor();

        if (0 != code) {
            throw new Exception("Install failure, code: [" + code + "]");
        }
    }
}
