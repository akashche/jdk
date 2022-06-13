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

import jdk.jpackage.test.Executor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static jdk.jpackage.test.TKit.*;
import static jdk.jpackage.test.WindowsHelper.runMsiexecWithRetries;

public class Install {

    private static final String ADMINISTRATORS_GROUP_SID = "S-1-5-114";

    private static final Path WINDIR = Path.of(System.getenv("WINDIR")).normalize().toAbsolutePath();
    private static final Path MSI_EXEC = WINDIR.resolve("system32/msiexec.exe");
    private static final Path WHOAMI = WINDIR.resolve("system32/whoami.exe");
    private static final Path CMD = WINDIR.resolve("system32/cmd.exe");
    private static final Path ICACLS = WINDIR.resolve("system32/icacls.exe");

    public static Path install(Path msi, String... options) {
        Path installed = createUniqueFileName("installed").normalize();
        Path installBat = createUniqueFileName("install.bat");

        ArrayList<String> cmdLine = new ArrayList<>(Arrays.asList(
                MSI_EXEC.normalize().toAbsolutePath().toString(),
                "/q",
                "/i",
                msi.normalize().toAbsolutePath().toString(),
                "/norestart",
                "/l*v",
                "install.log",
                "INSTALLDIR=\"" + installed.toAbsolutePath().toString() + "\""
        ));
        cmdLine.addAll(Arrays.asList(options));

        // Put msiexec in .bat file because can't pass value of INSTALLDIR
        // property containing spaces through ProcessBuilder properly.
        createTextFile(installBat, List.of(String.join(" ", cmdLine)));
        Executor ex = Executor.of(
                CMD.toString(),
                "/c",
                installBat.normalize().toAbsolutePath().toString());
        runMsiexecWithRetries(ex);
        return installed;
    }

    public static void uninstall(Path msi) {
        Executor ex = Executor.of(
                MSI_EXEC.toString(),
                "/q",
                "/x",
                msi.normalize().toAbsolutePath().toString(),
                "/l*v",
                "uninstall.log");
        runMsiexecWithRetries(ex);
    }

    public static Path unpack(Path msi) {
        Path unpacked = createUniqueFileName("unpacked").normalize();
        Path unpackBat = createUniqueFileName("unpack.bat");

        // set folder permissions to allow msiexec unpack msi bundle
        try {
            createDirectories(unpacked);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Executor.of(
                ICACLS.toString(),
                unpacked.toAbsolutePath().toString(),
                "/inheritance:e",
                "/grant",
                "Users:M")
                .execute(0);

        // put msiexec in .bat file because can't pass value of TARGETDIR
        // property containing spaces through ProcessBuilder properly
        createTextFile(unpackBat, List.of(
                String.join(" ",
                        MSI_EXEC.normalize().toAbsolutePath().toString(),
                        "/q",
                        "/a",
                        msi.normalize().toAbsolutePath().toString(),
                        "/l*v",
                        "unpack.log",
                        "TARGETDIR=\"" + unpacked.toAbsolutePath().toString() + "\"")));
        Executor ex = Executor.of(
                CMD.toString(),
                "/c",
                unpackBat.normalize().toAbsolutePath().toString());
        runMsiexecWithRetries(ex);
        return unpacked.resolve("openjdk");
    }

    public static boolean userCannotInstallMsiPackages() {
        // note: the check here maybe more precise, admin group membership
        // by itself does not guarantee installation success
        if(memberOfAdministratorsGroup()) {
           return false;
        }
        System.err.println("WARNING: OS user does not have enough permissions" +
                " to install MSI packages, test run skipped.");
        return true;
    }

    public static boolean memberOfAdministratorsGroup() {
        List<String> lines = Executor.of(WHOAMI.toString(), "/groups").executeAndGetOutput();
        for (String line : lines) {
            if (line.indexOf(ADMINISTRATORS_GROUP_SID) > 0) {
                return true;
            }
        }
        return false;
    }
}
