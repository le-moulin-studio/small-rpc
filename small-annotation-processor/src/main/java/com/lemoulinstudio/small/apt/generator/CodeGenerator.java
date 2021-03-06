package com.lemoulinstudio.small.apt.generator;

import com.lemoulinstudio.small.apt.APConfig;
import com.lemoulinstudio.small.apt.oom.ClassName;
import com.lemoulinstudio.small.apt.oom.ModelClass;
import com.lemoulinstudio.small.apt.oom.ModelData;
import java.util.List;

/**
 * This class generate code from models.
 *
 * @author Vincent Cantin
 */
public abstract class CodeGenerator {

  protected APConfig config;
  protected ModelData modelData;

  public void setConfig(APConfig config) {
    this.config = config;
  }

  public void setModelData(ModelData modelData) {
    this.modelData = modelData;
  }

  public abstract void generateAll();

  protected ClassName getInterfaceName(ModelClass modelClass) {
    return new ClassName(config.getOutputBasePackage() + "." +
            removeFromBeginning(modelClass.getPackageName(),
            (modelClass.isLocalSide() ? config.getInputLocalBasePackage() : config.getInputRemoteBasePackage())) +
            (modelClass.isLocalSide() ? "local." : "remote.") +
            modelClass.getSimpleName());
  }

  protected ClassName getProxyName(ModelClass modelClass) {
    return new ClassName(config.getOutputBasePackage() + "." +
            removeFromBeginning(modelClass.getPackageName(), config.getInputRemoteBasePackage()) +
            "proxy." + modelClass.getSimpleName() + "Proxy");
  }

  protected ClassName getDecoderName(ModelClass modelClass) {
    return new ClassName(config.getOutputBasePackage() + "." +
            removeFromBeginning(modelClass.getPackageName(), config.getInputLocalBasePackage()) +
            "decoder." + modelClass.getSimpleName() + "Decoder");
  }

  protected ClassName getValueObjectName(ClassName className) {
    if (className.getPackageName().startsWith(config.getInputVoBasePackage()))
      return new ClassName(config.getOutputBasePackage() + "." +
              removeFromBeginning(className.getPackageName(), config.getInputVoBasePackage()) +
              "vo." + className.getSimpleName());
    else
      return className;
  }

  protected String removeFromBeginning(String name, String toBeRemoved) {
    if (name.startsWith(toBeRemoved))
      return name.substring(toBeRemoved.length());
    else return name;
  }

  protected CharSequence getCommaSeparatedSequence(List<? extends CharSequence> list) {
    return getCommaSeparatedSequence(list, ", ");
  }

  protected CharSequence getCommaSeparatedSequence(List<? extends CharSequence> list, String commaString) {
    StringBuffer buffer = new StringBuffer();
    for (int i = 0; i < list.size(); i++) {
      if (i > 0) buffer.append(commaString);
      buffer.append(list.get(i));
    }
    return buffer;
  }

  protected void printNote(String note) {
    printNote(note, true);
  }

  protected void printNote(String note, boolean onlyForVerboseMode) {
    if (config.isVerbose() || !onlyForVerboseMode)
      //config.getProcessingEnv().getMessager().printMessage(Diagnostic.Kind.NOTE, note);
      System.out.println(note);
  }
}
