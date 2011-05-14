package com.lemoulinstudio.small.apt.oom;

import com.lemoulinstudio.small.apt.APConfig;
import com.lemoulinstudio.small.apt.model.BindToLocalId;
import com.lemoulinstudio.small.apt.model.BindToSharedId;
import com.lemoulinstudio.small.apt.model.CallerObject;
import com.lemoulinstudio.small.apt.model.ImplementedBy;
import com.lemoulinstudio.small.apt.model.Log;
import com.lemoulinstudio.small.apt.model.NetworkInterface;
import com.lemoulinstudio.small.apt.model.NoLog;
import com.lemoulinstudio.small.apt.model.Singleton;
import com.lemoulinstudio.small.apt.model.View;
import com.lemoulinstudio.small.apt.model.Side;
import com.lemoulinstudio.small.apt.type.ArrayType;
import com.lemoulinstudio.small.apt.type.DeclaredType;
import com.lemoulinstudio.small.apt.type.EnumType;
import com.lemoulinstudio.small.apt.type.ModelType;
import com.lemoulinstudio.small.apt.type.PrimitiveType;
import com.lemoulinstudio.small.apt.type.PrimitiveWrapperType;
import com.lemoulinstudio.small.apt.type.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    
    setupModelData();
  }

  public ModelData getModelData() {
    return modelData;
  }

  private void setupModelData() {
    // 1st pass: create the instances of ModelClass.
    for (Element classElement : config.getRoundEnv().getElementsAnnotatedWith(NetworkInterface.class))
      modelClassElementToModelClass.put((TypeElement) classElement, new ModelClass());

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
      if (config.getPlatform().getHostKind() == modelClass.getHostKind())
        modelData.sameSideModelClassOrderedList.add(modelClass);
      else
        modelData.otherSideModelClassOrderedList.add(modelClass);
    }
  }

  private void setupModelClass(ModelClass modelClass, TypeElement classElement) {
    modelClass.hostKind = classElement.getAnnotation(NetworkInterface.class).value();
    modelClass.isLocalSide = (modelClass.hostKind == config.getPlatform().getHostKind());
    modelClass.idBindingPolicy = IdBindingPolicy.LocalId;
    modelClass.implementedBy = classElement.getAnnotation(ImplementedBy.class);
    modelClass.qualifiedName = classElement.getQualifiedName().toString();

    // @Singleton / @BindToLocalId / @BindToSharedId.
    if (classElement.getAnnotation(Singleton.class) != null)
      modelClass.idBindingPolicy = IdBindingPolicy.Singleton;
    else if (classElement.getAnnotation(BindToSharedId.class) != null)
      modelClass.idBindingPolicy = IdBindingPolicy.SharedId;
    else if (classElement.getAnnotation(BindToLocalId.class) != null)
      modelClass.idBindingPolicy = IdBindingPolicy.LocalId;
    
    // ViewedModel / ViewedBy.
    TypeElement viewedElement = getClassElementRelationTag(classElement, View.class, 0);
    ModelClass viewedModelClass = modelClassElementToModelClass.get(viewedElement);
    if (viewedModelClass != null) {
      modelClass.viewedModel = viewedModelClass;
      viewedModelClass.viewedByModel = modelClass;

      // A view of a singleton a is singleton too.
      if (viewedElement.getAnnotation(Singleton.class) != null)
        modelClass.idBindingPolicy = IdBindingPolicy.Singleton;
    }

    // Methods.
    for (Element enclosedElement : classElement.getEnclosedElements()) {
      assert enclosedElement.getKind() == ElementKind.METHOD : classElement.getQualifiedName().toString() + " should only contains methods.";
      ExecutableElement methodElement = (ExecutableElement) enclosedElement;
      assert methodElement.getReturnType().getKind() == javax.lang.model.type.TypeKind.VOID;

      modelClass.methodList.add(createModelMethod(modelClass, methodElement));
    }
  }

  private void assignMethodIDs(ModelClass modelClass) {
    if (config.getPlatform().getHostKind() == modelClass.getHostKind())
      for (ModelMethod modelMethod : modelClass.getMethodList())
        modelMethod.methodId = modelData.sameSideMethodId++;
    else
      for (ModelMethod modelMethod : modelClass.getMethodList())
        modelMethod.methodId = modelData.otherSideMethodId++;
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
      Side side = classElementLogAnnotation.value();
      if (side.isInvocationSide()) modelMethod.logMethodInvocation = true;
      if (side.isReceptionSide())  modelMethod.logMessageReception = true;
    }

    // @NoLog policies at method level.
    NoLog methodElementNoLogAnnotation = methodElement.getAnnotation(NoLog.class);
    if (methodElementNoLogAnnotation != null) {
      Side side = methodElementNoLogAnnotation.value();
      if (side.isInvocationSide()) modelMethod.logMethodInvocation = false;
      if (side.isReceptionSide())  modelMethod.logMessageReception = false;
    }
    else {
      // @Log policies at method level.
      Log methodElementLogAnnotation = methodElement.getAnnotation(Log.class);
      if (methodElementLogAnnotation != null) {
        Side side = methodElementLogAnnotation.value();
        if (side.isInvocationSide()) modelMethod.logMethodInvocation = true;
        if (side.isReceptionSide())  modelMethod.logMessageReception = true;
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
      if (!modelParameter.isCallerObject || config.getPlatform().getHostKind() == parentModelClass.hostKind)
        modelMethod.parameterList.add(modelParameter);
    }

    return modelMethod;
  }

  private ModelParameter createModelParameter(ModelMethod parentModelMethod, VariableElement parameterElement) {
    ModelParameter modelParameter = new ModelParameter();
    modelParameter.parentModelMethod = parentModelMethod;
    modelParameter.type = createType(parameterElement.asType());
    modelParameter.name = parameterElement.getSimpleName().toString();
    modelParameter.isCallerObject = parameterElement.getAnnotation(CallerObject.class) != null;
    
    return modelParameter;
  }

  private Type createType(TypeMirror parameterType) {
    TypeKind parameterTypeKind = parameterType.getKind();

    // If this is not a type.
    if (parameterTypeKind == TypeKind.NONE) {
      return null;
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
      int nbEnumConstant = ElementFilter.fieldsIn(typeElement.getEnclosedElements()).size();
      return new EnumType(typeElement.getQualifiedName().toString(), nbEnumConstant);
    }

    // If this type is a primitive wrapper.
    if (PrimitiveWrapperType.isWrapperClass(elementName)) {
      return new PrimitiveWrapperType(elementName);
    }

    // If this type is a model class.
    if (modelClassElementToModelClass.containsKey(typeElement)) {
      return new ModelType(modelClassElementToModelClass.get(typeElement));
    }

    // It is a class Foo<Bar0, Bar1, ...>
    {
      String typeName = typeElement.getQualifiedName().toString();
      Type superType = createType(typeElement.getSuperclass());

      // Not used for now.
      List<Type> implementedTypeList = Collections.<Type>emptyList();

      List<? extends TypeMirror> typeArgumentList = parameterDeclaredType.getTypeArguments();
      List<Type> genericArgumentTypeList = new ArrayList<Type>();
      for (TypeMirror typeArgument : typeArgumentList)
        genericArgumentTypeList.add(createType(typeArgument));

      return new DeclaredType(typeName, superType, implementedTypeList, genericArgumentTypeList);
    }
  }
  
  private TypeElement getClassElementRelationTag(TypeElement classElement, Class interfaceClass, int genericArgumentIndex) {
    for (TypeMirror interfaceType : classElement.getInterfaces()) {
      javax.lang.model.type.DeclaredType declaredInterfaceType = (javax.lang.model.type.DeclaredType) interfaceType;
      TypeElement typeElement = (TypeElement) declaredInterfaceType.asElement();
      if (typeElement.getQualifiedName().toString().equals(interfaceClass.getName())) {
        String typeElementName = declaredInterfaceType.getTypeArguments().get(genericArgumentIndex).toString();
        return config.getProcessingEnv().getElementUtils().getTypeElement(typeElementName);
      }
    }

    return null;
  }

  private static class ModelClassComparator implements Comparator<ModelClass> {
    @Override
    public int compare(ModelClass o1, ModelClass o2) {
      return o1.getQualifiedName().compareTo(o2.getQualifiedName());
    }
  }

}
