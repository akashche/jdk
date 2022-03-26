Installer implementation
------------------------

Installer is created by running utilities from [WiX toolset](https://wixtoolset.org/) providing an XML descriptor as an input.
XML descriptor (and additional resources) is stored in-tree in `jdk/make/data/installermsi` directory.

Build system configure changes
------------------------------

In `make/autoconf/toolchain_microsoft.m4` WiX detection logic is added under `TOOLCHAIN_SETUP_INSTALLERMSI_WIX` name.
It is called from `make/autoconf/lib-std.m4` after the call to `TOOLCHAIN_SETUP_VS_RUNTIME_DLLS`.
It supports specifying the path to WiX directory using `--with-wix=` flag and, alternatively, the probing of
`WIX` environment variable that is set by WiX installer.

`candle.exe` and `light.exe` WiX tools are required, they have different paths in a WiX installer and in a WiX
ZIP bundle (no `bin` directory in ZIP bundle), detection logic tries both variants when looking for them.

Resulting paths are stored in `INSTALLERMSI_WIX_CANDLE` and `INSTALLERMSI_WIX_LIGHT` variables.

Overall in `make/autoconf/spec.gmk.in` the following variables were added:

```
INSTALLERMSI_IMAGE_SUBDIR := installermsi
INSTALLERMSI_IMAGE_DIR := $(IMAGES_OUTPUTDIR)/$(INSTALLERMSI_IMAGE_SUBDIR)
INSTALLERMSI_WIX_CANDLE := @INSTALLERMSI_WIX_CANDLE@
INSTALLERMSI_WIX_LIGHT := @INSTALLERMSI_WIX_LIGHT@
```

Build system make changes
-------------------------

`SetVersion.java` build tool, that is used to replace the version numbers in jdk.xml, is added
to `make/jdk/src/classes/build/tools/installermsi/SetVersion.java` directory. It is also
registered in `make/ToolsJdk.gmk` under the name `TOOL_INSTALLERMSI_SET_VERSION`.

In `make/Install.gmk` a dependency to `ToolsJdk.gmk` is added to be able to access `TOOL_INSTALLERMSI_SET_VERSION`.
`installer-msi` target is also added there (next to the existing `install` target), this target
copies installer resources from `make/data/installermsi/resources` to `<build_root>/images/installermsi/resources`,
then runs `SetVersion.java` on `jdk.xml` with resulting `jdk.wxs` being written into `<build_root>/images/installermsi`
directory. Then it runs WiX tools that take `jdk.wxs` as an input and produce `jdk.msi` output.

In `make/Main.gmk` new target `installer-msi` is registered as a top-level target.

In-tree XML descriptor
----------------------

Resulting files from `make images` target are included with installer. There is no auto-discovery for these files,
their paths need to be updated manually in `jdk.xml` when `make images` output file set is changed. 
`InstalledFilesTest.java` JTreg test can be used to check this set of files.

XML descriptor for WiX contains the following details:

 - file names and paths to every file to be included with installer
 - system integration details: Windows registry keys and environment variables (like `JAVA_HOME`)
 - set of "installation features", each feature containing a set of files and/or system integration entries
 - versioning information that is used during the update process

It was observed that the set of output files from `make images` target is relatively stable for a single jdk version.
This allows to prepare an "almost static" XML descriptor that contains predefined details of every jdk file to include,
the set of system integration entries and the set of installation features. The only thing that needs to be
updated in the descriptor during the build is version numbers.

When `make installer-msi` target is run, the "almost static" XML descriptor is loaded from `make/data/installermsi` directory,
version number placeholders in it are replaced with current build version and resulting ready-to-use descriptor
is written to `<build_root>/images/installermsi` directory.
