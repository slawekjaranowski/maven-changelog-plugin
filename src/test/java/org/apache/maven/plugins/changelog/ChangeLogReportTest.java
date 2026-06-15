/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.maven.plugins.changelog;

import java.io.File;
import java.nio.file.Files;

import org.apache.maven.api.di.Provides;
import org.apache.maven.api.plugin.testing.InjectMojo;
import org.apache.maven.api.plugin.testing.MojoTest;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.changelog.stubs.FailedScmManagerStub;
import org.apache.maven.plugins.changelog.stubs.MavenProjectStub;
import org.apache.maven.plugins.changelog.stubs.ScmManagerStub;
import org.apache.maven.plugins.changelog.stubs.ScmManagerWithHostStub;
import org.apache.maven.project.MavenProject;
import org.apache.maven.scm.manager.ScmManager;
import org.codehaus.plexus.util.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.apache.maven.api.plugin.testing.MojoExtension.getBasedir;
import static org.apache.maven.api.plugin.testing.MojoExtension.getVariableValueFromObject;
import static org.apache.maven.api.plugin.testing.MojoExtension.setVariableValueToObject;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Edwin Punzalan
 */
@MojoTest
public class ChangeLogReportTest {
    private ScmManager scmManager = new ScmManagerStub();

    @Provides
    @SuppressWarnings("unused")
    private MavenProject providesMavenProject() {
        return new MavenProjectStub();
    }

    @Test
    void name() {}

    @Test
    @InjectMojo(goal = "changelog", pom = "src/test/plugin-configs/changelog/no-source-plugin-config.xml")
    public void testNoSource(ChangeLogReport mojo) throws Exception {
        //        File pluginXmlFile = new File(getBasedir(),
        // "src/test/plugin-configs/changelog/no-source-plugin-config.xml");

        //        ChangeLogReport mojo = (ChangeLogReport) lookupMojo("changelog", pluginXmlFile);

        //        assertNotNull("Mojo found.", mojo);

        setVariableValueToObject(mojo, "manager", scmManager);

        mojo.execute();

        File outputDir = getVariableValueFromObject(mojo, "outputDirectory");

        File outputHtml = new File(outputDir, "changelog.html");

        Assertions.assertTrue(outputHtml.exists(), outputHtml.getAbsolutePath() + " not generated!");

        Assertions.assertTrue(outputHtml.length() > 0, outputHtml.getAbsolutePath() + " is empty!");
    }

    @Test
    @InjectMojo(goal = "changelog", pom = "src/test/plugin-configs/changelog/min-plugin-config.xml")
    public void testMinConfig(ChangeLogReport mojo) throws Exception {
        //        executeMojo("min-plugin-config.xml");
        executeMojo(mojo);
    }

    @Test
    @InjectMojo(goal = "changelog", pom = "src/test/plugin-configs/changelog/min-plugin-config.xml")
    public void testFailedChangelog(ChangeLogReport mojo) throws Exception {
        scmManager = new FailedScmManagerStub();

        try {
            //            executeMojo("min-plugin-config.xml");
            executeMojo(mojo);
        } catch (MojoExecutionException e) {
            Assertions.assertEquals("Command failed.", e.getCause().getCause().getMessage(), "Test thrown exception");
        }
    }

    @Test
    @InjectMojo(goal = "changelog", pom = "src/test/plugin-configs/changelog/cached-plugin-config.xml")
    public void testUsageOfCachedXml(ChangeLogReport mojo) throws Exception {
        File cacheFile = new File(getBasedir(), "src/test/changelog-xml/min-changelog.xml");
        cacheFile.setLastModified(System.currentTimeMillis());

        setVariableValueToObject(mojo, "outputXML", cacheFile);

        //        executeMojo("cached-plugin-config.xml");
        executeMojo(mojo);
    }

    @Test
    @InjectMojo(goal = "changelog", pom = "src/test/plugin-configs/changelog/inv-type-plugin-config.xml")
    public void testTypeException(ChangeLogReport mojo) throws Exception {
        try {
            //            executeMojo("inv-type-plugin-config.xml");
            executeMojo(mojo);

            fail("Test exception on invalid type");
        } catch (MojoExecutionException e) {
            Assertions.assertTrue(
                    e.getCause().getMessage().startsWith("The type parameter has an invalid value: invalid."),
                    "Test thrown exception");
        }
    }

    @Test
    @InjectMojo(goal = "changelog", pom = "src/test/plugin-configs/changelog/tag-plugin-config.xml")
    public void testTagType(ChangeLogReport mojo) throws Exception {
        executeMojo(mojo);
        //        executeMojo("tag-plugin-config.xml");
    }

    @Test
    @InjectMojo(goal = "changelog", pom = "src/test/plugin-configs/changelog/tags-plugin-config.xml")
    public void testTagsType(ChangeLogReport mojo) throws Exception {
        //        executeMojo("tags-plugin-config.xml");
        executeMojo(mojo);
    }

    @Test
    @InjectMojo(goal = "changelog", pom = "src/test/plugin-configs/changelog/inv-date-plugin-config.xml")
    public void testDateException(ChangeLogReport mojo) throws Exception {
        try {
            //            executeMojo("inv-date-plugin-config.xml");
            executeMojo(mojo);
        } catch (MojoExecutionException e) {
            Assertions.assertTrue(
                    e.getCause().getCause().getMessage().startsWith("Please use this date pattern: "),
                    "Test thrown exception");
        }
    }

    @Test
    @InjectMojo(goal = "changelog", pom = "src/test/plugin-configs/changelog/date-plugin-config.xml")
    public void testDateType(ChangeLogReport mojo) throws Exception {
        //        executeMojo("date-plugin-config.xml");
        executeMojo(mojo);
    }

    @Test
    @InjectMojo(goal = "changelog", pom = "src/test/plugin-configs/changelog/dates-plugin-config.xml")
    public void testDatesType(ChangeLogReport mojo) throws Exception {
        //        executeMojo("dates-plugin-config.xml");
        executeMojo(mojo);
    }

    @Test
    @InjectMojo(goal = "changelog", pom = "src/test/plugin-configs/changelog/hosted-plugin-config.xml")
    public void testScmRepositoryWithHost(ChangeLogReport mojo) throws Exception {
        scmManager = new ScmManagerWithHostStub();

        //        executeMojo("hosted-plugin-config.xml");
        executeMojo(mojo);
    }

    @Test
    @InjectMojo(goal = "changelog", pom = "src/test/plugin-configs/changelog/hosted-with-settings-plugin-config.xml")
    public void testScmRepositoryWithHostFromSettings(ChangeLogReport mojo) throws Exception {
        scmManager = new ScmManagerWithHostStub();

        //        executeMojo("hosted-with-settings-plugin-config.xml");
        executeMojo(mojo);
    }

    @Test
    @InjectMojo(goal = "changelog", pom = "src/test/plugin-configs/changelog/no-scm-plugin-config.xml")
    public void testNoScmConnection(ChangeLogReport mojo) throws Exception {
        try {
            //            executeMojo("no-scm-plugin-config.xml");
            executeMojo(mojo);
        } catch (MojoExecutionException e) {
            Assertions.assertEquals(
                    "SCM Connection is not set.",
                    e.getCause().getCause().getCause().getMessage(),
                    "Test thrown exception");
        }
    }

    @Test
    @InjectMojo(
            goal = "changelog",
            pom = "src/test/plugin-configs/changelog/dont-display-file-and-rev-info-plugin-config.xml")
    public void testDontDisplayFileAndRevInfo(ChangeLogReport mojo) throws Exception {
        File cacheFile = new File(getBasedir(), "src/test/changelog-xml/min-changelog.xml");
        cacheFile.setLastModified(System.currentTimeMillis());

        //        String html = executeMojo("dont-display-file-and-rev-info-plugin-config.xml", true);
        setVariableValueToObject(mojo, "outputXML", cacheFile);
        String html = executeMojo(mojo, true);

        assertFalse(html.contains("file.extension"));
        assertFalse(html.contains("file2.extension"));
        assertFalse(html.contains(" v 1"));
        assertFalse(html.contains(" v 2"));
        assertFalse(html.contains(" v 3"));
        assertFalse(html.contains(" v 4"));
    }

    @Test
    @InjectMojo(
            goal = "changelog",
            pom = "src/test/plugin-configs/changelog/display-file-and-rev-info-explicitly-plugin-config.xml")
    public void testDisplayFileAndRevInfoExplicitly(ChangeLogReport mojo) throws Exception {
        File cacheFile = new File(getBasedir(), "src/test/changelog-xml/min-changelog.xml");
        cacheFile.setLastModified(System.currentTimeMillis());

        //        String html = executeMojo("display-file-and-rev-info-explicitly-plugin-config.xml", true);
        setVariableValueToObject(mojo, "outputXML", cacheFile);

        String html = executeMojo(mojo, true);

        Assertions.assertTrue(html.contains("file.extension"));
        Assertions.assertTrue(html.contains("file2.extension"));
        Assertions.assertTrue(html.contains("v 1"));
        Assertions.assertTrue(html.contains("v 2"));
        Assertions.assertTrue(html.contains("v 3"));
        Assertions.assertTrue(html.contains("v 4"));
    }

    private void executeMojo(ChangeLogReport mojo) throws Exception {
        executeMojo(mojo, false);
    }

    private String executeMojo(ChangeLogReport mojo, boolean withOutput) throws Exception {
        //        File pluginXmlFile = new File(getBasedir(), "src/test/plugin-configs/changelog/" + pluginXml);

        //        ChangeLogReport mojo = (ChangeLogReport) lookupMojo("changelog", pluginXmlFile);

        //        assertNotNull("Mojo found.", mojo);

        setVariableValueToObject(mojo, "manager", scmManager);

        // use current directory project as basedir
        setVariableValueToObject(mojo, "basedir", new File(getBasedir(), "src/main/java"));

        mojo.execute();

        File outputXML = getVariableValueFromObject(mojo, "outputXML");

        String encoding = getVariableValueFromObject(mojo, "outputEncoding");

        Assertions.assertTrue(outputXML.exists(), "Test if changelog.xml is created");

        String changelogXml = FileUtils.fileRead(outputXML);

        Assertions.assertTrue(
                changelogXml.startsWith("<?xml version=\"1.0\" encoding=\"" + encoding + "\"?>"),
                "Test for xml header");

        Assertions.assertTrue(changelogXml.endsWith("</changelog>"), "Test for xml footer");

        File outputDir = getVariableValueFromObject(mojo, "outputDirectory");

        File outputHtml = new File(outputDir, "changelog.html");

        Assertions.assertTrue(outputHtml.exists(), outputHtml.getAbsolutePath() + " not generated!");

        Assertions.assertTrue(outputHtml.length() > 0, outputHtml.getAbsolutePath() + " is empty!");

        if (!withOutput) {
            return null;
        }

        return new String(Files.readAllBytes(outputHtml.toPath()));
    }
}
