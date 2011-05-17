package com.lemoulinstudio.small.apt;

import com.lemoulinstudio.small.apt.oom.ClassName;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.tools.Diagnostic;

/**
 *
 * @author Vincent Cantin
 */
public class APConfig {

  public static final String platformOption = "platform";
  public static final String inputLocalBasePackageOption = "inputLocalBasePackage";
  public static final String inputRemoteBasePackageOption = "inputRemoteBasePackage";
  public static final String outputBasePackageOption = "outputBasePackage";
  public static final String configurationClassOption = "configurationClass";
  public static final String rootDecoderClassOption = "rootDecoderClass";
  public static final String callerObjectClassOption = "callerObjectClass";
  public static final String noLogOption = "noLog";
  public static final String verboseOption = "verbose";

  /**
   * @return A set containing the strings
   * "platform", "inputBasePackage", "outputBasePackage" and "verbose".
   */
  public static Set<String> getSupportedOptions() {
    return new HashSet<String>(Arrays.asList(
            platformOption,
            inputLocalBasePackageOption,
            inputRemoteBasePackageOption,
            outputBasePackageOption,
            configurationClassOption,
            rootDecoderClassOption,
            callerObjectClassOption,
            noLogOption,
            verboseOption
            ));
  }

  private Platform platform;
  private String inputLocalBasePackage;
  private String inputRemoteBasePackage;
  private String outputBasePackage;
  private ClassName configurationClassName;
  private ClassName rootDecoderClassName;
  private ClassName callerObjectClassName;
  private boolean noLog;
  private boolean verbose;

  private ProcessingEnvironment processingEnv;
  private RoundEnvironment roundEnv;
  private int nbFileGenerated;

  public APConfig(ProcessingEnvironment processingEnv, RoundEnvironment roundEnv) {
    this.processingEnv = processingEnv;
    this.roundEnv = roundEnv;
    
    platform = readPlatformValue();
    inputLocalBasePackage = readOptionValue(inputLocalBasePackageOption, "");
    inputRemoteBasePackage = readOptionValue(inputRemoteBasePackageOption, "");
    outputBasePackage = readOptionValue(outputBasePackageOption, "small.generated");

    configurationClassName = new ClassName(readOptionValue(configurationClassOption, outputBasePackage + ".Configuration"));
    rootDecoderClassName = new ClassName(readOptionValue(rootDecoderClassOption, outputBasePackage + ".RootDecoder"));
    
    callerObjectClassName = new ClassName(readOptionValue(callerObjectClassOption, "java.lang.Object"));

    noLog = readBooleanOptionValue(noLogOption, false);
    verbose = readBooleanOptionValue(verboseOption, false);
  }

  public Platform getPlatform() {
    return platform;
  }

  public String getInputLocalBasePackage() {
    return inputLocalBasePackage;
  }

  public String getInputRemoteBasePackage() {
    return inputRemoteBasePackage;
  }

  public String getOutputBasePackage() {
    return outputBasePackage;
  }

  public ClassName getConfigurationClassName() {
    return configurationClassName;
  }

  public ClassName getRootDecoderClassName() {
    return rootDecoderClassName;
  }

  public ClassName getCallerObjectClassName() {
    return callerObjectClassName;
  }

  public boolean isNoLog() {
    return noLog;
  }

  public boolean isVerbose() {
    return verbose;
  }

  public ProcessingEnvironment getProcessingEnv() {
    return processingEnv;
  }

  public RoundEnvironment getRoundEnv() {
    return roundEnv;
  }

  public int getNbFileGenerated() {
    return nbFileGenerated;
  }

  public void incrementNbFileGenerated() {
    nbFileGenerated++;
  }

  private Platform readPlatformValue() {
    // Check that the platform option is specified.
    if (!processingEnv.getOptions().containsKey(platformOption)) {
      processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
              "The option for the plaform MUST be specified. " +
              "Please see the documentation for more information.");
      return null;
    }

    // Try to find the platform.
    String givenPlatformName = processingEnv.getOptions().get(platformOption);
    for (Platform pf : Platform.values())
      if (pf.getName().equalsIgnoreCase(givenPlatformName))
        return pf;

    // If we can't find the platform, display an error message.
    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Unrecognized platform: " + givenPlatformName);
    return null;
  }

  private String readOptionValue(String option, String defaultValue) {
    String optionValue;

    // If no option is specified, return a default value.
    if (!processingEnv.getOptions().containsKey(option))
      optionValue = defaultValue;
    else {
      optionValue = processingEnv.getOptions().get(option);
      if (optionValue == null) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                "If used, the option " + option + " needs a specified value.");
      }
    }

    return optionValue;
  }

  private boolean readBooleanOptionValue(String option, boolean defaultValue) {
    if (!processingEnv.getOptions().containsKey(option))
      return defaultValue;
    else {
      String optionValue = processingEnv.getOptions().get(option);
      if (optionValue == null)
        return defaultValue;
      else
        return "true".equals(optionValue);
    }
  }

  public boolean isValid() {
    return (platform != null) &&
           (inputLocalBasePackage != null) &&
           (inputRemoteBasePackage != null) &&
           (outputBasePackage != null);
  }

}
