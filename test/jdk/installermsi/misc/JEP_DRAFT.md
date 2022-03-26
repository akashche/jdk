JEP draft: MSI Installer for Windows
====================================
 
Summary
-------

Support building Windows MSI installer with "make installer-msi".
 
Goals
-----
 
 - allow to build a basic MSI installer from vanilla upstream repository without requiring any additional
 scripts or resources (MSI build tools must be present)
 - provide documentation and code examples for customizing and extending (with additional selectable features)
the installer in a way, that OpenJDK vendors can create their own enhanced installers building on top of vanilla one
 
Non-goals
---------
 
 - no additional files/components (like auto-updater) besides "make images" files
 - no installer customization with autoconf flags
 - no integration with jpackage tools/scripts
 - no support for other installation formats/tools (NSIS, Inno, install4j, IzPack etc)
 - no support for cross-building from other OS - installer can only be built on Windows
 - no support for Windows ARM64 - only x86_64 arch is supported in initial implementation
 - possible Linux RPMs or Mac bundles that can be added to jdk in future are not taken into account
 - no i18n translation resources - only en_us labels provided in-tree
 
Motivation
----------
 
 - MSI installers built with WiX toolset are a de-facto standard on Windows
 - casual OpenJDK builders may want to build a ready-to use installer
 - OpenJDK vendors may want to have a "base layer" ("make images" output) for their customized installers
 
Description
-----------
 
Installer is created by running utilities from WiX toolset, that take an XML descriptor as an input.
XML descriptor is stored in-tree in an "almost static" (templating restricted to only substituting version numbers) form.
It contains the paths to all jdk-image files an needs to be updated manually when the set of "make images" output files changes.
Default visual resources (banners, logos) are based on official OpenJDK Duke picture.

System integration
------------------

Vanilla installer provides the ability ro run ".jar" files from Windows Explorer either with a double-click or
from a context menu. This is implemented by setting a number of Windows Registry keys.

Environment variables "PATH" and "JAVA_HOME" also can be set by installer.
 
Extending approaches
--------------------

XML descriptor can be transformed by OpenJDK vendors to change the labels or
include additional files and selectable features.
 
Implementation includes documented extension examples in Java (using DOM or using JAXB) and in JavaScript.

Relation to prior JEPs
----------------------

JEP 392 Packaging Tool implements support for packaging Java application into MSI installers.
Proposed implementation is completely separate from JEP 392 because it covers the packaging of a JDK itself,
not the packaging of Java applications.

JEP draft JDK Packaging Guidelines (JDK-8278252) specify some details about the packaging of a JDK
on Windows. Installer implementation follows its guidelines on vendor directory naming ("OpenJDK" directory in vanilla)
and on the use of "VERSION_FEATURE" in JDK directory naming. It also intend to follow other relevant guidelines
(for example: the set of Windows Registry keys) that can be added to this JEP draft in future.
 
Testing
-------
 
While vanilla installer is not built by default, it must be always buildable.

Set of JTreg tests is included with the implementation, these tests cover the following areas:
 
 - vanilla installer (created with "make installer-msi"): installation/uninstallation with different
 sets of selected features, comparison of a set of installed files with a "make images" output
 - extended installers (created using extension examples): extended installers are created and then
 installed/uninstalled during the test
 
Dependencies
------------
 
To build the installer it is necessary to install a third-party WiX toolset.
This toolset is a widely used open-source project that is governed by .NET Foundation.
