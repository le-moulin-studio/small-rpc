package com.lemoulinstudio.small.apt;

import com.lemoulinstudio.small.apt.generator.CodeGenerator;
import com.lemoulinstudio.small.apt.generator.CppCodeGenerator;
import com.lemoulinstudio.small.apt.generator.JavaCodeGenerator;

/**
 *
 * @author Vincent Cantin
 */
public enum Platform {
  
  Java("java", JavaCodeGenerator.class),
  Cpp("cpp", CppCodeGenerator.class),
  ;

  private final String name;
  private final Class<? extends CodeGenerator> codeGeneratorClass;

  private Platform(String name,
          Class<? extends CodeGenerator> codeGeneratorClass) {
    this.name = name;
    this.codeGeneratorClass = codeGeneratorClass;
  }

  public String getName() {
    return name;
  }

  public CodeGenerator getCodeGeneratorInstance() {
    try {return codeGeneratorClass.newInstance();}
    catch (Exception e) {return null;}
  }

}
