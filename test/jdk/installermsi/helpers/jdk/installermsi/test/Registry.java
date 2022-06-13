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

import jdk.jpackage.test.TKit;

import java.nio.file.Path;

import static jdk.jpackage.test.TKit.*;
import static jdk.jpackage.test.WindowsHelper.queryRegistryValue;

public class Registry {

    public static final String SYSTEM_ENVIRONMENT_REGKEY = "HKLM\\System\\CurrentControlSet\\Control\\Session Manager\\Environment";

    public static void assertRegistryValueEquals(String path, String key, String expected) {
        String val = queryRegistryValue(path, key);
        String msg = path + ":" + key;
        assertNotNull(val, msg);
        assertEquals(expected, val, msg);
    }

    public static void assertRegistryValueAbsent(String path, String key) {
        String val = queryRegistryValue(path, key);
        TKit.assertNull(val, path + ":" + key);
    }

    public static void assertRegistryRuntime(Path installed) {
        String curVer = queryRegistryValue("HKLM\\Software\\JavaSoft\\JDK\\", "CurrentVersion");
        assertNotNull(curVer, "current version");
        String[] curVerParts = curVer.split("\\.");
        assertTrue(curVerParts.length >= 1 && curVerParts.length <= 4, "current version parts count");
        for (String part : curVerParts) {
            assertTrue(part.matches("\\d+"), "current version part");
        }
        assertRegistryValueEquals("HKLM\\Software\\JavaSoft\\JDK\\" + curVer,
                "JavaHome", installed.toAbsolutePath().toString() + "\\");
    }

    public static void assertNoRegistryRuntime(Path installed) {
        String curVer = queryRegistryValue("HKLM\\Software\\JavaSoft\\JDK\\", "CurrentVersion");
        if (null != curVer) {
            String javaHome = queryRegistryValue("HKLM\\Software\\JavaSoft\\JDK\\" + curVer, "JavaHome");
            if (null != javaHome) {
                assertNotEquals(installed.toAbsolutePath().toString() + "\\", javaHome, "JavaHome");
            }
        }
    }

    public static void assertRegistryJar(Path installed) {
        assertRegistryValueEquals("HKLM\\Software\\Classes\\.jar",
                "", "JARFile");
        assertRegistryValueEquals("HKLM\\Software\\Classes\\.jar",
                "Content Type", "application/java-archive");
        assertRegistryValueEquals("HKLM\\Software\\Classes\\JARFile",
                "", "JAR File");
        assertRegistryValueEquals("HKLM\\Software\\Classes\\JARFile",
                "EditFlags", "0x10000");
        assertRegistryValueEquals("HKLM\\Software\\Classes\\JARFile\\Shell\\Open",
                "", "&Launch with OpenJDK");
        String javaw = installed.resolve("bin/javaw.exe").toAbsolutePath().toString();
        assertRegistryValueEquals("HKLM\\Software\\Classes\\JARFile\\Shell\\Open\\Command",
                "", "\"" + javaw + "\" -jar \"%1\" %*");
    }

    public static void assertNoRegistryJar(Path installed) {
        String cmd = queryRegistryValue("HKLM\\Software\\Classes\\JARFile\\Shell\\Open\\Command", "");
        if (null != cmd) {
            String javaw = installed.resolve("bin/javaw.exe").toAbsolutePath().toString();
            assertNotEquals("\"" + javaw + "\" -jar \"%1\" %*", cmd, "Command");
        }
    }

}
