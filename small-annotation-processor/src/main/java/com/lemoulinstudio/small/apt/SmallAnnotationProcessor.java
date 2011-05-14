package com.lemoulinstudio.small.apt;

import com.lemoulinstudio.small.apt.model.CallerObject;
import com.lemoulinstudio.small.apt.model.ImplementedBy;
import com.lemoulinstudio.small.apt.model.NetworkInterface;
import com.lemoulinstudio.small.apt.model.NoLog;
import com.lemoulinstudio.small.apt.model.Singleton;
import com.lemoulinstudio.small.apt.generator.CodeGenerator;
import com.lemoulinstudio.small.apt.model.BindToLocalId;
import com.lemoulinstudio.small.apt.model.BindToSharedId;
import com.lemoulinstudio.small.apt.model.Log;
import com.lemoulinstudio.small.apt.oom.ModelData;
import com.lemoulinstudio.small.apt.oom.ModelFactory;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;


/**
 * <p>The annotation processor for the network model for Small.
 * It generates source files for encoding and decoding messages.</p>
 *
 * <p>Usage:
 * <pre>
 * javac
 *     -proc:only
 *     -processor com.lemoulinstudio.small.apt.SmallAnnotationProcessor
 *     -sourcepath sourceDir
 *     -s generatedSourceDir
 *     -Aplatform=platform
 *     -AinputBasePackage=inputBasePackage
 *     -AoutputBasePackage=outputBasePackage
 *     [-AconfigurationClass=configurationClass]
 *     [-ArootRemoteClass=rootRemoteClass]
 *     [-ArootProxyClass=rootProxyClass]
 *     [-ArootDecoderClass=rootDecoderClass]
 *     [-AembedSingletonProxies]
 *     [-AnoLog]
 *     [-Averbose]
 * </pre>
 * </p>
 *
 * <p><code>sourceDir</code> is the directory where the source of your network model is located.</p>
 * 
 * <p><code>generatedSourceDir</code> is the directory where the the annotation processor should
 * place the generated source files.</p>
 *
 * <p><code>platform</code> is the platform where your program is running. For now, you can
 * use one of the following values:
 * <ul>
 * <li>"<code>jse-client</code>" for the Jse client of your Red Dwarf application.</li>
 * <li>"<code>rds</code>" for the server of your Red Dwarf application.</li>
 * </ul>
 * </p>
 *
 * <p><code>inputBasePackage</code> is the root package where the network models are located.</p>
 *
 * <p><code>outputBasePackage</code> is the root package where the generated files will be located.</p>
 *
 * <p>An example of usage:
 * <pre>
 *   javac
 *     -proc:only
 *     -processor com.lemoulinstudio.small.apt.SmallAnnotationProcessor
 *     -sourcepath src/main/java/
 *     -s target/generated-sources/small-generated-stuffs/
 *     -Aplatform=jse
 *     -AinputBasePackage=game.networkmodel
 *     -AoutputBasePackage=game.client.network
 *     -Averbose
 * </pre>
 * </p>
 *
 * @author Vincent Cantin
 */
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class SmallAnnotationProcessor extends AbstractProcessor {

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<String> getSupportedOptions() {
    return APConfig.getSupportedOptions();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<String> getSupportedAnnotationTypes() {
    List<Class> supportedAnnotationTypeList = Arrays.<Class>asList(
            BindToLocalId.class,
            BindToSharedId.class,
            CallerObject.class,
            ImplementedBy.class,
            Log.class,
            NetworkInterface.class,
            NoLog.class,
            Singleton.class);

    Set<String> result = new HashSet<String>();
    for (Class clazz : supportedAnnotationTypeList)
      result.add(clazz.getName());

    return result;
  }

  private boolean alreadyProcessed = false;

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    // Only process once.
    if (alreadyProcessed) return true;
    alreadyProcessed = true;

    // Read the command line options.
    APConfig config = new APConfig(processingEnv, roundEnv);

    // If the options are not valid, we stop here.
    if (!config.isValid())
      return true;

    // Build object representation of the models.
    ModelFactory modelFactory = new ModelFactory(config);
    ModelData modelData = modelFactory.getModelData();

    // Get the code generator object.
    CodeGenerator codeGenerator = config.getPlatform().getCodeGeneratorInstance();
    codeGenerator.setConfig(config);
    codeGenerator.setModelData(modelData);

    // Generate interfaces, proxies and decoders.
    codeGenerator.generateAll();

    config.getProcessingEnv().getMessager().printMessage(Diagnostic.Kind.NOTE,
            "" + config.getNbFileGenerated() + " file(s) have been generated.");
    
    return true;
  }

}
