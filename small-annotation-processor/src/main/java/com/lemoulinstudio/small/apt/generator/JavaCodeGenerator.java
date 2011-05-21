package com.lemoulinstudio.small.apt.generator;

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
import com.lemoulinstudio.small.AbstractConfiguration;
import com.lemoulinstudio.small.Decoder;
import com.lemoulinstudio.small.LocalService;
import com.lemoulinstudio.small.Proxy;
import com.lemoulinstudio.small.RemoteService;
import com.lemoulinstudio.small.Response;
import com.lemoulinstudio.small.SmallSessionImpl;
import com.lemoulinstudio.small.apt.type.VoidType;
import com.lemoulinstudio.small.util.Utils;
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
  private Class decoderClass;

  public JavaCodeGenerator() {
    this.smallSessionClass = SmallSessionImpl.class;
    this.generatedConfigClass = AbstractConfiguration.class;
    this.proxyClass = Proxy.class;
    this.decoderClass = Decoder.class;
  }
  
  @Override
  public void generateAll() {

    List<ModelClass> localSideClassList =  modelData.getSameSideModelClassOrderedList();
    List<ModelClass> remoteSideClassList =  modelData.getOtherSideModelClassOrderedList();

    // Generate the configuration file.
    generateConfigFile(remoteSideClassList);

    // Generate the decoder.
    generateRootDecoder(localSideClassList, remoteSideClassList);

    // Generate the interfaces for local objects to implement.
    for (ModelClass modelClass : localSideClassList)
      generateInterface(modelClass);

    // Generate the interface for remote objects which will have a proxy.
    for (ModelClass modelClass : remoteSideClassList)
      generateInterface(modelClass);
    
    // Generate the proxies for remote objects which are not embedded inside the root proxy.
    for (ModelClass modelClass : remoteSideClassList)
      generateClassProxy(modelClass);

  }

  protected void generateConfigFile(List<ModelClass> modelClassList) {
    StringBuilder buffer = new StringBuilder();
    
    buffer.append("package " + config.getConfigurationClassName().getPackageName() + ";\n");
    buffer.append("\n");
    buffer.append("public class " + config.getConfigurationClassName().getSimpleName() +
            " implements " + generatedConfigClass.getName() + " {\n");

    buffer.append("\n");
    
    String mapTypeName = "java.util.Map<Class<? extends " + RemoteService.class.getName() + ">, " +
            "Class<? extends " + proxyClass.getName() + ">>";
    String hashmapTypeName = "java.util.HashMap<Class<? extends " + RemoteService.class.getName() + ">, " +
            "Class<? extends " + proxyClass.getName() + ">>";
    
    buffer.append("  @Override\n");
    buffer.append("  public " + mapTypeName + " getRemoteServiceClassToProxyClass() {\n");
    buffer.append("    " + mapTypeName + " result = new " + hashmapTypeName + "();\n");
    for (ModelClass modelClass : modelClassList)
      buffer.append("    result.put(" + getInterfaceName(modelClass).getQualifiedName() + ".class, " + getProxyName(modelClass).getQualifiedName() + ".class);\n");
    buffer.append("    return result;\n");
    buffer.append("  }\n");

    buffer.append("\n");
    buffer.append("  @Override\n");
    buffer.append("  public " + decoderClass.getName() + " getDecoder() {\n");
    buffer.append("    return new " + config.getRootDecoderClassName().getQualifiedName() + "();\n");
    buffer.append("  }\n");
    
    buffer.append("\n");
    buffer.append("}\n");

    writeFileContent(config.getConfigurationClassName().getQualifiedName(), buffer);
  }

  protected void generateRootDecoder(
          List<ModelClass> localSideClassList,
          List<ModelClass> remoteSideClassList) {
    StringBuilder buffer = new StringBuilder();

    buffer.append("package " + config.getRootDecoderClassName().getPackageName() + ";\n");
    buffer.append("\n");
    buffer.append("public class " + config.getRootDecoderClassName().getSimpleName() +
            " implements " + decoderClass.getName() + " {\n");

    buffer.append("\n");
    buffer.append("  @Override\n");
    buffer.append("  public void decodeAndInvoke(" + smallSessionClass.getName() + " smallSession, java.io.DataInputStream inputStream) throws java.io.IOException {\n");

    buffer.append("    int methodId = ");
    if (modelData.getNumberOfMethodsOnSameSide() <= 1) buffer.append("0;\n");
    else if (modelData.getNumberOfMethodsOnSameSide() <= 256) buffer.append("inputStream.readUnsignedByte();\n");
    else if (modelData.getNumberOfMethodsOnSameSide() <= 65536) buffer.append("inputStream.readUnsignedShort();\n");
    else buffer.append("inputStream.readInt();\n");

    buffer.append("    switch (methodId) {\n");
    for (ModelClass modelClass : localSideClassList) {
      for (ModelMethod modelMethod : modelClass.getMethodList()) {
        buffer.append("      // " + getInterfaceName(modelClass).getQualifiedName() + "." + modelMethod.getName() + "()\n");
        buffer.append("      case " + modelMethod.getMethodId() + ": {\n");

        // Find the message's target.
        buffer.append("        // Find the target of the message.\n");
        buffer.append("        " + getInterfaceName(modelClass).getQualifiedName() +
                " service = smallSession.getLocalService(" +
                getInterfaceName(modelClass).getQualifiedName() +
                ".class);\n");
        
        // Declare the parameters.
        if (!modelMethod.getParameterList().isEmpty()) {
          buffer.append("\n");
          buffer.append("        // Declare the parameters.\n");
          for (ModelParameter modelParameter : modelMethod.getParameterList()) {
            buffer.append("        " + toString(modelParameter.getType()) + " param_" + modelParameter.getName() + ";\n");
          }
        }

        // Decode the parameters.
        for (ModelParameter modelParameter : modelMethod.getParameterList()) {
          buffer.append("\n");
          buffer.append("        // Decode the parameter param_" + modelParameter.getName() + ".\n");
          if (modelParameter.isCallerObject()) {
            buffer.append("        param_" + modelParameter.getName() + " = (" +
                    toString(modelParameter.getType()) + ") smallSession.getCallerObject();\n");
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
          buffer.append("        smallSession.logText(\"<- \"" + getArgumentLogText("service", modelMethod) + ");\n");
        }

        // Call the method.
        buffer.append("\n");
        buffer.append("        // Call the method of the service.\n");
        List<String> parameterNameList = new ArrayList<String>();
        for (ModelParameter modelParameter : modelMethod.getParameterList())
          parameterNameList.add("param_" + modelParameter.getName());
        buffer.append("        " +
                (modelMethod.getReturnType() == VoidType.instance ? "" : toString(modelMethod.getReturnType()) + " returnValue = ") +
                "service." + modelMethod.getName() + "(" + getCommaSeparatedSequence(parameterNameList) + ");\n");
        
        // Encode and send the return value.
        if (modelMethod.getReturnType() != VoidType.instance) {
          buffer.append("\n");
          buffer.append("        java.io.ByteArrayOutputStream byteArrayOutputStream = new java.io.ByteArrayOutputStream();\n");
          buffer.append("        java.io.DataOutputStream outputStream = new java.io.DataOutputStream(byteArrayOutputStream);\n");
          buffer.append("\n");
          
          // Encode the method's Id.
          buffer.append("        // Encode the return method id.\n");
          String writeMethodIdFormat;
          if (modelData.getNumberOfMethodsOnOtherSide() <= 1) writeMethodIdFormat = "// No need.";
          else if (modelData.getNumberOfMethodsOnOtherSide() <= 256) writeMethodIdFormat = "outputStream.writeByte(%s);";
          else if (modelData.getNumberOfMethodsOnOtherSide() <= 65536) writeMethodIdFormat = "outputStream.writeShort(%s);";
          else writeMethodIdFormat = "outputStream.writeInt(%s);";
          buffer.append("        " + String.format(writeMethodIdFormat, modelMethod.getReturnMethodId()) + "\n");
          buffer.append("\n");
          
          buffer.append("        // Encode the return value.\n");
          buffer.append(getEncodingSourceCode("        ",
                  "returnValue",
                  "outputStream",
                  modelMethod.getReturnType(),
                  0));
          buffer.append("\n");
          
          buffer.append("        // Send the return value back to the caller.\n");
          buffer.append("        smallSession.sendMessage(byteArrayOutputStream);\n");
        }

        buffer.append("\n");
        buffer.append("        break;\n");
        buffer.append("      }\n");
        buffer.append("\n");
      }
    }

    for (ModelClass modelClass : remoteSideClassList) {
      for (ModelMethod modelMethod : modelClass.getMethodList()) {
        if (modelMethod.getReturnType() != VoidType.instance) {
          buffer.append("      // Response from " +
                  getInterfaceName(modelClass).getQualifiedName() +
                  "." + modelMethod.getName() + "()\n");
          buffer.append("      case " + modelMethod.getReturnMethodId() + ": {\n");

          // Declare the return value.
          buffer.append("        // Declare return value.\n");
          buffer.append("        " + toString(modelMethod.getReturnType()) + " returnValue;\n");
          buffer.append("\n");

          // Decode the return value.
          buffer.append("        // Decode the return value.\n");
          buffer.append(getDecodingSourceCode("%s = %s",
                  "        ",
                  "returnValue",
                  "inputStream",
                  modelMethod.getReturnType(),
                  0));
          buffer.append("\n");
          
          // Find the reponse container.
          buffer.append("        // Find the reponse container.\n");
          buffer.append("        " + Response.class.getName() +
                  " response = smallSession.pullResponseFromQueue();\n");
          buffer.append("\n");
          
          // Store the response value in the container.
          buffer.append("        // Store the response value in the container.\n");
          buffer.append("        response.setValue(returnValue);\n");

          // Eventually log the message.
//          if (modelMethod.shouldLogMessageReception()) {
//            buffer.append("\n");
//            buffer.append("        // Log the message reception.\n");
//            buffer.append("        smallSession.logText(\"<- \"" + getArgumentLogText("service", modelMethod) + ");\n");
//          }
          
          buffer.append("\n");
          buffer.append("        break;\n");
          buffer.append("      }\n");
          buffer.append("\n");
        }
      }
    }
    
    buffer.append("      default:\n");
    buffer.append("    }\n");
    buffer.append("  }\n");
    buffer.append("\n");
    buffer.append("}\n");

    writeFileContent(config.getRootDecoderClassName().getQualifiedName(), buffer);
  }

  protected void generateInterface(ModelClass modelClass) {
    StringBuilder buffer = new StringBuilder();

    buffer.append("package " + getInterfaceName(modelClass).getPackageName() + ";\n");
    buffer.append("\n");
    buffer.append(getGeneratedTag() + "\n");
    buffer.append("public interface " + getInterfaceName(modelClass).getSimpleName());

    List<String> superInterfaceList = new ArrayList<String>();

    superInterfaceList.add((modelClass.isLocalSide() ?
            LocalService.class : RemoteService.class).getName());
    
    buffer.append(" extends " + getCommaSeparatedSequence(superInterfaceList) + " {\n");

    for (ModelMethod modelMethod : modelClass.getMethodList()) {
      List<String> parameterTextList = new ArrayList<String>();
      for (ModelParameter modelParameter : modelMethod.getParameterList())
        parameterTextList.add(toString(modelParameter.getType()) +
                " " + modelParameter.getName());

      String returnTypeName = toString(modelMethod.getReturnType());
      
      buffer.append("  public " + returnTypeName + " " + modelMethod.getName() + "(" + getCommaSeparatedSequence(parameterTextList) + ");\n");
    }

    buffer.append("}\n");

    writeFileContent(getInterfaceName(modelClass).getQualifiedName(), buffer);
  }

  protected void generateClassProxy(ModelClass modelClass) {
    // Choose the right way to encode the methodIDs with respect to their cardinal.
    String writeMethodIdFormat;
    if (modelData.getNumberOfMethodsOnOtherSide() <= 1) writeMethodIdFormat = "// No need.";
    else if (modelData.getNumberOfMethodsOnOtherSide() <= 256) writeMethodIdFormat = "outputStream.writeByte(%s);";
    else if (modelData.getNumberOfMethodsOnOtherSide() <= 65536) writeMethodIdFormat = "outputStream.writeShort(%s);";
    else writeMethodIdFormat = "outputStream.writeInt(%s);";

    // Start to generate the proxy.
    StringBuilder buffer = new StringBuilder();

    buffer.append("package " + getProxyName(modelClass).getPackageName() + ";\n");
    buffer.append("\n");
    buffer.append(getGeneratedTag() + "\n");
    buffer.append("public class " + getProxyName(modelClass).getSimpleName() +
            " extends " + proxyClass.getName() + "<" + getInterfaceName(modelClass).getQualifiedName() + ">" +
            " implements " + getInterfaceName(modelClass).getQualifiedName() + " {\n");

    // The constructor.
    buffer.append("\n");
    buffer.append("  public " + getProxyName(modelClass).getSimpleName() +
            "(" + smallSessionClass.getName() + " smallSession) {\n");
    buffer.append("    super(smallSession);\n");
    buffer.append("  }\n");

    for (ModelMethod modelMethod : modelClass.getMethodList()) {
      List<String> parameterTextList = new ArrayList<String>();
      for (ModelParameter modelParameter : modelMethod.getParameterList())
        parameterTextList.add(toString(modelParameter.getType()) + " param_" + modelParameter.getName());

      String returnTypeName = toString(modelMethod.getReturnType());
      
      buffer.append("\n");
      buffer.append("  @Override\n");
      buffer.append("  public " + returnTypeName + " " + modelMethod.getName() + "(" + getCommaSeparatedSequence(parameterTextList) + ") {\n");
      
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

      // Encode the parameter's values.
      for (ModelParameter modelParameter : modelMethod.getParameterList()) {
        buffer.append("\n");
        buffer.append("      // Encode the parameter param_" + modelParameter.getName() + ".\n");
        buffer.append(getEncodingSourceCode("      ",
                "param_" + modelParameter.getName(),
                "outputStream",
                modelParameter.getType(),
                0));
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

      if (modelMethod.getReturnType() == VoidType.instance) {
        // Send the message.
        buffer.append("    // Send the message.\n");
        buffer.append("    smallSession.sendMessage(byteArrayStream);\n");
      }
      else {
        // Create the response container.
        buffer.append("    // Create the response container.\n");
        buffer.append("    " + Response.class.getName() + "<" + returnTypeName +
                "> _response_ = new " + Response.class.getName() + "<" + returnTypeName + ">();\n");
        buffer.append("    \n");

        // Send the message.
        buffer.append("    // Send the message.\n");
        buffer.append("    smallSession.sendMessage(_response_, byteArrayStream);\n");
        buffer.append("    \n");
        
        // And wait for the response.
        buffer.append("    // Wait and return the response's value.\n");
        buffer.append("    return _response_.waitForValue();\n");
      }
      
      buffer.append("  }\n");
    }

    buffer.append("}\n");

    writeFileContent(getProxyName(modelClass).getQualifiedName(), buffer);
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

  protected String toString(Type type) {
    switch (type.getTypeKind()) {
      case Void:
        return "void";
      case Primitive:
        return ((PrimitiveType) type).getPrimitiveClass().getName();
      case Array:
        return toString(((ArrayType) type).getComponentType()) + "[]";
      case Enum:
        return ((EnumType) type).getQualifiedClassName();
      case PrimitiveWrapper:
        return ((PrimitiveWrapperType) type).getWrapperClass().getName();
      case Model: {
        ModelClass modelClass = ((ModelType) type).getModelClass();
        return getInterfaceName(modelClass).getQualifiedName();
      }
      case Declared: {
        StringBuilder buffer = new StringBuilder();
        DeclaredType declaredType = (DeclaredType) type;

        buffer.append(declaredType.getTypeName());

        List<String> argumentTypeTextList = new ArrayList<String>();
        for (Type argumentType : declaredType.getGenericArgumentTypeList())
          argumentTypeTextList.add(toString(argumentType));
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
          int recursionDepth) {
    StringBuilder buffer = new StringBuilder();

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
              recursionDepth + 1);

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
                recursionDepth + 1);

        buffer.append(String.format(
                "%1$s%2$s.writeInt(%3$s.size());\n" +
                "%1$sfor (%4$s val%5$s : %3$s) {\n" +
                "%6$s" +
                "%1$s}\n",
                indentation,
                outputStreamVarName,
                valueName,
                toString(declaredType.getGenericArgumentTypeList().get(0)),
                recursionDepth,
                writeValueBlock));
      }

      else if (declaredType.getTypeClass() == Map.class) {
        CharSequence writeKeyBlock = getEncodingSourceCode(
                indentation + "  ",
                "entry" + recursionDepth + ".getKey()",
                outputStreamVarName,
                declaredType.getGenericArgumentTypeList().get(0),
                recursionDepth + 1);
        CharSequence writeValueBlock = getEncodingSourceCode(
                indentation + "  ",
                "entry" + recursionDepth + ".getValue()",
                outputStreamVarName,
                declaredType.getGenericArgumentTypeList().get(1),
                recursionDepth + 1);

        buffer.append(String.format(
                "%1$s%2$s.writeInt(%3$s.size());\n" +
                "%1$sfor (java.util.Map.Entry<%4$s, %5$s> entry%6$s : %3$s.entrySet()) {\n" +
                "%7$s" +
                "%8$s" +
                "%1$s}\n",
                indentation,
                outputStreamVarName,
                valueName,
                toString(declaredType.getGenericArgumentTypeList().get(0)),
                toString(declaredType.getGenericArgumentTypeList().get(1)),
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
    StringBuilder buffer = new StringBuilder();

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

//    else if (parameterType.getTypeKind() == TypeKind.Model) {
//      ModelType modelType = (ModelType) parameterType;
//      ModelClass modelClass = modelType.getModelClass();
//
//      if (modelClass.isLocalSide())
//        if (modelClass.getIdBindingPolicy() == IdBindingPolicy.Singleton)
//          buffer.append(indentation + String.format(affectationFormat, targetVariableName,
//                  "smallSession.getLocalSingletonRef(" + toString(modelType, true, false, true) + ".class)") + ";\n");
//    }

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
                toString(declaredType.getGenericArgumentTypeList().get(0)),
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
                toString(declaredType.getGenericArgumentTypeList().get(0)),
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
                toString(declaredType.getGenericArgumentTypeList().get(0)),
                toString(declaredType.getGenericArgumentTypeList().get(1)),
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
    StringBuilder buffer = new StringBuilder();
    
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
