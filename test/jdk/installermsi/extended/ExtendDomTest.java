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

import jdk.jpackage.test.Executor;

import java.nio.file.Files;
import java.nio.file.Path;

import static jdk.installermsi.test.EnvVars.*;
import static jdk.installermsi.test.Files.*;
import static jdk.installermsi.test.FindPaths.*;
import static jdk.installermsi.test.Install.*;
import static jdk.installermsi.test.Registry.*;
import static jdk.installermsi.test.Wix.createExtendedInstaller;
import static jdk.jpackage.test.Annotations.Test;

/*
 * @test
 * @summary test DOM extension example for jdk installer
 * @library ../../tools/jpackage/helpers
 * @library ../helpers
 * @requires (os.family == "windows")
 * @modules jdk.jpackage/jdk.jpackage.internal
 *
 * @compile ExtendDomTest.java
 * @run main/othervm jdk.jpackage.test.Main --jpt-run=ExtendDomTest
 */
public class ExtendDomTest {

    @Test
    public static void test() throws Exception {

        // prepare directories
        Path scratchImagesDir = Path.of(".");
        Path instBuildDir = scratchImagesDir.resolve("instbuild");
        Files.createDirectory(instBuildDir);

        // transform installer XML
        Path jdkWxs = transformXml(instBuildDir);

        // create new installer package
        Path buildRoot = findBuildRoot();
        Path msi = createExtendedInstaller(buildRoot, scratchImagesDir, instBuildDir, jdkWxs);
        Path image = findJdkImage();

        if (memberOfAdministratorsGroup()) {

            // install MSI and check
            Path installed = install(msi, "ADDLOCAL=ALL");
            try {

                assertInstalledFiles(image, installed);
                assertVendorInstalledFiles(installed);
                assertRegistryRuntime(installed);
                assertEnvPath(installed);
                assertEnvJavaHome(installed);
                assertEnvVendorJavaHome(installed);
                assertRegistryJar(installed);

            } finally {
                uninstall(msi);
            }

        } else {

            // unpack MSI and check files
            Path dir = unpack(msi);
            Path installed = dir.getParent().resolve("VendorDirectory/jdk-VENDOR_VERSION");
            assertInstalledFiles(image, installed);
            assertVendorInstalledFiles(installed);

        }
    }

    private static Path transformXml(Path instBuildDir) {
        Path javaExe = findTestJavaExe();
        Path jdkXml = findInstallerMsiXml();
        Path testSrcDir = findTestSrcDir();
        Path extendDomJava = testSrcDir.resolve("extended/example/ExtendDom.java");
        Path jdkWxs = instBuildDir.resolve("jdk.wxs");
        Executor.of(
                javaExe.toAbsolutePath().toString(),
                extendDomJava.toAbsolutePath().toString(),
                jdkXml.toAbsolutePath().toString(),
                jdkWxs.toAbsolutePath().toString()
        ).setDirectory(instBuildDir).execute();
        return jdkWxs;
    }
}
