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
import java.util.Optional;

import static support.Registry.REGISTRY_ENV_PATH;
import static support.Registry.queryRegistry;

public class FindPaths {

    private static final String INSTALLERMSI_TEST_BUILD_ROOT = "INSTALLERMSI_TEST_BUILD_ROOT";
    private static final String INSTALLERMSI_TEST_SRC_ROOT = "INSTALLERMSI_TEST_SRC_ROOT";
    private static final String INSTALLERMSI_JAXB_EXTEND_LIBS_DIR = "INSTALLERMSI_JAXB_EXTEND_LIBS_DIR";

    public static Optional<Path> findInstaller() {
        Path buildRoot = findBuildRoot();
        Path msi = buildRoot.resolve("images/installermsi/jdk.msi");
        return Files.exists(msi) ? Optional.of(msi) : Optional.empty();
    }

    public static Path findJdkImageDir() {
        Path buildRoot = findBuildRoot();
        Path jdk = buildRoot.resolve("images/jdk");
        if (!Files.exists(jdk)) {
            throw new RuntimeException("Cannot find jdk image");
        }
        return jdk;
    }

    public static Path findSrcRoot() {
        // expect build root under src root
        Path buildRoot = findBuildRoot();
        Path parent = buildRoot.getParent();
        do {
            Path check = parent.resolve("make/data/installermsi");
            if (Files.exists(check)) {
                return parent;
            }
            parent = parent.getParent();
        } while (null != parent);

        // custom build root
        String srcRootVar = System.getenv(INSTALLERMSI_TEST_SRC_ROOT);
        if (null == srcRootVar) {
            throw new RuntimeException("Unable to determine src root path," +
                    " please specify '-e:" + INSTALLERMSI_TEST_SRC_ROOT + "=c:/path/to/jdksrc'" +
                    " option to JTreg command");
        }
        Path srcRoot = Path.of(srcRootVar);
        Path check = srcRoot.resolve("make/data/installermsi");
        if (!Files.exists(check)) {
            throw new RuntimeException("Invalid '" + INSTALLERMSI_TEST_SRC_ROOT + "' variable specified");
        }
        return srcRoot;
    }

    public static Path findBuildRoot() {
        // expect running with make run-test
        Path cwd = Path.of(".").toAbsolutePath();
        Path testSupport = cwd.getParent();
        while (null != testSupport && null != testSupport.getFileName() &&
                !"test-support".equalsIgnoreCase(testSupport.getFileName().toString())) {
            testSupport = testSupport.getParent();
        }
        if (null != testSupport) {
            Path parent = testSupport.getParent();
            if (null != parent) {
                return parent;
            }
        }

        // not running with make run-test
        String buildRootVar = System.getenv(INSTALLERMSI_TEST_BUILD_ROOT);
        if (null == buildRootVar) {
            throw new RuntimeException("Unable to determine build root path," +
                    " please specify '-e:" + INSTALLERMSI_TEST_BUILD_ROOT + "=c:/path/to/jdk/build/root'" +
                    " option to JTreg command");
        }
        return Path.of(buildRootVar);
    }

    public static Path findWixDir() throws Exception {
        Optional<String> envOpt = queryRegistry(REGISTRY_ENV_PATH, "WIX");
        if (envOpt.isEmpty()) {
            throw new RuntimeException("Unable to find WiX directory," +
                    " 'WIX' environment variable is not set");
        }
        Path wixDir = Path.of(envOpt.get());
        if (!Files.exists(wixDir)) {
            throw new RuntimeException("Unable to find WiX directory," +
                    " please check that 'WIX' environment variable is set correctly");
        }
        return wixDir;
    }

    public static Path findDenoExe() throws Exception {
        Optional<String> envOpt = queryRegistry(REGISTRY_ENV_PATH, "DENO_HOME");
        if (envOpt.isEmpty()) {
            throw new RuntimeException("Unable to find Deno directory," +
                    " 'DENO_HOME' variable is not set");
        }
        Path denoDir = Path.of(envOpt.get());
        if (!Files.exists(denoDir)) {
            throw new RuntimeException("Unable to find Deno directory," +
                    " please check that 'DENO_HOME' variable set correctly");
        }
        Path denoExe = denoDir.resolve("deno.exe");
        if (!Files.exists(denoExe)) {
            throw new RuntimeException("Unable to find Deno executable, path: [" + denoExe.toAbsolutePath() + "]" +
                    " please check that 'DENO_HOME' variable set correctly");
        }
        return denoExe;
    }

    public static Path findJaxbLibsDir() throws Exception {
        Optional<String> envOpt = queryRegistry(REGISTRY_ENV_PATH, INSTALLERMSI_JAXB_EXTEND_LIBS_DIR);
        if (envOpt.isEmpty()) {
            throw new RuntimeException("Unable to find JAXB libraries," +
                    " '" + INSTALLERMSI_JAXB_EXTEND_LIBS_DIR + "' variable is not set");
        }
        Path dir = Path.of(envOpt.get());
        if (!Files.exists(dir)) {
            throw new RuntimeException("Unable to find JAXB libraries," +
                    " please check that '" + INSTALLERMSI_JAXB_EXTEND_LIBS_DIR + "' variable set correctly");
        }
        return dir;
    }
}
