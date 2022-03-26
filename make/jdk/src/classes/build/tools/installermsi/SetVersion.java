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

package build.tools.installermsi;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * This tool reads the installer XML descriptor, replaces version number placeholders
 * and writes modified XML at the specified path.
 */
public class SetVersion {

    private static final String PLACEHOLDER_VERSION_FEATURE = "PLACEHOLDER_VERSION_FEATURE";
    private static final String PLACEHOLDER_VERSION_NUMBER = "PLACEHOLDER_VERSION_NUMBER";
    private static final String PLACEHOLDER_VERSION_NUMBER_FOUR_POSITIONS = "PLACEHOLDER_VERSION_NUMBER_FOUR_POSITIONS";

    public static void main(String[] args) throws Exception {
        if (5 != args.length) {
            System.err.println("Usage: java SetVersion.java input.xml output.xml VERSION_FEATURE VERSION_NUMBER VERSION_NUMBER_FOUR_POSITIONS");
            System.exit(1);
        }
        Path srcXml = Path.of(args[0]);
        if (!Files.exists(srcXml)) {
            System.err.println("Error: specified input file does not exist, path: [" + srcXml + "]");
            System.exit(1);
        }
        Path destXml = Path.of(args[1]);
        Document doc = readXml(srcXml);
        setVersion(doc, args[2], args[3], args[4]);
        writeXml(destXml, doc);
    }

    private static void setVersion(Document doc, String versionFeature, String versionNumber, String versionNumberFourPositions) throws Exception {
        Node product = findProductNode(doc);

        Node productAttrName = product.getAttributes().getNamedItem("Name");
        productAttrName.setNodeValue(productAttrName.getNodeValue().replace(PLACEHOLDER_VERSION_NUMBER, versionNumber));

        Node productAttrVersion = product.getAttributes().getNamedItem("Version");
        productAttrVersion.setNodeValue(productAttrVersion.getNodeValue().replace(PLACEHOLDER_VERSION_NUMBER_FOUR_POSITIONS, versionNumberFourPositions));

        Node rvcv = findRegistryValueCurrentVersion(product);
        Node rvcvAttrValue = rvcv.getAttributes().getNamedItem("Value");
        rvcvAttrValue.setNodeValue(rvcvAttrValue.getNodeValue().replace(PLACEHOLDER_VERSION_NUMBER, versionNumber));
        Node rkjh = findRegistryKeyJavaHome(product);
        Node rkjhKey = rkjh.getAttributes().getNamedItem("Key");
        rkjhKey.setNodeValue(rkjhKey.getNodeValue().replace(PLACEHOLDER_VERSION_NUMBER, versionNumber));

        Node installDir = findInstallDirNode(product);
        Node installDirAttrName = installDir.getAttributes().getNamedItem("Name");
        installDirAttrName.setNodeValue(installDirAttrName.getNodeValue().replace(PLACEHOLDER_VERSION_FEATURE, versionFeature));
    }

    private static Node findProductNode(Document doc) {
        for (int i1 = 0; i1 < doc.getChildNodes().getLength(); i1++) {
            Node node = doc.getChildNodes().item(i1);
            if ("Wix".equals(node.getNodeName())) {
                for (int i2 = 0; i2 < node.getChildNodes().getLength(); i2++) {
                    Node product = node.getChildNodes().item(i2);
                    if ("Product".equals(product.getNodeName())) {
                        return product;
                    }
                }
            }
        }
        throw new IllegalStateException("Product node not found");
    }

    private static Node findInstallDirNode(Node product) {
        Node targetDir = findChildWithId(product, "TARGETDIR");
        Node pf64 = findChildWithId(targetDir, "ProgramFiles64Folder");
        Node vendorDir = findChildWithId(pf64, "dir_vendor");
        return findChildWithId(vendorDir, "INSTALLDIR");
    }

    private static Node findRegistryValueCurrentVersion(Node product) {
        Node compRegistryRuntime = findChildWithId(product, "comp_registry_runtime_current_version");
        Node regKey = findChildWithId(compRegistryRuntime, "registry_runtime_current_version");
        for (int i = 0; i < regKey.getChildNodes().getLength(); i++) {
            Node regVal = regKey.getChildNodes().item(i);
            if ("RegistryValue".equals(regVal.getNodeName()) &&
                    "CurrentVersion".equals(regVal.getAttributes().getNamedItem("Name").getNodeValue())) {
                return regVal;
            }
        }
        throw new IllegalStateException("Registry value current version not found");
    }

    private static Node findRegistryKeyJavaHome(Node product) {
        Node compRegistryRuntime = findChildWithId(product, "comp_registry_runtime_java_home");
        return findChildWithId(compRegistryRuntime, "registry_runtime_java_home");
    }

    private static Node findChildWithId(Node parent, String id) {
        for (int i = 0; i < parent.getChildNodes().getLength(); i++) {
            Node child = parent.getChildNodes().item(i);
            NamedNodeMap attrs = child.getAttributes();
            if (null != attrs && null != attrs.getNamedItem("Id") &&
                    id.equals(attrs.getNamedItem("Id").getNodeValue())) {
                return child;
            }
        }
        throw new IllegalStateException("Child node not found, id: [" + id + "]");
    }

    private static Document readXml(Path path) throws Exception {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        return docBuilder.parse(path.toFile());
    }

    private static void writeXml(Path path, Document doc) throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(path.toFile());
        transformer.transform(source, result);
    }
}
