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
import java.nio.file.Paths;
import java.util.Optional;

import static support.Registry.queryRegistry;

public class Assert {

    public static void assertThat(String message, boolean condition) {
        if (!condition) {
            throw new AssertionError("Check failed, message: [" + message + "]");
        }
    }

    public static void assertFalse(String message, boolean condition) {
        assertThat(message, !condition);
    }

    public static void assertPath(String path) {
        assertThat(path, Files.exists(Paths.get(path)));
    }

    public static void assertNoPath(String path) {
        assertFalse(path, Files.exists(Paths.get(path)));
    }

    public static void assertEquals(String message, String expected, String actual) {
        if (!(null == expected || null == actual || expected.equals(actual))) {
            throw new AssertionError("Check failed, expected: [" + expected + "]," +
                    " actual: [" + actual + "], message: [" + message + "]");
        }
    }

    public static void assertRegKey(String path, String key, String expected) throws Exception {
        Optional<String> opt = queryRegistry(path, key);
        String msg = path + ":" + key;
        assertThat(msg, opt.isPresent());
        assertEquals(msg, expected, opt.get());
    }

    public static void assertNoRegKey(String path, String key) throws Exception {
        Optional<String> opt = queryRegistry(path, key);
        assertFalse(path + ":" + key, opt.isPresent());
    }
}
