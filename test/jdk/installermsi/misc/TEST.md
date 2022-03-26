Testing the installer
---------------------

A set of JTreg tests in included with the implementation in `test/jdk/installermsi` directory.

These tests perform actual installation and uninstallation using system `msiexec` utility, thus
they can only be run under an OS user with `Administrator` privileges.

`InstalledFilesTest.java` can be used to check that the set of files included with installer matches the output of `make images` target.

`Extend*` tests, that cover the extending of a vanilla installer, perform a transformation
of `jdk.xml` descriptor and create a new installer package that is then being installed/uninstalled.
These tests require the following tools and resources to be specified using system environment variables:

 - `WIX`: path to WiX toolset directory, required by all `Extend*` tests
 - `DENO_HOME`: path to the directory where [Deno JavaScript runtime](https://deno.land/) resides, required by `ExtendScriptTest.java`
 - `INSTALLERMSI_JAXB_EXTEND_LIBS_DIR`: path to the directory with the set of JAXB libraries, required by `ExtendJaxbTest.java`:
   - `msiextend-jaxb-1.0.jar` - generated JAXB classes, [download](https://github.com/akashche/msiextend-jaxb/releases/tag/1.0)
   - `jaxb-api-2.3.1.jar` - [download](https://repo1.maven.org/maven2/javax/xml/bind/jaxb-api/2.3.1/)
   - `jaxb-impl-2.3.1.jar` - [download](https://repo1.maven.org/maven2/com/sun/xml/bind/jaxb-impl/2.3.1/)
   - `istack-commons-runtime-4.0.1.jar` - [download](https://repo1.maven.org/maven2/com/sun/istack/istack-commons-runtime/4.0.1/)
   - `activation-1.1.1.jar` - [download](https://repo1.maven.org/maven2/javax/activation/activation/1.1.1/)

Usage:

```
make run-test TEST=jdk/installermsi JTREG="JOBS=1"
...
Running test 'jtreg:test/jdk/installermsi'
Passed: installermsi/AllFeaturesTest.java
Passed: installermsi/DefaultFeaturesTest.java
Passed: installermsi/EnvJavaHomeTest.java
Passed: installermsi/EnvPathTest.java
Passed: installermsi/ExtendDomTest.java
Passed: installermsi/ExtendJaxbTest.java
Passed: installermsi/ExtendScriptTest.java
Passed: installermsi/InstalledFilesTest.java
Passed: installermsi/RegistryJarTest.java
Passed: installermsi/RegistryRuntimeTest.java
Test results: passed: 10
```

