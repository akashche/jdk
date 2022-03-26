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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static support.FindPaths.findInstaller;
import static support.Format.formatCommandLine;

public class Uninstall {

    public static void uninstall() throws Exception {
        Optional<Path> msiPathOpt = findInstaller();
        if (msiPathOpt.isEmpty()) {
            System.out.println("Installer not found, uninstall skipped");
            return;
        }
        uninstallPackage(msiPathOpt.get());
    }

    public static void uninstallPackage(Path msiPath) throws Exception {
        List<String> cline = Arrays.asList(
            System.getenv("WINDIR") + "/system32/msiexec.exe",
            "/q",
            "/x",
            msiPath.toAbsolutePath().toString(),
            "/l*v",
            "uninstall.log"
        );

        System.out.println("Spawning uninstall process, command line: [" + formatCommandLine(cline) + "]");
        int code = new ProcessBuilder(cline)
                .inheritIO()
                .start()
                .waitFor();

        if (0 != code) {
            throw new Exception("Uninstall failure, code: [" + code + "]");
        }
    }
}
