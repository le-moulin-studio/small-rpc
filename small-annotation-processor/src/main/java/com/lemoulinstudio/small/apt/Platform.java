package com.lemoulinstudio.small.apt;

import com.lemoulinstudio.small.apt.model.HostKind;
import com.lemoulinstudio.small.apt.generator.CodeGenerator;
import com.lemoulinstudio.small.apt.generator.CppCodeGenerator;
import com.lemoulinstudio.small.apt.generator.JseCodeGenerator;

/**
 *
 * @author Vincent Cantin
 */
public enum Platform {
  
  JseClient("jse-client", HostKind.Client, JseCodeGenerator.class),
  JseServer("jse-server", HostKind.Server, JseCodeGenerator.class),
  CppClient("cpp-client", HostKind.Client, CppCodeGenerator.class),
  CppServer("cpp-server", HostKind.Server, CppCodeGenerator.class),

//  Jme("jme", HostKind.Client, Language.Java),
//  WiiCpp("wii-cpp", HostKind.Client, Language.Cpp),
//  Xbox360Cpp("xbox360-cpp", HostKind.Client, Language.Cpp),
//  Ps3Cpp("ps3-cpp", HostKind.Client, Language.Cpp),
//  PspCpp("psp-cpp", HostKind.Client, Language.Cpp),
//  NdsCpp("nds-cpp", HostKind.Client, Language.Cpp),
//  WindowsCpp("windows-cpp", HostKind.Client, Language.Cpp),
//  LinuxCpp("linux-cpp", HostKind.Client, Language.Cpp),
//  MacCpp("mac-cpp", HostKind.Client, Language.Cpp),
//  MacObjC("mac-objc", HostKind.Client, Language.ObjectiveC),
//  IPhoneObjC("iphone-objc", HostKind.Client, Language.ObjectiveC),
;

  private final String name;
  private final HostKind hostKind;
  private final Class<? extends CodeGenerator> codeGeneratorClass;

  private Platform(String name,
          HostKind hostKind,
          Class<? extends CodeGenerator> codeGeneratorClass) {
    this.name = name;
    this.hostKind = hostKind;
    this.codeGeneratorClass = codeGeneratorClass;
  }

  public String getName() {
    return name;
  }

  public HostKind getHostKind() {
    return hostKind;
  }

  public CodeGenerator getCodeGeneratorInstance() {
    try {
      return codeGeneratorClass.newInstance();
    } catch (Exception e) {
      return null;
    }
  }

}
