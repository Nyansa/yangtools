/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin.it;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import com.google.common.base.Joiner;
import com.google.common.io.Resources;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.Test;

public class YangToSourcesPluginTestIT {

    // TODO Test yang files in transitive dependencies

    @Test
    public void testYangRootNotExist() throws Exception {
        setUp("test-parent/YangRootNotExist/", false)
            .verifyTextInLog("[WARNING] yang-to-sources: YANG source directory");
    }

    @Test
    public void testCorrect() throws VerificationException, URISyntaxException, IOException {
        verifyCorrectLog(setUp("test-parent/Correct/", false));
    }

    @Test
    public void testAdditionalConfiguration() throws Exception {
        final Verifier vrf = setUp("test-parent/AdditionalConfig/", false);
        vrf.verifyTextInLog("[DEBUG] yang-to-sources: Additional configuration picked up for : "
                + "org.opendaylight.yangtools.yang2sources.spi.CodeGeneratorTestImpl: "
                + "{nm1=abcd=a.b.c.d, nm2=abcd2=a.b.c.d.2}");
        vrf.verifyTextInLog("[DEBUG] yang-to-sources: Additional configuration picked up for : "
                + "org.opendaylight.yangtools.yang2sources.spi.CodeGeneratorTestImpl: {c1=config}");
        vrf.verifyTextInLog(File.separator + "files marked as resources: META-INF/yang");
        vrf.verifyTextInLog(Joiner.on(File.separator).join(Arrays.asList("target", "generated-sources", "spi"))
                + " marked as resources for generator: org.opendaylight.yangtools.yang2sources.spi."
                + "CodeGeneratorTestImpl");
    }

    @Test
    public void testMissingYangInDep() throws Exception {
        try {
            setUp("test-parent/MissingYangInDep/", false);
            fail("Verification exception should have been thrown");
        } catch (VerificationException e) {
            assertVerificationException(e, "org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException: "
                    + "Imported module [unknownDep] was not found.");
        }
    }

    static void verifyCorrectLog(final Verifier vrf) throws VerificationException {
        vrf.verifyErrorFreeLog();
        vrf.verifyTextInLog("[INFO] yang-to-sources: YANG files parsed from");
        vrf.verifyTextInLog("[INFO] yang-to-sources: Code generator instantiated "
                + "from org.opendaylight.yangtools.yang2sources.spi.CodeGeneratorTestImpl");
        vrf.verifyTextInLog("[INFO] yang-to-sources: Sources generated by "
                + "org.opendaylight.yangtools.yang2sources.spi.CodeGeneratorTestImpl: null");
    }

    @Test
    public void testNoGenerators() throws Exception {
        Verifier vrf = setUp("test-parent/NoGenerators/", false);
        vrf.verifyErrorFreeLog();
        vrf.verifyTextInLog("[WARNING] yang-to-sources: No code generators provided");
    }

    @Test
    public void testInvalidVersion() throws Exception {
        Verifier vrf = setUp("test-parent/InvalidVersion/", false);
        vrf.verifyErrorFreeLog();
        vrf.verifyTextInLog("[WARNING] yang-to-sources: Dependency resolution conflict:");
    }

    @Test
    public void testUnknownGenerator() throws Exception {
        Verifier vrf = setUp("test-parent/UnknownGenerator/", true);
        vrf.verifyTextInLog("[ERROR] yang-to-sources: Unable to generate sources with unknown generator");
        vrf.verifyTextInLog("java.lang.ClassNotFoundException: unknown");
        vrf.verifyTextInLog("[INFO] yang-to-sources: Code generator instantiated from "
                + "org.opendaylight.yangtools.yang2sources.spi.CodeGeneratorTestImpl");
        vrf.verifyTextInLog("[INFO] yang-to-sources: Sources generated by org.opendaylight.yangtools.yang2sources.spi."
                + "CodeGeneratorTestImpl: null");
        vrf.verifyTextInLog("[ERROR] yang-to-sources: One or more code generators failed, including failed list"
                + "(generatorClass=exception) {unknown=java.lang.ClassNotFoundException}");
    }

    @Test
    public void testNoYangFiles() throws Exception {
        setUp("test-parent/NoYangFiles/", false).verifyTextInLog("[INFO] yang-to-sources: No input files found");
    }

    static void assertVerificationException(final VerificationException ex, final String string) {
        assertThat(ex.getMessage(), containsString(string));
    }

    static Verifier setUp(final String project, final boolean ignoreF)
            throws VerificationException, URISyntaxException, IOException {
        final URL path = YangToSourcesPluginTestIT.class.getResource("/" + project + "pom.xml");
        final Verifier verifier = new Verifier(new File(path.toURI()).getParent());
        if (ignoreF) {
            verifier.addCliOption("-fn");
        }

        final Optional<String> maybeSettings = getEffectiveSettingsXML();
        if (maybeSettings.isPresent()) {
            verifier.addCliOption("-gs");
            verifier.addCliOption(maybeSettings.get());
        }
        verifier.setMavenDebug(true);
        verifier.executeGoal("generate-sources");
        return verifier;
    }

    @Test
    public void testNoOutputDir() throws Exception {
        verifyCorrectLog(YangToSourcesPluginTestIT.setUp("test-parent/NoOutputDir/", false));
    }

    @Test
    public void testFindResourceOnCp() throws Exception {
        Verifier v1 = setUp("test-parent/GenerateTest1/", false);
        v1.executeGoal("clean");
        v1.executeGoal("package");

        String buildDir = getMavenBuildDirectory(v1);
        v1.assertFilePresent(buildDir + "/classes/META-INF/yang/testfile1.yang");
        v1.assertFilePresent(buildDir + "/classes/META-INF/yang/testfile2.yang");
        v1.assertFilePresent(buildDir + "/classes/META-INF/yang/testfile3.yang");

        Verifier v2 = setUp("test-parent/GenerateTest2/", false);
        v2.executeGoal("clean");
        v2.executeGoal("package");

        buildDir = getMavenBuildDirectory(v2);
        v2.assertFilePresent(buildDir + "/classes/META-INF/yang/private.yang");
        v2.assertFileNotPresent(buildDir + "/classes/META-INF/yang/testfile1.yang");
        v2.assertFileNotPresent(buildDir + "/classes/META-INF/yang/testfile2.yang");
        v2.assertFileNotPresent(buildDir + "/classes/META-INF/yang/testfile3.yang");
    }

    private static String getMavenBuildDirectory(final Verifier verifier) throws IOException {
        final Properties sp = new Properties();
        final Path path = new File(verifier.getBasedir() + "/it-project.properties").toPath();
        try (InputStream is = Files.newInputStream(path)) {
            sp.load(is);
        }
        return sp.getProperty("target.dir");
    }

    private static Optional<String> getEffectiveSettingsXML() throws URISyntaxException, VerificationException,
            IOException {
        final URL path = Resources.getResource(YangToSourcesPluginTestIT.class, "/test-parent/pom.xml");
        final File buildDir = new File(path.toURI()).getParentFile().getParentFile().getParentFile();
        final File effectiveSettingsXML = new File(buildDir, "effective-settings.xml");
        if (effectiveSettingsXML.exists()) {
            return Optional.of(effectiveSettingsXML.getAbsolutePath());
        }

        fail(effectiveSettingsXML.getAbsolutePath());
        return Optional.empty();
    }
}
