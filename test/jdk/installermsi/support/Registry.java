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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static support.Format.formatCommandLine;

public class Registry {

    public static final String REGISTRY_ENV_PATH = "HKLM\\System\\CurrentControlSet\\Control\\Session Manager\\Environment";

    public static Optional<String> queryRegistry(String path, String key) throws Exception {
        ArrayList<String> cline = new ArrayList<>(Arrays.asList(
                System.getenv("WINDIR") + "/system32/reg.exe",
                "query",
                path
        ));
        if (!key.isEmpty()) {
            cline.add("/v");
            cline.add(key);
        } else {
            cline.add("/ve");
        }

        System.out.println("Spawning reg process, command line: [" + formatCommandLine(cline) + "]");
        Path output = Paths.get("reg_out.txt");
        int code = new ProcessBuilder(cline)
                .inheritIO()
                .redirectOutput(output.toFile())
                .start()
                .waitFor();

        if (1 == code) {
            return Optional.empty();
        }

        if (0 != code) {
            throw new Exception("Reg query failure, code: [" + code + "]");
        }

        List<String> linesAll = Files.readAllLines(output, UTF_8);
        List<String> lines = new ArrayList<String>();
        for (String li : linesAll) {
            if (!li.isEmpty()) {
                lines.add(li);
            }
        }
        if (2 != lines.size()) {
            throw new Exception("Reg query invalid output, entries: [" + lines + "]");
        }

        String[] parts = lines.get(1).split(" {4}");

        String res = parts[parts.length - 1];

        if ("REG_SZ".equals(res)) {
            return Optional.of("");
        }

        return Optional.of(res);
    }
}
