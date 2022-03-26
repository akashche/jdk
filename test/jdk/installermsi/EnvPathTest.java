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

import static support.Assert.*;
import static support.Install.install;
import static support.Registry.REGISTRY_ENV_PATH;
import static support.Registry.queryRegistry;
import static support.Uninstall.uninstall;

/**
 * @test
 * @requires (os.family == "windows")
 */

public class EnvPathTest {

    public static void main(String[] args) throws Exception {
        install("ADDLOCAL=jdk_env_path");
        try {

            String scratchDir = Path.of("").toAbsolutePath().toString();
            String pathVar = queryRegistry(REGISTRY_ENV_PATH, "PATH").get();
            assertThat(pathVar, pathVar.endsWith(scratchDir + "\\installed\\bin"));
            assertNoRegKey(REGISTRY_ENV_PATH, "JAVA_HOME");

            assertPath("installed/bin/java.exe");
            assertPath("installed/bin/server/jvm.dll");
            assertPath("installed/lib/modules");

        } finally {
            uninstall();
        }
    }
}
