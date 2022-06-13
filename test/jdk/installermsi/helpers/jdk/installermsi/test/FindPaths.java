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
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.Files.*;
import static java.nio.file.Files.isDirectory;
import static jdk.installermsi.test.Registry.SYSTEM_ENVIRONMENT_REGKEY;
import static jdk.jpackage.test.WindowsHelper.queryRegistryValue;

public class FindPaths {

    public static final String DENO_HOME = "DENO_HOME";
    private static final String INSTALLERMSI_TEST_BUILD_ROOT = "INSTALLERMSI_TEST_BUILD_ROOT";

    public static Path findTestSrcDir() {
        Path dir = Path.of(System.getProperty("test.src"));

        for (int i = 0; i < 8; i++) {
            if ("installermsi".equals(dir.getFileName().toString()) && isDirectory(dir)) {
                return dir.normalize().toAbsolutePath();
            }
            dir = dir.getParent();
        }

        throw new RuntimeException("Unable to locate test sources root path");
    }

    public static Path findSrcRoot() {
        Path testSrcDir = findTestSrcDir();

        Path srcRoot = testSrcDir.resolve("../../..");
        if (isDirectory(srcRoot.resolve("src"))) {
            return srcRoot.normalize().toAbsolutePath();
        }

        throw new RuntimeException("Unable to locate sources root path");
    }

    public static Path findBuildRoot() {
        // expect running with make run-test
        Path runtestRoot = Path.of(".");

        for (int i = 0; i < 10; i++) {
            if (isDirectory(runtestRoot.resolve("test-support"))) {
                return runtestRoot.normalize().toAbsolutePath();
            }
            runtestRoot = runtestRoot.resolve("..");
        }

        // not running with make run-test, check default build dir
        Path srcRoot = findSrcRoot();

        Path defaultRoot = srcRoot.resolve("build/windows-x86_64-server-release");
        if (isDirectory(defaultRoot)) {
            return defaultRoot.normalize().toAbsolutePath();
        }

        // not using default build dir, check env var
        String rootFromEnvVar = System.getenv(INSTALLERMSI_TEST_BUILD_ROOT);
        if (null != rootFromEnvVar) {
            Path rootFromEnv = Path.of(rootFromEnvVar);
            if (isDirectory(rootFromEnv)) {
                return rootFromEnv.normalize().toAbsolutePath();
            }
        }

        // not found
        throw new RuntimeException("Unable to locate build root path," +
                " please specify '-e:" + INSTALLERMSI_TEST_BUILD_ROOT + "=c:/path/to/jdk/build/root'" +
                " option to JTreg command");
    }

    public static Path findJdkImage() {
        Path buildRoot = findBuildRoot();
        Path image = buildRoot.resolve("images/jdk");

        if (isDirectory(image)) {
            return image.normalize().toAbsolutePath();
        }

        throw new RuntimeException("Unable to locate jdk image path");
    }

    public static Path findInstallerMsi() {
        Path buildRoot = findBuildRoot();
        Path msiDir = buildRoot.resolve("images/installermsi");
        try (DirectoryStream<Path> stream = newDirectoryStream(msiDir)) {
            for (Path path : stream) {
                String name = path.getFileName().toString();
                if (isRegularFile(path) && name.startsWith("openjdk-") && name.endsWith(".msi")) {
                    return path.normalize().toAbsolutePath();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        throw new RuntimeException("Unable to locate MSI installer," +
                " please run 'make installer-msi' and re-run the test");
    }

    public static Path findInstallerMsiXml() {
        Path buildRoot = findBuildRoot();
        Path msiDir = buildRoot.resolve("images/installermsi");
        try (DirectoryStream<Path> stream = newDirectoryStream(msiDir)) {
            for (Path path : stream) {
                String name = path.getFileName().toString();
                if (isRegularFile(path) && name.startsWith("openjdk-") && name.endsWith(".xml")) {
                    return path.normalize().toAbsolutePath();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        throw new RuntimeException("Unable to locate MSI installer XML," +
                " please run 'make installer-msi-xml' and re-run the test");
    }

    public static Path findWixDir() {
        String envOpt = queryRegistryValue(SYSTEM_ENVIRONMENT_REGKEY, "WIX");
        if (null == envOpt) {
            throw new RuntimeException("Unable to find WiX directory," +
                    " 'WIX' environment variable is not set");
        }
        Path wixDir = Path.of(envOpt);
        if (!isDirectory(wixDir)) {
            throw new RuntimeException("Unable to find WiX directory," +
                    " please check that 'WIX' environment variable is set correctly");
        }
        return wixDir;
    }

    public static Path findTestJavaExe() {
        Path testJdk = Path.of(System.getProperty("test.jdk"));
        return testJdk.resolve("bin/java.exe");
    }

    public static Path findDenoExe() {
        String envOpt = queryRegistryValue(SYSTEM_ENVIRONMENT_REGKEY, DENO_HOME);
        if (null == envOpt) {
            throw new RuntimeException("Unable to find Deno directory," +
                    " '" + DENO_HOME + "' variable is not set");
        }
        Path denoDir = Path.of(envOpt);
        if (!isDirectory(denoDir)) {
            throw new RuntimeException("Unable to find Deno directory," +
                    " please check that '" + DENO_HOME + "' variable is set correctly");
        }
        Path denoExe = denoDir.resolve("deno.exe");
        if (!Files.exists(denoExe)) {
            throw new RuntimeException("Unable to find Deno executable, path: [" + denoExe.toAbsolutePath() + "]" +
                    " please check that '" + DENO_HOME + "' variable set correctly");
        }
        return denoExe;
    }
}
