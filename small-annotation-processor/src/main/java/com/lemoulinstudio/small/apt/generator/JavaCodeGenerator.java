package com.lemoulinstudio.small.apt.generator;

import com.lemoulinstudio.small.apt.oom.IdBindingPolicy;
import com.lemoulinstudio.small.apt.oom.ModelClass;
import com.lemoulinstudio.small.apt.oom.ModelMethod;
import com.lemoulinstudio.small.apt.oom.ModelParameter;
import com.lemoulinstudio.small.apt.type.ArrayType;
import com.lemoulinstudio.small.apt.type.DeclaredType;
import com.lemoulinstudio.small.apt.type.EnumType;
import com.lemoulinstudio.small.apt.type.ModelType;
import com.lemoulinstudio.small.apt.type.PrimitiveType;
import com.lemoulinstudio.small.apt.type.PrimitiveWrapperType;
import com.lemoulinstudio.small.apt.type.Type;
import com.lemoulinstudio.small.apt.type.TypeKind;
import com.lemoulinstudio.small.common.BindToLocalId;
import com.lemoulinstudio.small.common.BindToSharedId;
import com.lemoulinstudio.small.common.Local;
import com.lemoulinstudio.small.common.Remote;
import com.lemoulinstudio.small.common.Singleton;
import com.lemoulinstudio.small.jse.View;
import com.lemoulinstudio.small.utils.Utils;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 *
 * @author Vincent Cantin
 */
public class JavaCodeGenerator extends CodeGenerator {

  private Class smallSessionClass;
  private Class generatedConfigClass;
  private Class proxyClass;
  private Class rootDecoderClass;
  private Class decoderClass;
  private String implementSerializable;
  private String serialVersionUIDField;

  public JavaCodeGenerator(Class smallSessionClass, Class generatedConfigClass, Class proxyClass, Class rootDecoderClass, Class decoderClass, String implementSerializable, String serialVersionUIDField) {
    this.smallSessionClass = smallSessionClass;
    this.generatedConfigClass = generatedConfigClass;
    this.proxyClass = proxyClass;
    this.rootDecoderClass = rootDecoderClass;
    this.decoderClass = decoderClass;
    this.implementSerializable = implementSerializable;
    this.serialVersionUIDField = serialVersionUIDField;
  }
  
  @Override
  public void generateAll() {

    List<ModelClass> otherSideEmbeddedClassList = new ArrayList<ModelClass>();
    List<ModelClass> otherSideNonEmbeddedClassList = new ArrayList<ModelClass>();

    for (ModelClass modelClass : modelData.getOtherSideModelClassOrderedList()) {
      if (modelClass.isView() || (modelClass.isSingleton() && config.shouldEmbedSingletonProxies())) otherSideEmbeddedClassList.add(modelClass);
      else otherSideNonEmbeddedClassList.add(modelClass);
    }

    // Generate the configuration file.
    generateConfigFile(!otherSideEmbeddedClassList.isEmpty(), otherSideNonEmbeddedClassList);

    // Generate the decoders.
    generateRootDecoder(modelData.getSameSideModelClassOrderedList());

    // Generate the root proxy if it is needed.
    if (!otherSideEmbeddedClassList.isEmpty()) {
      generateRootRemoteInterface(otherSideEmbeddedClassList);
      generateRootProxy(otherSideEmbeddedClassList);
    }

    // Generate the interfaces for local objects to implement.
    for (ModelClass modelClass : modelData.getSameSideModelClassOrderedList())
      generateInterface(modelClass);

    // Generate the interface for remote objects which will have a proxy.
    for (ModelClass modelClass : otherSideNonEmbeddedClassList)
      generateInterface(modelClass);
    
    // Generate the proxies for remote objects which are not embedded inside the root proxy.
    for (ModelClass modelClass : otherSideNonEmbeddedClassList)
      generateClassProxy(modelClass);

  }

  protected void generateConfigFile(boolean hasRootProxy, List<ModelClass> modelClasseList) {
    StringBuffer buffer = new StringBuffer();
    
    buffer.append("package " + config.getConfigurationClassName().getPackageName() + ";\n");
    buffer.append("\n");
    buffer.append("public class " + config.getConfigurationClassName().getSimpleName() +
            " implements " + generatedConfigClass.getName() +
            implementSerializable + " {\n");

    buffer.append(serialVersionUIDField);
    buffer.append("\n");
    buffer.append("  @Override\n");
    buffer.append("  public java.util.Map<Class<? extends " + Remote.class.getName() + ">, Class> getRemoteClassToProxyClass() {\n");
    buffer.append("    java.util.Map<Class<? extends " + Remote.class.getName() + ">, Class> result = new java.util.HashMap<Class<? extends " + Remote.class.getName() + ">, Class>();\n");
    if (hasRootProxy)
      buffer.append("    result.put(" + config.getRootRemoteClassName().getQualifiedName() + ".class, " + config.getRootProxyClassName().getQualifiedName() + ".class);\n");
    for (ModelClass modelClass : modelClasseList)
      buffer.append("    result.put(" + getInterfaceName(modelClass).getQualifiedName() + ".class, " + getProxyName(modelClass).getQualifiedName() + ".class);\n");
    buffer.append("    return result;\n");
    buffer.append("  }\n");

    buffer.append("\n");
    buffer.append("  @Override\n");
    buffer.append("  public " + rootDecoderClass.getName() + " getRootDecoder() {\n");
    buffer.append("    return new " + config.getRootDecoderClassName().getQualifiedName() + "();\n");
    buffer.append("  }\n");
    
    buffer.append("\n");
    buffer.append("}\n");

    writeFileContent(config.getConfigurationClassName().getQualifiedName(), buffer);
  }

  protected void generateRootRemoteInterface(List<ModelClass> modelClassList) {
    StringBuffer buffer = new StringBuffer();
    buffer.append("package " + config.getRootRemoteClassName().getPackageName() + ";\n");
    buffer.append("\n");
    buffer.append(getGeneratedTag() + "\n");
    buffer.append("public interface " + config.getRootRemoteClassName().getSimpleName() +
            " extends " + Singleton.class.getName() + ", " + Remote.class.getName() + " {\n");

    for (ModelClass modelClass : modelClassList) {
      for (ModelMethod modelMethod : modelClass.getMethodList()) {
        List<String> parameterTextList = new ArrayList<String>();
        if (!modelClass.isSingleton())
          parameterTextList.add(toString(new ModelType(modelClass.getViewedModel()), false, false) + " messageTarget");
        for (ModelParameter modelParameter : modelMethod.getParameterList())
          parameterTextList.add(toString(modelParameter.getType(), modelClass.isLocalSide(), false) +
                  " " + modelParameter.getName());

        buffer.append("  public void " + modelClass.getSimpleName() + "_" + modelMethod.getName() +
                "(" + getCommaSeparatedSequence(parameterTextList) + ");\n");
      }
    }

    buffer.append("}\n");
    
    writeFileContent(config.getRootRemoteClassName().getQualifiedName(), buffer);
  }

  protected void generateRootProxy(List<ModelClass> modelClassList) {
    // Choose the right way to encode the methodIDs with respect to their cardinal.
    String writeMethodIdFormat;
    if (modelData.getNumberOfMethodsOnSameSide() <= 1) writeMethodIdFormat = "// No need to write the methodId.";
    else if (modelData.getNumberOfMethodsOnSameSide() <= 256) writeMethodIdFormat = "outputStream.writeByte(%s);";
    else if (modelData.getNumberOfMethodsOnSameSide() <= 65536) writeMethodIdFormat = "outputStream.writeShort(%s);";
    else writeMethodIdFormat = "outputStream.writeInt(%s);";

    // Start to generate the proxy.
    StringBuffer buffer = new StringBuffer();

    buffer.append("package " + config.getRootProxyClassName().getPackageName() + ";\n");
    buffer.append("\n");
    buffer.append(getGeneratedTag() + "\n");
    buffer.append("public class " + config.getRootProxyClassName().getSimpleName() +
            " extends " + proxyClass.getName() + "<" + config.getRootRemoteClassName().getQualifiedName() + ">" +
            " implements " + config.getRootRemoteClassName().getQualifiedName() +
            implementSerializable + " {\n");

    buffer.append(serialVersionUIDField);

    // The constructor.
    buffer.append("\n");
    buffer.append("  public " + config.getRootProxyClassName().getSimpleName() +
            "(" + smallSessionClass.getName() + " smallSession) {\n");
    buffer.append("    super(smallSession);\n");
    buffer.append("  }\n");

    for (ModelClass modelClass : modelClassList) {
      for (ModelMethod modelMethod : modelClass.getMethodList()) {
        List<String> parameterTextList = new ArrayList<String>();
        if (!modelClass.isSingleton())
          parameterTextList.add(toString(new ModelType(modelClass.getViewedModel()), false, false) + " messageTarget");
        for (ModelParameter modelParameter : modelMethod.getParameterList())
          parameterTextList.add(toString(modelParameter.getType(), modelClass.isLocalSide(), false) +
                  " param_" + modelParameter.getName());

        buffer.append("\n");
        buffer.append("  @Override\n");
        buffer.append("  public void " + modelClass.getSimpleName() + "_" + modelMethod.getName() + "(" + getCommaSeparatedSequence(parameterTextList) + ") {\n");

//        if (config.getPlatform() == Platform.RedDwarfServer)
//          buffer.append("    " + com.lemoulinstudio.small.rds.SmallSessionImpl.class.getName() + " smallSession = smallSessionRef.get();\n");
        buffer.append("    java.io.ByteArrayOutputStream byteArrayStream = new java.io.ByteArrayOutputStream();\n");
        buffer.append("    java.io.DataOutputStream outputStream = new java.io.DataOutputStream(byteArrayStream);\n");

        // Open the "try".
        buffer.append("\n");
        buffer.append("    try {\n");

        // Encode the method's Id.
        buffer.append("      // Encode the method's Id.\n");
        buffer.append("      " + String.format(writeMethodIdFormat, modelMethod.getMethodId()) + "\n");
        buffer.append("\n");

        // Encode the targeted object's Id.
        buffer.append("      // Encode the target object's reference.\n");
        buffer.append(getEncodingSourceCode("      ",
                "messageTarget",
                "outputStream",
                new ModelType(modelClass),
                0, true));

        // Encode the parameter's values.
        for (ModelParameter modelParameter : modelMethod.getParameterList()) {
          buffer.append("\n");
          buffer.append("      // Encode the parameter param_" + modelParameter.getName() + ".\n");
          buffer.append(getEncodingSourceCode("      ",
                  "param_" + modelParameter.getName(),
                  "outputStream",
                  modelParameter.getType(),
                  0, true));
        }

        // Close the "try" with a "catch" which does nothing.
        buffer.append("    }\n");
        buffer.append("    catch (java.io.IOException e) {}\n");
        buffer.append("\n");

        // Eventually log the message.
        if (modelMethod.shouldLogMethodInvocation()) {
          buffer.append("    // Log the method call.\n");
          buffer.append("    smallSession.logText(\"-> \"" + getArgumentLogText("this", modelMethod) + ");\n");
          buffer.append("\n");
        }

        // Send the message.
        buffer.append("    // Send the message.\n");
        buffer.append("    smallSession.sendMessage(byteArrayStream);\n");
        buffer.append("  }\n");
      }
    }

    buffer.append("}\n");

    writeFileContent(config.getRootProxyClassName().getQualifiedName(), buffer);
  }

  protected void generateRootDecoder(List<ModelClass> modelClassList) {
    StringBuffer buffer = new StringBuffer();

    buffer.append("package " + config.getRootDecoderClassName().getPackageName() + ";\n");
    buffer.append("\n");
    buffer.append("public class " + config.getRootDecoderClassName().getSimpleName() +
            " implements " + rootDecoderClass.getName() +
            implementSerializable + " {\n");

    buffer.append(serialVersionUIDField);
    buffer.append("\n");
    buffer.append("  @Override\n");
    buffer.append("  public void decodeAndInvoke(" + smallSessionClass.getName() + " smallSession, java.io.DataInputStream inputStream) throws java.io.IOException {\n");

    buffer.append("    int methodId = ");
    if (modelData.getNumberOfMethodsOnSameSide() <= 1) buffer.append("0;\n");
    else if (modelData.getNumberOfMethodsOnSameSide() <= 256) buffer.append("inputStream.readUnsignedByte();\n");
    else if (modelData.getNumberOfMethodsOnSameSide() <= 65536) buffer.append("inputStream.readUnsignedShort();\n");
    else buffer.append("inputStream.readInt();\n");

    buffer.append("    switch (methodId) {\n");
    for (ModelClass modelClass : modelClassList)
      for (ModelMethod modelMethod : modelClass.getMethodList()) {
        buffer.append("      // " + getInterfaceName(modelClass).getQualifiedName() + "." + modelMethod.getName() + "()\n");
        buffer.append("      case " + modelMethod.getMethodId() + ": {\n");

        // Decode the message's target.
        buffer.append("        // Decode the target of the message.\n");
        buffer.append(getDecodingSourceCode(
                "%s = %s",
                "        ",
                getInterfaceName(modelClass).getQualifiedName() + " target",
                "inputStream",
                new ModelType(modelClass),
                0));
        
        // Declare the parameters.
        if (!modelMethod.getParameterList().isEmpty()) {
          buffer.append("\n");
          buffer.append("        // Declare the parameters.\n");
          for (ModelParameter modelParameter : modelMethod.getParameterList()) {
            buffer.append("        " + toString(modelParameter.getType(), true, false) + " param_" + modelParameter.getName() + ";\n");
          }
        }

        // Decode the parameters.
        for (ModelParameter modelParameter : modelMethod.getParameterList()) {
          buffer.append("\n");
          buffer.append("        // Decode the parameter param_" + modelParameter.getName() + ".\n");
          if (modelParameter.isCallerObject()) {
            buffer.append("        param_" + modelParameter.getName() + " = (" +
                    toString(modelParameter.getType(), true, false) + ") smallSession.getCallerObject();\n");
          }
          else {
            buffer.append(getDecodingSourceCode("%s = %s",
                    "        ",
                    "param_" + modelParameter.getName(),
                    "inputStream",
                    modelParameter.getType(),
                    0));
          }
        }

        // Eventually log the message.
        if (modelMethod.shouldLogMessageReception()) {
          buffer.append("\n");
          buffer.append("        // Log the message reception.\n");
          buffer.append("        smallSession.logText(\"<- \"" + getArgumentLogText("target", modelMethod) + ");\n");
        }

        // Call the method.
        buffer.append("\n");
        buffer.append("        // Call the method on the target.\n");
        List<String> parameterNameList = new ArrayList<String>();
        for (ModelParameter modelParameter : modelMethod.getParameterList())
          parameterNameList.add("param_" + modelParameter.getName());
        buffer.append("        target." + modelMethod.getName() + "(" + getCommaSeparatedSequence(parameterNameList) + ");\n");

        buffer.append("\n");
        buffer.append("        break;\n");
        buffer.append("      }\n");
        buffer.append("\n");
      }

    buffer.append("      default:\n");
    buffer.append("    }\n");
    buffer.append("  }\n");
    buffer.append("\n");
    buffer.append("}\n");

    writeFileContent(config.getRootDecoderClassName().getQualifiedName(), buffer);
  }

  protected void generateInterface(ModelClass modelClass) {
    StringBuffer buffer = new StringBuffer();

    buffer.append("package " + getInterfaceName(modelClass).getPackageName() + ";\n");
    buffer.append("\n");
    buffer.append(getGeneratedTag() + "\n");
    buffer.append("public interface " + getInterfaceName(modelClass).getSimpleName());

    List<String> superInterfaceList = new ArrayList<String>();

    if (modelClass.isLocalSide()) {
      if (modelClass.isView())
        superInterfaceList.add(View.class.getName() + "<" + getInterfaceName(modelClass.getViewedModel()).getQualifiedName() + ">");
      else switch (modelClass.getIdBindingPolicy()) {
        case Singleton: superInterfaceList.add(Singleton.class.getName()); break;
        case SharedId: superInterfaceList.add(BindToSharedId.class.getName()); break;
        case LocalId: superInterfaceList.add(BindToLocalId.class.getName()); break;
      }
      
      superInterfaceList.add(Local.class.getName());
    }
    else {
      switch (modelClass.getIdBindingPolicy()) {
        case Singleton: superInterfaceList.add(Singleton.class.getName()); break;
        case SharedId: superInterfaceList.add(BindToSharedId.class.getName()); break;
        case LocalId: superInterfaceList.add(BindToLocalId.class.getName()); break;
      }

      superInterfaceList.add(Remote.class.getName());
    }
    
    if (!superInterfaceList.isEmpty())
      buffer.append(" extends " + getCommaSeparatedSequence(superInterfaceList));
    
    buffer.append(" {\n");

    for (ModelMethod modelMethod : modelClass.getMethodList()) {
      List<String> parameterTextList = new ArrayList<String>();
      for (ModelParameter modelParameter : modelMethod.getParameterList())
        parameterTextList.add(toString(modelParameter.getType(), modelClass.isLocalSide(), false) +
                " " + modelParameter.getName());

      buffer.append("  public void " + modelMethod.getName() + "(" + getCommaSeparatedSequence(parameterTextList) + ");\n");
    }

    buffer.append("}\n");

    writeFileContent(getInterfaceName(modelClass).getQualifiedName(), buffer);
  }

  protected void generateClassProxy(ModelClass modelClass) {
    // Choose the right way to encode the methodIDs with respect to their cardinal.
    String writeMethodIdFormat;
    if (modelData.getNumberOfMethodsOnSameSide() <= 1) writeMethodIdFormat = "// No need to write the methodId.";
    else if (modelData.getNumberOfMethodsOnSameSide() <= 256) writeMethodIdFormat = "outputStream.writeByte(%s);";
    else if (modelData.getNumberOfMethodsOnSameSide() <= 65536) writeMethodIdFormat = "outputStream.writeShort(%s);";
    else writeMethodIdFormat = "outputStream.writeInt(%s);";

    // Start to generate the proxy.
    StringBuffer buffer = new StringBuffer();

    buffer.append("package " + getProxyName(modelClass).getPackageName() + ";\n");
    buffer.append("\n");
    buffer.append(getGeneratedTag() + "\n");
    buffer.append("public class " + getProxyName(modelClass).getSimpleName() +
            " extends " + proxyClass.getName() + "<" + getInterfaceName(modelClass).getQualifiedName() + ">" +
            " implements " + getInterfaceName(modelClass).getQualifiedName() +
            implementSerializable + " {\n");

    buffer.append(serialVersionUIDField);

    // The constructor.
    buffer.append("\n");
    buffer.append("  public " + getProxyName(modelClass).getSimpleName() +
            "(" + smallSessionClass.getName() + " smallSession) {\n");
    buffer.append("    super(smallSession);\n");
    buffer.append("  }\n");

    for (ModelMethod modelMethod : modelClass.getMethodList()) {
      List<String> parameterTextList = new ArrayList<String>();
      for (ModelParameter modelParameter : modelMethod.getParameterList())
        parameterTextList.add(toString(modelParameter.getType(), modelClass.isLocalSide(), false) +
                " param_" + modelParameter.getName());

      buffer.append("\n");
      buffer.append("  @Override\n");
      buffer.append("  public void " + modelMethod.getName() + "(" + getCommaSeparatedSequence(parameterTextList) + ") {\n");
      
//      if (config.getPlatform() == Platform.RedDwarfServer)
//        buffer.append("    " + com.lemoulinstudio.small.rds.SmallSessionImpl.class.getName() + " smallSession = smallSessionRef.get();\n");
      buffer.append("    java.io.ByteArrayOutputStream byteArrayStream = new java.io.ByteArrayOutputStream();\n");
      buffer.append("    java.io.DataOutputStream outputStream = new java.io.DataOutputStream(byteArrayStream);\n");

      // Open the "try".
      buffer.append("\n");
      buffer.append("    try {\n");
      
      // Encode the method's Id.
      buffer.append("      // Encode the method's Id.\n");
      buffer.append("      " + String.format(writeMethodIdFormat, modelMethod.getMethodId()) + "\n");
      buffer.append("\n");

      // Encode the targeted object's Id.
      buffer.append("      // Encode the targeted object's reference.\n");
      buffer.append(getEncodingSourceCode("      ",
              "this",
              "outputStream",
              new ModelType(modelClass),
              0, false));
      
      // Encode the parameter's values.
      for (ModelParameter modelParameter : modelMethod.getParameterList()) {
        buffer.append("\n");
        buffer.append("      // Encode the parameter param_" + modelParameter.getName() + ".\n");
        buffer.append(getEncodingSourceCode("      ",
                "param_" + modelParameter.getName(),
                "outputStream",
                modelParameter.getType(),
                0, true));
      }

      // Close the "try" with a "catch" which does nothing.
      buffer.append("    }\n");
      buffer.append("    catch (java.io.IOException e) {}\n");
      buffer.append("\n");

      // Eventually log the message.
      if (modelMethod.shouldLogMethodInvocation()) {
        buffer.append("    // Log the method call.\n");
        buffer.append("    smallSession.logText(\"-> \"" + getArgumentLogText("this", modelMethod) + ");\n");
        buffer.append("\n");
      }

      // Send the message.
      buffer.append("    // Send the message.\n");
      buffer.append("    smallSession.sendMessage(byteArrayStream);\n");
      buffer.append("  }\n");
    }

    buffer.append("}\n");

    writeFileContent(getProxyName(modelClass).getQualifiedName(), buffer);
  }

  protected void generateClassDecoder(ModelClass modelClass) {
  }

  protected void writeFileContent(String fileName, CharSequence content) {
    try {
      JavaFileObject f = config.getProcessingEnv().getFiler().createSourceFile(fileName);
      printNote("Generated \"" + fileName.replace(".", File.separator) + ".java\".");
      config.incrementNbFileGenerated();
      
      Writer w = f.openWriter();
      try {
        w.append(content);
        w.flush();
      } finally {
        w.close();
      }
    } catch (IOException e) {
      config.getProcessingEnv().getMessager().printMessage(Diagnostic.Kind.ERROR, e.toString());
    }
  }

  protected String getGeneratedTag() {
    return "/** Build date: " +
            DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
            DateFormat.MEDIUM,
            Locale.UK).format(new Date()) + " */";
  }

  protected String toString(Type type, boolean isReceiverSide, boolean isGenericArgument) {
    return toString(type, isReceiverSide, isGenericArgument, false);
  }
  
  protected String toString(Type type, boolean isReceiverSide, boolean isGenericArgument, boolean noManagedReference) {
    switch (type.getTypeKind()) {
      case Primitive:
        return ((PrimitiveType) type).getPrimitiveClass().getName();
      case Array:
        return toString(((ArrayType) type).getComponentType(), isReceiverSide, false, noManagedReference) + "[]";
      case Enum:
        return ((EnumType) type).getQualifiedClassName();
      case PrimitiveWrapper:
        return ((PrimitiveWrapperType) type).getWrapperClass().getName();
      case Model: {
        ModelClass modelClass = ((ModelType) type).getModelClass();

        // ViewOf<...>
        if (!modelClass.isLocalSide()) {
          if (modelClass.isView()) modelClass = modelClass.getViewedModel();
          else if (modelClass.isViewed()) modelClass = modelClass.getViewedByModel();
        }

        // @ImplementedBy
        String result = "";
        if (modelClass.isImplementationSpecified() && modelClass.isLocalSide())
          result += modelClass.getImplementationQualifiedName();
        else
          result += getInterfaceName(modelClass).getQualifiedName();

        // Add "? extends " if enclosed within a generic type.
        if (isGenericArgument && !isReceiverSide)
          result = "? extends " + result;

        return result;
      }
      case Declared: {
        StringBuffer buffer = new StringBuffer();
        DeclaredType declaredType = (DeclaredType) type;

        // Add "? extends " if enclosed within a generic type.
        if (isGenericArgument && !isReceiverSide) buffer.append("? extends ");
 
        buffer.append(declaredType.getTypeName());

        List<String> argumentTypeTextList = new ArrayList<String>();
        for (Type argumentType : declaredType.getGenericArgumentTypeList())
          argumentTypeTextList.add(toString(argumentType, isReceiverSide, true, noManagedReference));
        if (!argumentTypeTextList.isEmpty())
          buffer.append("<" + getCommaSeparatedSequence(argumentTypeTextList) + ">");

        return buffer.toString();
      }
      default:
        return "<unsupported type kind>";
    }
  }

  private CharSequence getEncodingSourceCode(String indentation,
          String valueName,
          String outputStreamVarName,
          Type parameterType,
          int recursionDepth,
          boolean replaceViewViewed) {
    StringBuffer buffer = new StringBuffer();

    if (parameterType.getTypeKind() == TypeKind.Primitive) {
      PrimitiveType primitiveType = (PrimitiveType) parameterType;
      String primitiveName = primitiveType.getPrimitiveClass().getName();
      buffer.append(indentation + outputStreamVarName + ".write" +
              primitiveName.substring(0, 1).toUpperCase() +
              primitiveName.substring(1).toLowerCase() +
              "(" + valueName + ");\n");
    }

    else if (parameterType.getTypeKind() == TypeKind.PrimitiveWrapper) {
      PrimitiveWrapperType primitiveWrapperType = (PrimitiveWrapperType) parameterType;
      String primitiveName = primitiveWrapperType.getPrimitiveClass().getName();
      buffer.append(indentation + outputStreamVarName + ".write" +
              primitiveName.substring(0, 1).toUpperCase() +
              primitiveName.substring(1).toLowerCase() +
              "(" + valueName + ");\n");
    }

    else if (parameterType.getTypeKind() == TypeKind.Array) {
      ArrayType arrayType = (ArrayType) parameterType;

      CharSequence writeValueBlock = getEncodingSourceCode(
              indentation + "  ",
              "val" + recursionDepth,
              outputStreamVarName,
              arrayType.getComponentType(),
              recursionDepth + 1,
              replaceViewViewed);

      buffer.append(String.format(
              "%1$s%2$s.writeInt(%3$s.length);\n" +
              "%1$sfor (%4$s val%5$s : %3$s) {\n" +
              "%6$s" +
              "%1$s}\n",
              indentation,
              outputStreamVarName,
              valueName,
              arrayType.getComponentType(),
              recursionDepth,
              writeValueBlock));
    }

    else if (parameterType.getTypeKind() == TypeKind.Enum) {
      EnumType enumType = (EnumType) parameterType;
      
      int nbEnumItems = enumType.getNbEnumItems();
      String enumWriteCommand =
              (nbEnumItems <= 256 ? ".writeByte" :
                (nbEnumItems <= 65536 ? ".writeShort" :
                  ".writeInt")) +
                  "(" + valueName + ".ordinal())";

      buffer.append(indentation + outputStreamVarName + enumWriteCommand + ";\n");
    }

    else if (parameterType.getTypeKind() == TypeKind.Model) {
      ModelType modelType = (ModelType) parameterType;
      ModelClass modelClass = modelType.getModelClass();

      // Replace the type of the object that we are going to encode.
      if (replaceViewViewed && !modelClass.isLocalSide()) {
        if (modelClass.isView()) modelClass = modelClass.getViewedModel();
        else if (modelClass.isViewed()) modelClass = modelClass.getViewedByModel();
      }

      if (modelClass.getIdBindingPolicy() == IdBindingPolicy.Singleton) {
        buffer.append(indentation + "// Singletons are not encoded.\n");
      }
      else if (modelClass.isLocalSide()) {
        if (modelClass.isView()) {
          buffer.append(indentation + "smallSession.encodeViewRef(" + valueName +
                  ", " + outputStreamVarName + ");\n");
        }
        else if (modelClass.getIdBindingPolicy() == IdBindingPolicy.SharedId) {
          buffer.append(indentation + "smallSession.encodeObjectSharedId(" + valueName +
                  ", " + outputStreamVarName + ");\n");
        }
        else if (modelClass.getIdBindingPolicy() == IdBindingPolicy.LocalId) {
          buffer.append(indentation + "smallSession.encodeObjectLocalId(" + valueName +
                  ", " + outputStreamVarName + ");\n");
        }
      }
      else {
        buffer.append(indentation + "smallSession.encodeRemoteObjectRef(" + valueName +
                ", " + outputStreamVarName + ");\n");
      }
    }

    else if (parameterType.getTypeKind() == TypeKind.Declared) {
      DeclaredType declaredType = (DeclaredType) parameterType;

      if (declaredType.getTypeClass() == String.class) {
        buffer.append(indentation + outputStreamVarName + ".writeUTF(" + valueName + ");\n");
      }

      else if (declaredType.getTypeClass() == List.class || declaredType.getTypeClass() == Set.class) {
        CharSequence writeValueBlock = getEncodingSourceCode(
                indentation + "  ",
                "val" + recursionDepth,
                outputStreamVarName,
                declaredType.getGenericArgumentTypeList().get(0),
                recursionDepth + 1,
                replaceViewViewed);

        buffer.append(String.format(
                "%1$s%2$s.writeInt(%3$s.size());\n" +
                "%1$sfor (%4$s val%5$s : %3$s) {\n" +
                "%6$s" +
                "%1$s}\n",
                indentation,
                outputStreamVarName,
                valueName,
                toString(declaredType.getGenericArgumentTypeList().get(0), false, false),
                recursionDepth,
                writeValueBlock));
      }

      else if (declaredType.getTypeClass() == Map.class) {
        CharSequence writeKeyBlock = getEncodingSourceCode(
                indentation + "  ",
                "entry" + recursionDepth + ".getKey()",
                outputStreamVarName,
                declaredType.getGenericArgumentTypeList().get(0),
                recursionDepth + 1,
                replaceViewViewed);
        CharSequence writeValueBlock = getEncodingSourceCode(
                indentation + "  ",
                "entry" + recursionDepth + ".getValue()",
                outputStreamVarName,
                declaredType.getGenericArgumentTypeList().get(1),
                recursionDepth + 1,
                replaceViewViewed);

        buffer.append(String.format(
                "%1$s%2$s.writeInt(%3$s.size());\n" +
                "%1$sfor (java.util.Map.Entry<%4$s, %5$s> entry%6$s : %3$s.entrySet()) {\n" +
                "%7$s" +
                "%8$s" +
                "%1$s}\n",
                indentation,
                outputStreamVarName,
                valueName,
                toString(declaredType.getGenericArgumentTypeList().get(0), false, true),
                toString(declaredType.getGenericArgumentTypeList().get(1), false, true),
                recursionDepth,
                writeKeyBlock,
                writeValueBlock));
      }

      else {
        config.getProcessingEnv().getMessager().printMessage(Diagnostic.Kind.ERROR,
                "Unsupported type: " + parameterType + "\n");

        buffer.append(indentation + "// Unsupported type: " + parameterType + "\n");
      }
    }

    return buffer;
  }

  private CharSequence getDecodingSourceCode(String affectationFormat,
          String indentation,
          String targetVariableName,
          String inputStreamVarName,
          Type parameterType,
          int recursionDepth) {
    StringBuffer buffer = new StringBuffer();

    if (parameterType.getTypeKind() == TypeKind.Primitive) {
      PrimitiveType primitiveType = (PrimitiveType) parameterType;
      String primitiveName = primitiveType.getPrimitiveClass().getName();
      String valueName = inputStreamVarName + ".read" +
              primitiveName.substring(0, 1).toUpperCase() +
              primitiveName.substring(1).toLowerCase() + "()";
      buffer.append(indentation + String.format(affectationFormat, targetVariableName, valueName) + ";\n");
    }

    else if (parameterType.getTypeKind() == TypeKind.PrimitiveWrapper) {
      PrimitiveWrapperType primitiveWrapperType = (PrimitiveWrapperType) parameterType;
      String primitiveName = primitiveWrapperType.getPrimitiveClass().getName();
      String valueName = inputStreamVarName + ".read" +
              primitiveName.substring(0, 1).toUpperCase() +
              primitiveName.substring(1).toLowerCase() + "()";
      buffer.append(indentation + String.format(affectationFormat, targetVariableName, valueName) + ";\n");
    }

    else if (parameterType.getTypeKind() == TypeKind.Array) {
      ArrayType arrayType = (ArrayType) parameterType;

      CharSequence initValueBlock = getDecodingSourceCode("%s = %s",
              indentation + "    ",
              "tmp" + recursionDepth + "[i" + recursionDepth + "]",
              inputStreamVarName,
              arrayType.getComponentType(),
              recursionDepth + 1);

      CharSequence affectation = String.format(affectationFormat,
              targetVariableName, "tmp" + recursionDepth);

      buffer.append(String.format(
              "%1$s{\n" +
              "%1$s  %2$s[] tmp%4$d = new %7$s[%3$s.readInt()]%8$s;\n" +
              "%1$s  for (int i%4$d = 0; i%4$d < tmp%4$d.length; i%4$d++)\n" +
              "%5$s" +
              "%1$s  %6$s;\n" +
              "%1$s}\n",
              indentation,
              arrayType.getComponentType(),
              inputStreamVarName,
              recursionDepth,
              initValueBlock,
              affectation,
              arrayType.getTypeWithNoArray(),
              arrayType.bracketToString().substring(2)));
    }

    else if (parameterType.getTypeKind() == TypeKind.Enum) {
      EnumType enumType = (EnumType) parameterType;
      int nbEnumItems = enumType.getNbEnumItems();
        String enumOrdinalAsInt = inputStreamVarName +
                (nbEnumItems <= 256 ? ".readByte() & 0xff" :
                  (nbEnumItems <= 65536 ? ".readShort() & 0xffff" :
                    ".readInt()"));

        buffer.append(indentation + String.format(affectationFormat, targetVariableName,
                enumType.getQualifiedClassName() + ".values()[" + enumOrdinalAsInt + "]") + ";\n");
    }

    else if (parameterType.getTypeKind() == TypeKind.Model) {
      ModelType modelType = (ModelType) parameterType;
      ModelClass modelClass = modelType.getModelClass();

      if (modelClass.isLocalSide()) {
        if (modelClass.isView()) {
          buffer.append(indentation + String.format(affectationFormat, targetVariableName,
                    "(" + toString(modelType, true, false) + ") smallSession." +
                    (modelClass.isSingleton() ? "decodeSingletonViewRef" : "decodeViewRef") +
                    "(" + inputStreamVarName + ", " + getInterfaceName(modelClass.getViewedModel()).getQualifiedName() + ".class)") + ";\n");
        }
        else if (modelClass.getIdBindingPolicy() == IdBindingPolicy.Singleton) {
            buffer.append(indentation + String.format(affectationFormat, targetVariableName,
                    "smallSession.getLocalSingletonRef(" + toString(modelType, true, false, true) + ".class)") + ";\n");
        }
        else if (modelClass.getIdBindingPolicy() == IdBindingPolicy.SharedId) {
            buffer.append(indentation + String.format(affectationFormat, targetVariableName,
                    "smallSession.decodeObjectSharedId(" +
                    inputStreamVarName + ", " + toString(modelType, true, false, true) + ".class)") + ";\n");
        }
        else if (modelClass.getIdBindingPolicy() == IdBindingPolicy.LocalId) {
            buffer.append(indentation + String.format(affectationFormat, targetVariableName,
                    "smallSession.decodeObjectLocalId(" +
                    inputStreamVarName + ", " + toString(modelType, true, false, true) + ".class)") + ";\n");
        }
      }
      else {
        if (modelClass.getIdBindingPolicy() == IdBindingPolicy.Singleton) {
            buffer.append(indentation + String.format(affectationFormat, targetVariableName,
                    "smallSession.getRemoteSingletonRef(" + toString(modelType, true, false, true) + ".class)") + ";\n");
        }
        else {
            buffer.append(indentation + String.format(affectationFormat, targetVariableName,
                    "smallSession.decodeRemoteObjectRef(" +
                    inputStreamVarName + ", " + toString(modelType, true, false, true) + ".class)") + ";\n");
        }
      }
    }

    else if (parameterType.getTypeKind() == TypeKind.Declared) {
      DeclaredType declaredType = (DeclaredType) parameterType;

      if (declaredType.getTypeClass() == String.class) {
        buffer.append(indentation + String.format(affectationFormat, targetVariableName,
                inputStreamVarName + ".readUTF()") + ";\n");
      }

      else if (declaredType.getTypeClass() == List.class) {
        CharSequence initValueBlock = getDecodingSourceCode("%s.add(%s)",
                indentation + "    ",
                "tmp" + recursionDepth,
                inputStreamVarName,
                declaredType.getGenericArgumentTypeList().get(0),
                recursionDepth + 1);

        String affectation = String.format(affectationFormat,
                targetVariableName, "tmp" + recursionDepth);

        buffer.append(String.format(
                "%1$s{\n" +
                "%1$s  int length%4$d = %3$s.readInt();\n" +
                "%1$s  java.util.List<%2$s> tmp%4$d = new java.util.ArrayList<%2$s>(length%4$d);\n" +
                "%1$s  for (int i%4$d = 0; i%4$d < length%4$d; i%4$d++)\n" +
                "%5$s" +
                "%1$s  %6$s;\n" +
                "%1$s}\n",
                indentation,
                toString(declaredType.getGenericArgumentTypeList().get(0), true, false),
                inputStreamVarName,
                recursionDepth,
                initValueBlock,
                affectation));
      }

      else if (declaredType.getTypeClass() == Set.class) {
        CharSequence initValueBlock = getDecodingSourceCode("%s.add(%s)",
                indentation + "    ",
                "tmp" + recursionDepth,
                inputStreamVarName,
                declaredType.getGenericArgumentTypeList().get(0),
                recursionDepth + 1);

        String affectation = String.format(affectationFormat,
                targetVariableName, "tmp" + recursionDepth);

        buffer.append(String.format(
                "%1$s{\n" +
                "%1$s  int length%4$d = %3$s.readInt();\n" +
                "%1$s  java.util.Set<%2$s> tmp%4$d = new java.util.HashSet<%2$s>(length%4$d);\n" +
                "%1$s  for (int i%4$d = 0; i%4$d < length%4$d; i%4$d++)\n" +
                "%5$s" +
                "%1$s  %6$s;\n" +
                "%1$s}\n",
                indentation,
                toString(declaredType.getGenericArgumentTypeList().get(0), true, false),
                inputStreamVarName,
                recursionDepth,
                initValueBlock,
                affectation));
      }

      else if (declaredType.getTypeClass() == Map.class) {
        CharSequence initKeyBlock = getDecodingSourceCode("%s = %s",
                indentation + "    ",
                "key" + recursionDepth,
                inputStreamVarName,
                declaredType.getGenericArgumentTypeList().get(0),
                recursionDepth + 1);
        CharSequence initValueBlock = getDecodingSourceCode("%s = %s",
                indentation + "    ",
                "val" + recursionDepth,
                inputStreamVarName,
                declaredType.getGenericArgumentTypeList().get(1),
                recursionDepth + 1);

        String affectation = String.format(affectationFormat,
                targetVariableName, "tmp" + recursionDepth);

        buffer.append(String.format(
                "%1$s{\n" +
                "%1$s  int length%5$d = %4$s.readInt();\n" +
                "%1$s  java.util.Map<%2$s, %3$s> tmp%5$d = new java.util.HashMap<%2$s, %3$s>(length%5$d);\n" +
                "%1$s  for (int i%5$d = 0; i%5$d < length%5$d; i%5$d++) {\n" +
                "%1$s    %2$s key%5$d;\n" +
                "%1$s    %3$s val%5$d;\n" +
                "%6$s" +
                "%7$s" +
                "%1$s    tmp%5$d.put(key%5$d, val%5$d);\n" +
                "%1$s  }\n" +
                "%1$s  %8$s;\n" +
                "%1$s}\n",
                indentation,
                toString(declaredType.getGenericArgumentTypeList().get(0), true, false),
                toString(declaredType.getGenericArgumentTypeList().get(1), true, false),
                inputStreamVarName,
                recursionDepth,
                initKeyBlock,
                initValueBlock,
                affectation));
      }

      else {
        config.getProcessingEnv().getMessager().printMessage(Diagnostic.Kind.ERROR,
                "Unsupported type: " + parameterType + "\n");

        buffer.append(indentation + "// Unsupported type: " + parameterType + "\n");
      }
    }

    return buffer;
  }
  
  private CharSequence getArgumentLogText(String targetVarName, ModelMethod modelMethod) {
    StringBuffer buffer = new StringBuffer();
    
    buffer.append(" + " + Utils.class.getName() + ".refToString(" + targetVarName +") + \"." + modelMethod.getName() + "(\"");

    for (int i = 0; i < modelMethod.getParameterList().size(); i++) {
      if (i > 0) buffer.append(" + \", \"");

      ModelParameter modelParameter = modelMethod.getParameterList().get(i);
      String parameterName = "param_" + modelParameter.getName();

      if (modelParameter.getType().toString().equals(String.class.getName())) {
        buffer.append(" + \"\\\"\" + " + parameterName + " + \"\\\"\"");
      }
      else if (modelParameter.getType().getTypeKind() == TypeKind.Declared) {
        buffer.append(" + " + Utils.class.getName() + ".refToString(" + parameterName + ")");
      }
      else {
        buffer.append(" + " + parameterName);
      }
    }

    buffer.append(" + \");\"");

    return buffer;
  }

}
