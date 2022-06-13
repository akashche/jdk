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

import java.nio.file.Path;

import static jdk.installermsi.test.EnvVars.*;
import static jdk.installermsi.test.Files.*;
import static jdk.installermsi.test.FindPaths.*;
import static jdk.installermsi.test.Install.*;
import static jdk.installermsi.test.Registry.*;
import static jdk.jpackage.test.Annotations.Test;

/*
 * @test
 * @summary test installation of the jdk installer with only "jdk_env_path" feature selected
 * @library ../../tools/jpackage/helpers
 * @library ../helpers
 * @requires (os.family == "windows")
 * @modules jdk.jpackage/jdk.jpackage.internal
 *
 * @compile EnvPathTest.java
 * @run main/othervm jdk.jpackage.test.Main --jpt-run=EnvPathTest
 */
public class EnvPathTest {

    @Test
    public static void test() {
        if (userCannotInstallMsiPackages()) {
            return;
        }

        Path msi = findInstallerMsi();
        Path installed = install(msi, "ADDLOCAL=jdk_env_path");
        try {
            Path image = findJdkImage();

            assertInstalledFiles(image, installed);
            assertNoRegistryRuntime(installed);
            assertEnvPath(installed);
            assertNoEnvJavaHome(installed);
            assertNoRegistryJar(installed);

        } finally {
            uninstall(msi);
        }
    }
}
