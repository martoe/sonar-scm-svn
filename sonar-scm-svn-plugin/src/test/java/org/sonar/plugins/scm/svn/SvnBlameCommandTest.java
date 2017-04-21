/*
 * SonarQube :: Plugins :: SCM :: SVN
 * Copyright (C) 2014-2017 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.plugins.scm.svn;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.ArgumentCaptor;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.scm.BlameCommand.BlameInput;
import org.sonar.api.batch.scm.BlameCommand.BlameOutput;
import org.sonar.api.batch.scm.BlameLine;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.wc2.compat.SvnCodec;
import org.tmatesoft.svn.core.wc.ISVNOptions;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;
import org.tmatesoft.svn.core.wc2.SvnCheckout;
import org.tmatesoft.svn.core.wc2.SvnTarget;

import com.google.common.io.Closeables;

@RunWith(Parameterized.class)
public class SvnBlameCommandTest {

  /*
   * Note about SONARSCSVN-11: The case of a project baseDir is in a subFolder of working copy is part of method tests by default
   */

  private static final String DUMMY_JAVA = "src/main/java/org/dummy/Dummy.java";

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private FileSystem fs;
  private BlameInput input;
  private String serverVersion;
  private int wcVersion;

  @Parameters(name = "SVN server version {0}, WC version {1}")
  public static Iterable<Object[]> data() {
    return Arrays.asList(new Object[][] {{"1.6", 10}, {"1.8", 31}});
  }

  public SvnBlameCommandTest(String serverVersion, int wcVersion) {
    this.serverVersion = serverVersion;
    this.wcVersion = wcVersion;
  }

  @Before
  public void prepare() throws IOException {
    fs = mock(FileSystem.class);
    input = mock(BlameInput.class);
    when(input.fileSystem()).thenReturn(fs);
  }

  @Test
  public void testParsingOfOutput() throws Exception {
    File repoDir = unzip("repo-svn.zip");

    String scmUrl = "file:///" + unixPath(new File(repoDir, "repo-svn"));
    File baseDir = new File(checkout(scmUrl), "dummy-svn");

    when(fs.baseDir()).thenReturn(baseDir);
    DefaultInputFile inputFile = new DefaultInputFile("foo", DUMMY_JAVA)
      .setLines(27)
      .setModuleBaseDir(baseDir.toPath());

    BlameOutput blameResult = mock(BlameOutput.class);
    when(input.filesToBlame()).thenReturn(Arrays.<InputFile>asList(inputFile));

    new SvnBlameCommand(mock(SvnConfiguration.class)).blame(input, blameResult);
    ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
    verify(blameResult).blameResult(eq(inputFile), captor.capture());
    List<BlameLine> result = captor.getValue();
    assertThat(result).hasSize(27);
    Date commitDate = new Date(1342691097393L);
    assertThat(result).containsExactly(
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"));
  }

  private File unzip(String repoName) throws IOException {
    File repoDir = temp.newFolder();
    javaUnzip(Paths.get("test-repos", serverVersion, repoName).toFile(), repoDir);
    return repoDir;
  }

  private File checkout(String scmUrl) throws Exception {
    ISVNOptions options = SVNWCUtil.createDefaultOptions(true);
    ISVNAuthenticationManager isvnAuthenticationManager = SVNWCUtil.createDefaultAuthenticationManager(null, null, (char[]) null, false);
    SVNClientManager svnClientManager = SVNClientManager.newInstance(options, isvnAuthenticationManager);
    File out = temp.newFolder();
    SVNUpdateClient updateClient = svnClientManager.getUpdateClient();
    SvnCheckout co = updateClient.getOperationsFactory().createCheckout();
    co.setUpdateLocksOnDemand(updateClient.isUpdateLocksOnDemand());
    co.setSource(SvnTarget.fromURL(SVNURL.parseURIEncoded(scmUrl), SVNRevision.HEAD));
    co.setSingleTarget(SvnTarget.fromFile(out));
    co.setRevision(SVNRevision.HEAD);
    co.setDepth(SVNDepth.INFINITY);
    co.setAllowUnversionedObstructions(false);
    co.setIgnoreExternals(updateClient.isIgnoreExternals());
    co.setExternalsHandler(SvnCodec.externalsHandler(updateClient.getExternalsHandler()));
    co.setTargetWorkingCopyFormat(wcVersion);
    co.run();
    return out;
  }

  @Test
  public void testParsingOfOutputWithMergeHistory() throws Exception {
    File repoDir = unzip("repo-svn-with-merge.zip");

    String scmUrl = "file:///" + unixPath(new File(repoDir, "repo-svn"));
    File baseDir = new File(checkout(scmUrl), "dummy-svn/trunk");

    when(fs.baseDir()).thenReturn(baseDir);
    DefaultInputFile inputFile = new DefaultInputFile("foo", DUMMY_JAVA)
      .setLines(27)
      .setModuleBaseDir(baseDir.toPath());

    BlameOutput blameResult = mock(BlameOutput.class);
    when(input.filesToBlame()).thenReturn(Arrays.<InputFile>asList(inputFile));

    new SvnBlameCommand(mock(SvnConfiguration.class)).blame(input, blameResult);
    ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
    verify(blameResult).blameResult(eq(inputFile), captor.capture());
    List<BlameLine> result = captor.getValue();
    assertThat(result).hasSize(27);
    Date commitDate = new Date(1342691097393L);
    Date revision6Date = new Date(1415262184300L);
    assertThat(result).containsExactly(
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(revision6Date).revision("6").author("henryju"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(revision6Date).revision("6").author("henryju"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"));
  }

  @Test
  public void shouldNotFailIfFileContainsLocalModification() throws Exception {
    File repoDir = unzip("repo-svn.zip");

    String scmUrl = "file:///" + unixPath(new File(repoDir, "repo-svn"));
    File baseDir = new File(checkout(scmUrl), "dummy-svn");

    when(fs.baseDir()).thenReturn(baseDir);
    DefaultInputFile inputFile = new DefaultInputFile("foo", DUMMY_JAVA)
      .setLines(28)
      .setModuleBaseDir(baseDir.toPath());

    FileUtils.write(new File(baseDir, DUMMY_JAVA), "\n//foo", true);

    BlameOutput blameResult = mock(BlameOutput.class);
    when(input.filesToBlame()).thenReturn(Arrays.<InputFile>asList(inputFile));

    new SvnBlameCommand(mock(SvnConfiguration.class)).blame(input, blameResult);
    verifyZeroInteractions(blameResult);
  }

  // SONARSCSVN-7
  @Test
  public void shouldNotFailOnWrongFilename() throws Exception {
    File repoDir = unzip("repo-svn.zip");

    String scmUrl = "file:///" + unixPath(new File(repoDir, "repo-svn"));
    File baseDir = new File(checkout(scmUrl), "dummy-svn");

    when(fs.baseDir()).thenReturn(baseDir);
    DefaultInputFile inputFile = new DefaultInputFile("foo", DUMMY_JAVA.toLowerCase())
      .setLines(27)
      .setModuleBaseDir(baseDir.toPath());

    BlameOutput blameResult = mock(BlameOutput.class);
    when(input.filesToBlame()).thenReturn(Arrays.<InputFile>asList(inputFile));

    new SvnBlameCommand(mock(SvnConfiguration.class)).blame(input, blameResult);
    verifyZeroInteractions(blameResult);
  }

  @Test
  public void shouldNotFailOnUncommitedFile() throws Exception {
    File repoDir = unzip("repo-svn.zip");

    String scmUrl = "file:///" + unixPath(new File(repoDir, "repo-svn"));
    File baseDir = new File(checkout(scmUrl), "dummy-svn");

    when(fs.baseDir()).thenReturn(baseDir);
    String relativePath = "src/main/java/org/dummy/Dummy2.java";
    DefaultInputFile inputFile = new DefaultInputFile("foo", relativePath)
      .setLines(28)
      .setModuleBaseDir(baseDir.toPath());

    FileUtils.write(new File(baseDir, relativePath), "package org.dummy;\npublic class Dummy2 {}");

    BlameOutput blameResult = mock(BlameOutput.class);
    when(input.filesToBlame()).thenReturn(Arrays.<InputFile>asList(inputFile));

    new SvnBlameCommand(mock(SvnConfiguration.class)).blame(input, blameResult);
    verifyZeroInteractions(blameResult);
  }

  @Test
  public void shouldNotFailOnUncommitedDir() throws Exception {
    File repoDir = unzip("repo-svn.zip");

    String scmUrl = "file:///" + unixPath(new File(repoDir, "repo-svn"));
    File baseDir = new File(checkout(scmUrl), "dummy-svn");

    when(fs.baseDir()).thenReturn(baseDir);
    String relativePath = "src/main/java/org/dummy2/dummy/Dummy.java";
    DefaultInputFile inputFile = new DefaultInputFile("foo", relativePath)
      .setLines(28)
      .setModuleBaseDir(baseDir.toPath());

    FileUtils.write(new File(baseDir, relativePath), "package org.dummy;\npublic class Dummy {}");

    BlameOutput blameResult = mock(BlameOutput.class);
    when(input.filesToBlame()).thenReturn(Arrays.<InputFile>asList(inputFile));

    new SvnBlameCommand(mock(SvnConfiguration.class)).blame(input, blameResult);
    verifyZeroInteractions(blameResult);
  }

  private static void javaUnzip(File zip, File toDir) {
    try {
      ZipFile zipFile = new ZipFile(zip);
      try {
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
          ZipEntry entry = entries.nextElement();
          File to = new File(toDir, entry.getName());
          if (entry.isDirectory()) {
            FileUtils.forceMkdir(to);
          } else {
            File parent = to.getParentFile();
            if (parent != null) {
              FileUtils.forceMkdir(parent);
            }

            OutputStream fos = new FileOutputStream(to);
            try {
              IOUtils.copy(zipFile.getInputStream(entry), fos);
            } finally {
              Closeables.closeQuietly(fos);
            }
          }
        }
      } finally {
        zipFile.close();
      }
    } catch (Exception e) {
      throw new IllegalStateException("Fail to unzip " + zip + " to " + toDir, e);
    }
  }

  private static String unixPath(File file) {
    return file.getAbsolutePath().replace('\\', '/');
  }

}
