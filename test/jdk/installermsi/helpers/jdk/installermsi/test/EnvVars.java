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

import java.nio.file.Path;

import static jdk.installermsi.test.Registry.SYSTEM_ENVIRONMENT_REGKEY;
import static jdk.installermsi.test.Registry.assertRegistryValueEquals;
import static jdk.jpackage.test.TKit.*;
import static jdk.jpackage.test.WindowsHelper.queryRegistryValue;

public class EnvVars {

    public static void assertEnvJavaHome(Path installed) {
        assertRegistryValueEquals(SYSTEM_ENVIRONMENT_REGKEY, "JAVA_HOME",
                installed.toAbsolutePath().toString() + "\\");
    }

    public static void assertNoEnvJavaHome(Path installed) {
        String javaHomeVar = queryRegistryValue(SYSTEM_ENVIRONMENT_REGKEY, "JAVA_HOME");
        if (null != javaHomeVar) {
            assertNotEquals(installed.toAbsolutePath().toString() + "\\", javaHomeVar, "JAVA_HOME");
        }
    }

    public static void assertEnvPath(Path installed) {
        String pathVar = queryRegistryValue(SYSTEM_ENVIRONMENT_REGKEY, "PATH");
        assertNotNull(pathVar, "PATH");
        assertTrue(pathVar.endsWith(installed.resolve("bin").toAbsolutePath().toString()), "PATH");
    }

    public static void assertNoEnvPath(Path installed) {
        String pathVar = queryRegistryValue(SYSTEM_ENVIRONMENT_REGKEY, "PATH");
        if (null != pathVar) {
            assertFalse(pathVar.endsWith(installed.resolve("bin").toAbsolutePath().toString()), "PATH");
        }
    }

    public static void assertEnvVendorJavaHome(Path installed) {
        assertRegistryValueEquals(SYSTEM_ENVIRONMENT_REGKEY, "VENDOR_JAVA_HOME",
                installed.toAbsolutePath().toString() + "\\");
    }

}
