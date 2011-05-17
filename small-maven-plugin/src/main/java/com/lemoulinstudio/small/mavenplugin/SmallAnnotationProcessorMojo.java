package com.lemoulinstudio.small.mavenplugin;

import com.lemoulinstudio.small.apt.APConfig;
import com.lemoulinstudio.small.apt.SmallAnnotationProcessor;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;


/**
 * Mojo which wraps the Small annotation processor into a Maven plugin
 * for a better integration into the build process.
 *
 * @phase generate-sources
 * @goal generate-rpc-files
 * @requiresDependencyResolution
 */
public class SmallAnnotationProcessorMojo extends AbstractMojo {

  /**
   * This Maven project.
   * @parameter expression="${project}"
   * @required
   * @readonly
   */
  private MavenProject project;

  /**
   * The target platform.
   * @parameter
   * @required
   */
  private String platform;

  /**
   * The artifact that contains the RPC protocol.
   * @parameter
   * @required
   */
  private File[] sourceDirectories;

  /**
   * Location of the output directory.
   * @parameter expression="${project.build.directory}"
   * @required
   * @readonly
   */
  private File outputDirectory;

  /**
   * The base package for local services in the model.
   * @parameter
   * @required
   */
  private String inputLocalBasePackage;

  /**
   * The base package for remote services in the model.
   * @parameter
   * @required
   */
  private String inputRemoteBasePackage;

  /**
   * The base package in the generated sources.
   * @parameter
   * @required
   */
  private String outputBasePackage;

  /**
   * The configuration qualified classname.
   * @parameter
   */
  private String configurationClass;

  /**
   * The root decoder qualified classname.
   * @parameter
   */
  private String rootDecoderClass;
  
  /**
   * The caller object qualified classname.
   * @parameter
   */
  private String callerObjectClass;

  /**
   * The noLog flag.
   * @parameter
   */
  private boolean noLog;

  /**
   * The verbose flag.
   * @parameter
   */
  private boolean verbose;

  @Override
  public void execute() throws MojoExecutionException {
    // Prepare the destination directory.
    File generatedSourceDir = new File(outputDirectory, "generated-sources/small-maven-plugin/");
    if (!generatedSourceDir.exists()) generatedSourceDir.mkdirs();
    project.addCompileSourceRoot(generatedSourceDir.getAbsolutePath());

    // Get the compiler.
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

    // 
    try {
      Set<File> files = new HashSet<File>();
      //String sourceDirectoryPaths = "";
      
      for (int i = 0; i < sourceDirectories.length; i++)
        files.addAll(FileUtils.getFiles(sourceDirectories[i], "**/*.java", null));

      Iterable<? extends JavaFileObject> compilationUnits =
          fileManager.getJavaFileObjectsFromFiles(files);

      String compileClassPath = buildCompileClasspath();
      //System.out.println("$$$ compileClassPath = " + compileClassPath);

      List<String> options = new ArrayList<String>(Arrays.asList(
              "-cp", compileClassPath,
              "-proc:only",
              "-processor", SmallAnnotationProcessor.class.getName(),
              //"-sourcepath", sourceDirectoryPaths,
              "-s", generatedSourceDir.getPath(),
              "-A" + APConfig.platformOption + "=" + platform,
              "-A" + APConfig.inputLocalBasePackageOption + "=" + inputLocalBasePackage,
              "-A" + APConfig.inputRemoteBasePackageOption + "=" + inputRemoteBasePackage,
              "-A" + APConfig.outputBasePackageOption + "=" + outputBasePackage));

      if (configurationClass != null)
        options.add("-A" + APConfig.configurationClassOption + "=" + configurationClass);

      if (rootDecoderClass != null)
        options.add("-A" + APConfig.rootDecoderClassOption + "=" + rootDecoderClass);

      if (callerObjectClass != null)
        options.add("-A" + APConfig.callerObjectClassOption + "=" + callerObjectClass);

      if (noLog)
        options.add("-A" + APConfig.noLogOption);

      if (verbose)
        options.add("-A" + APConfig.verboseOption);

      CompilationTask task = compiler.getTask(
              new PrintWriter(System.out),
              fileManager,
              null,
              options,
              null,
              compilationUnits);

      //Perform the compilation task.
      task.call();
    }
    catch (Exception e) {
      super.getLog().error("execute error", e);
      throw new MojoExecutionException( e.getMessage() );
    }
  }

  @SuppressWarnings("unchecked")
  private String buildCompileClasspath() {
    StringBuilder result = new StringBuilder();

    List<String> pathElements = new ArrayList<String>();
    
    try {
      pathElements.addAll(project.getCompileClasspathElements());
    }
    catch (DependencyResolutionRequiredException e) {
      super.getLog().warn("exception calling getCompileClasspathElements", e);
    }

    for (int i = 0; i < pathElements.size(); i++) {
      if (i > 0) result.append(File.pathSeparator);
      result.append(pathElements.get(i));
    }

    return result.toString();
  }

  protected <T> void println(String name, Collection<T> collection) {
    System.out.println(name);

    if (collection == null) return;

    for (T a : collection)
      System.out.printf( "\t[%s] %s\n", a.getClass().getName(), a.toString());
  }

  protected void printDeps(String name, Collection<Dependency> dependencies) {
    System.out.println(name);

    for(Dependency dependency : dependencies) {
      System.out.printf("dependency [%s]\n", dependency.toString());
      
      String versionlessKey = ArtifactUtils.versionlessKey(dependency.getGroupId(),
              dependency.getArtifactId());

      Artifact artifact = (Artifact) project.getArtifactMap().get(versionlessKey);
      if (artifact != null) {
        File file = artifact.getFile();
        System.out.printf("artifact [%s]\n", file.getPath());
      }
    }
  }

}
