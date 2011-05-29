package com.lemoulinstudio.small.apt.oom;

import com.lemoulinstudio.small.apt.APConfig;
import com.lemoulinstudio.small.apt.model.Caller;
import com.lemoulinstudio.small.apt.model.Log;
import com.lemoulinstudio.small.apt.model.Service;
import com.lemoulinstudio.small.apt.model.NoLog;
import com.lemoulinstudio.small.apt.model.LogSide;
import com.lemoulinstudio.small.apt.type.ArrayType;
import com.lemoulinstudio.small.apt.type.DeclaredType;
import com.lemoulinstudio.small.apt.type.EnumType;
import com.lemoulinstudio.small.apt.type.ModelType;
import com.lemoulinstudio.small.apt.type.PrimitiveType;
import com.lemoulinstudio.small.apt.type.PrimitiveWrapperType;
import com.lemoulinstudio.small.apt.type.Type;
import com.lemoulinstudio.small.apt.type.VoidType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

/**
 *
 * @author Vincent Cantin
 */
public class ModelFactory {

  private APConfig config;

  private ModelData modelData;

  private Map<TypeKind, Class> primitiveTypeKindToPrimitiveClass;
  private Map<TypeElement, ModelClass> modelClassElementToModelClass;
  private Map<String, VoClass> classNameToVoClass;

  public ModelFactory(APConfig config) {
    this.config = config;

    modelData = new ModelData();

    primitiveTypeKindToPrimitiveClass = new HashMap<TypeKind, Class>();
    primitiveTypeKindToPrimitiveClass.put(TypeKind.BOOLEAN, boolean.class);
    primitiveTypeKindToPrimitiveClass.put(TypeKind.CHAR,    char.class);
    primitiveTypeKindToPrimitiveClass.put(TypeKind.BYTE,    byte.class);
    primitiveTypeKindToPrimitiveClass.put(TypeKind.SHORT,   short.class);
    primitiveTypeKindToPrimitiveClass.put(TypeKind.INT,     int.class);
    primitiveTypeKindToPrimitiveClass.put(TypeKind.LONG,    long.class);
    primitiveTypeKindToPrimitiveClass.put(TypeKind.FLOAT,   float.class);
    primitiveTypeKindToPrimitiveClass.put(TypeKind.DOUBLE,  double.class);

    modelClassElementToModelClass = new HashMap<TypeElement, ModelClass>();
    
    classNameToVoClass = new HashMap<String, VoClass>();
    
    setupModelData();
  }

  public ModelData getModelData() {
    return modelData;
  }

  private void setupModelData() {
    // 1st pass: create the instances of ModelClass.
    for (Element element : config.getRoundEnv().getElementsAnnotatedWith(Service.class)) {
      TypeElement classElement = (TypeElement) element;
      String qualifiedName = classElement.getQualifiedName().toString();
      if (qualifiedName.startsWith(config.getInputLocalBasePackage()) ||
          qualifiedName.startsWith(config.getInputRemoteBasePackage()))
        modelClassElementToModelClass.put(classElement, new ModelClass());
    }

    // 2nd pass: setup their content and their relationships.
    for (Map.Entry<TypeElement, ModelClass> entry : modelClassElementToModelClass.entrySet())
      setupModelClass(entry.getValue(), entry.getKey());

    // 3rd pass: sort the classes, then assign to each method a unique ID.
    List<ModelClass> modelClassList = new ArrayList<ModelClass>(modelClassElementToModelClass.values());
    Collections.sort(modelClassList, new ModelClassComparator());
    for (ModelClass modelClass : modelClassList)
      assignMethodIDs(modelClass);

    // Store the modelClass instances in the modelData.
    modelData.modelClassSet.addAll(modelClassList);
    for (ModelClass modelClass : modelClassList) {
      if (modelClass.isLocalSide())
        modelData.sameSideModelClassOrderedList.add(modelClass);
      else
        modelData.otherSideModelClassOrderedList.add(modelClass);
    }
    
    // Store the voClass instances in the modelData.
//    for (VoClass voClass : classNameToVoClass.values()) {
//      modelData.classNameToVoClass.put(voClass.type.getTypeName(), voClass);
//    }
    modelData.classNameToVoClass = classNameToVoClass;
  }

  private void setupModelClass(ModelClass modelClass, TypeElement classElement) {
    modelClass.qualifiedName = classElement.getQualifiedName().toString();
    modelClass.isLocalSide = modelClass.qualifiedName.startsWith(config.getInputLocalBasePackage());

    // Methods.
    for (Element enclosedElement : classElement.getEnclosedElements()) {
      assert enclosedElement.getKind() == ElementKind.METHOD : classElement.getQualifiedName().toString() + " should only contain methods.";
      ExecutableElement methodElement = (ExecutableElement) enclosedElement;

      modelClass.methodList.add(createModelMethod(modelClass, methodElement));
    }
  }

  private void assignMethodIDs(ModelClass modelClass) {
      for (ModelMethod modelMethod : modelClass.getMethodList()) {
        if (modelClass.isLocalSide()) {
          modelMethod.methodId = modelData.sameSideMethodId++;
          if (modelMethod.returnType != VoidType.instance)
            modelMethod.returnMethodId = modelData.otherSideMethodId++;
        }
        else {
          modelMethod.methodId = modelData.otherSideMethodId++;
          if (modelMethod.returnType != VoidType.instance)
            modelMethod.returnMethodId = modelData.sameSideMethodId++;
        }
      }
  }

  private ModelMethod createModelMethod(ModelClass parentModelClass, ExecutableElement methodElement) {
    ModelMethod modelMethod = new ModelMethod();
    modelMethod.parentModelClass = parentModelClass;
    modelMethod.name = methodElement.getSimpleName().toString();

    // By default, methods are not log if not annotated.
    modelMethod.logMethodInvocation = false;
    modelMethod.logMessageReception = false;

    // @Log policies at class level.
    Log classElementLogAnnotation = methodElement.getEnclosingElement().getAnnotation(Log.class);
    if (classElementLogAnnotation != null) {
      LogSide logSide = classElementLogAnnotation.value();
      if (logSide.hasInvocation()) modelMethod.logMethodInvocation = true;
      if (logSide.hasReception())  modelMethod.logMessageReception = true;
    }

    // @NoLog policies at method level.
    NoLog methodElementNoLogAnnotation = methodElement.getAnnotation(NoLog.class);
    if (methodElementNoLogAnnotation != null) {
      LogSide logSide = methodElementNoLogAnnotation.value();
      if (logSide.hasInvocation()) modelMethod.logMethodInvocation = false;
      if (logSide.hasReception())  modelMethod.logMessageReception = false;
    }
    else {
      // @Log policies at method level.
      Log methodElementLogAnnotation = methodElement.getAnnotation(Log.class);
      if (methodElementLogAnnotation != null) {
        LogSide logSide = methodElementLogAnnotation.value();
        if (logSide.hasInvocation()) modelMethod.logMethodInvocation = true;
        if (logSide.hasReception())  modelMethod.logMessageReception = true;
      }
    }

    // Compilation parameter overrides everything.
    if (config.isNoLog()) {
      modelMethod.logMethodInvocation = false;
      modelMethod.logMessageReception = false;
    }
    
    // Parameters.
    for (VariableElement variableElement : methodElement.getParameters()) {
      ModelParameter modelParameter = createModelParameter(modelMethod, variableElement);
      if (!modelParameter.isCallerObject || parentModelClass.isLocalSide())
        modelMethod.parameterList.add(modelParameter);
    }

    // Return type.
    modelMethod.returnType = createType(methodElement.getReturnType());

    return modelMethod;
  }

  private ModelParameter createModelParameter(ModelMethod parentModelMethod, VariableElement parameterElement) {
    ModelParameter modelParameter = new ModelParameter();
    modelParameter.parentModelMethod = parentModelMethod;
    modelParameter.type = createType(parameterElement.asType());
    modelParameter.name = parameterElement.getSimpleName().toString();
    
    modelParameter.isCallerObject = hasCallerType(parameterElement.asType());
    if (modelParameter.isCallerObject) {
      // Replace the type by the one specified in the config.
      modelParameter.type = new DeclaredType(
              config.getCallerObjectClassName().getQualifiedName(),
              null, Collections.<Type>emptyList(), Collections.<Type>emptyList());
    }
    
    return modelParameter;
  }

  private Type createType(TypeMirror parameterType) {
    TypeKind parameterTypeKind = parameterType.getKind();

    // If this is not a type.
    if (parameterTypeKind == TypeKind.NONE) {
      return null;
    }

    // If this is the void type.
    if (parameterTypeKind == TypeKind.VOID) {
      return VoidType.instance;
    }

    // If the type is a primitive.
    if (parameterTypeKind.isPrimitive()) {
      return new PrimitiveType(primitiveTypeKindToPrimitiveClass.get(parameterTypeKind));
    }

    // If this type is an array.
    if (parameterTypeKind == TypeKind.ARRAY) {
      return new ArrayType(createType(((javax.lang.model.type.ArrayType) parameterType).getComponentType()));
    }

    // This type is an enum, a class or an interface.
    assert parameterTypeKind == TypeKind.DECLARED;
    javax.lang.model.type.DeclaredType parameterDeclaredType = (javax.lang.model.type.DeclaredType) parameterType;
    TypeElement typeElement = (TypeElement) parameterDeclaredType.asElement();
    String elementName = typeElement.getQualifiedName().toString();

    // If this is an enum.
    if (typeElement.getSuperclass().toString().startsWith(Enum.class.getCanonicalName() + "<")) {
      List<String> enumItems = new ArrayList<String>();
      for (VariableElement variableElement : ElementFilter.fieldsIn(typeElement.getEnclosedElements()))
        enumItems.add(variableElement.getSimpleName().toString());
      EnumType enumType = new EnumType(typeElement.getQualifiedName().toString(), enumItems);
      modelData.enumTypes.add(enumType);
      return enumType;
    }

    // If this type is a primitive wrapper.
    if (PrimitiveWrapperType.isWrapperClass(elementName)) {
      return new PrimitiveWrapperType(elementName);
    }

    // If this type is a model class.
    if (modelClassElementToModelClass.containsKey(typeElement)) {
      return new ModelType(modelClassElementToModelClass.get(typeElement));
    }

    // It is a general class of the form Foo<Bar0, Bar1, ...>,
    // we consider it as a value object.
    {
      String typeName = typeElement.getQualifiedName().toString();
      Type superType = createType(typeElement.getSuperclass());

      // Not used for now.
      List<Type> implementedTypeList = Collections.<Type>emptyList();

      List<? extends TypeMirror> typeArgumentList = parameterDeclaredType.getTypeArguments();
      List<Type> genericArgumentTypeList = new ArrayList<Type>();
      for (TypeMirror typeArgument : typeArgumentList)
        genericArgumentTypeList.add(createType(typeArgument));

      DeclaredType declaredType = new DeclaredType(typeName, superType, implementedTypeList, genericArgumentTypeList);
      
      final List<Class> speciallyHandledClasses = Arrays.<Class>asList(Object.class, String.class, Collection.class, List.class, Set.class, Map.class);
      if (!speciallyHandledClasses.contains(declaredType.getTypeClass())) {
        getVoClass(typeElement).type = declaredType;
      }
      
      return declaredType;
    }
  }

  private VoClass getVoClass(TypeElement classElement) {
    if (classNameToVoClass.containsKey(classElement.toString()))
      return classNameToVoClass.get(classElement.toString());
    
    VoClass voClass = new VoClass();
    classNameToVoClass.put(classElement.toString(), voClass);
    
    //voClass.type = null;
    
    // Create the fields.
    for (Element enclosedElement : classElement.getEnclosedElements()) {
      if (enclosedElement.getKind() == ElementKind.FIELD) {
        VariableElement fieldElement = (VariableElement) enclosedElement;
        voClass.fieldList.add(createModelField(voClass, fieldElement));
      }
    }
    
    // Short the fields according to their name.
    Collections.sort(voClass.fieldList, new ModelFieldComparator());
    
    return voClass;
  }

  private ModelField createModelField(VoClass voClass, VariableElement fieldElement) {
    ModelField field = new ModelField();
    field.parentVoClass = voClass;
    field.type = createType(fieldElement.asType());
    field.name = fieldElement.getSimpleName().toString();
    return field;
  }

  private boolean hasCallerType(TypeMirror parameterType) {
    if (parameterType.getKind() != TypeKind.DECLARED) return false;
    javax.lang.model.type.DeclaredType parameterDeclaredType = (javax.lang.model.type.DeclaredType) parameterType;
    TypeElement typeElement = (TypeElement) parameterDeclaredType.asElement();
    return typeElement.getQualifiedName().contentEquals(Caller.class.getName());
  }

  private static class ModelClassComparator implements Comparator<ModelClass> {
    @Override
    public int compare(ModelClass o1, ModelClass o2) {
      return o1.getQualifiedName().compareTo(o2.getQualifiedName());
    }
  }

  private static class ModelFieldComparator implements Comparator<ModelField> {
    @Override
    public int compare(ModelField o1, ModelField o2) {
      return o1.name.compareTo(o2.name);
    }
  }

}
