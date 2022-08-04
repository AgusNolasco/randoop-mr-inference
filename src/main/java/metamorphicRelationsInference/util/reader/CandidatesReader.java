package metamorphicRelationsInference.util.reader;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import metamorphicRelationsInference.metamorphicRelation.MetamorphicRelation;
import metamorphicRelationsInference.util.ReaderUtils;

public class CandidatesReader {

  private final Class<?> cut;

  public CandidatesReader(Class<?> cut) {
    this.cut = cut;
  }

  public Set<MetamorphicRelation> read(String pathToFile) {
    Set<MetamorphicRelation> metamorphicRelations = new HashSet<>();

    List<String> lines = ReaderUtils.getLines(pathToFile);
    for (String line : lines) {
      String[] components = line.trim().split(",");
      Constructor<?> leftConstructor = getConstructor(components[0]);
      List<Method> leftMethods = getMethods(components[1]);
      Constructor<?> rightConstructor = getConstructor(components[2]);
      List<Method> rightMethods = getMethods(components[3]);
      Set<String> statesWhereSurvives =
          Arrays.stream(components[4].split(";"))
              .filter(s -> !s.equals(""))
              .collect(Collectors.toSet());

      metamorphicRelations.add(
          new MetamorphicRelation(
              leftConstructor, leftMethods, rightConstructor, rightMethods, statesWhereSurvives));
    }

    return metamorphicRelations;
  }

  private Constructor<?> getConstructor(String str) {
    Constructor<?> constructor = null;
    if (!str.isEmpty()) {
      String classesStr = str.substring(1, str.length() - 1);
      List<String> classes =
          Arrays.stream(classesStr.split(";"))
              .filter(c -> !c.equals(""))
              .collect(Collectors.toList());
      Class<?>[] constructorParams =
          classes.stream().map(this::getClassForName).toArray(Class<?>[]::new);
      constructor = getConstructorForParams(constructorParams);
    }
    return constructor;
  }

  private Constructor<?> getConstructorForParams(Class<?>[] params) {
    try {
      return cut.getConstructor(params);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  private List<Method> getMethods(String str) {
    List<String> methodsStr =
        Arrays.stream(str.split(";")).filter(m -> !m.equals("")).collect(Collectors.toList());
    return methodsStr.stream().map(this::getMethod).collect(Collectors.toList());
  }

  private Method getMethod(String mStr) {
    String name = mStr.substring(0, mStr.indexOf("["));
    String paramStr = mStr.substring(mStr.indexOf("[") + 1, mStr.indexOf("]"));
    List<String> classes =
        Arrays.stream(paramStr.split(":")).filter(t -> !t.equals("")).collect(Collectors.toList());
    Class<?>[] params = classes.stream().map(this::getClassForName).toArray(Class<?>[]::new);
    try {
      return cut.getMethod(name, params);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("signature")
  private Class<?> getClassForName(String name) {
    try {
      return Class.forName(name);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }
}
