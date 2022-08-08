package metamorphicRelationsInference.util.reader;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import metamorphicRelationsInference.epa.EPAState;
import metamorphicRelationsInference.metamorphicRelation.MetamorphicRelation;
import metamorphicRelationsInference.util.ReaderUtils;
import org.apache.commons.lang3.ClassUtils;

public class CandidatesReader {

  private final Class<?> cut;
  private final String COMPONENTS_DELIMITER = ",";
  private final String ACTIONS_DELIMITER = ";";
  private final String PARAMS_DELIMITER = ":";

  public CandidatesReader(Class<?> cut) {
    this.cut = cut;
  }

  public List<MetamorphicRelation> read(String pathToFile) {
    List<MetamorphicRelation> metamorphicRelations = new ArrayList<>();

    List<String> lines = ReaderUtils.getLines(pathToFile);
    for (String line : lines) {
      String[] components = line.trim().split(COMPONENTS_DELIMITER);
      Constructor<?> leftConstructor = getConstructor(components[0]);
      List<Method> leftMethods = getMethods(components[1]);
      Constructor<?> rightConstructor = getConstructor(components[2]);
      List<Method> rightMethods = getMethods(components[3]);
      Set<EPAState> statesWhereSurvives = getStates(components[4]);

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
          Arrays.stream(classesStr.split(ACTIONS_DELIMITER))
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
        Arrays.stream(str.split(ACTIONS_DELIMITER))
            .filter(m -> !m.equals(""))
            .collect(Collectors.toList());
    return methodsStr.stream().map(this::getMethod).collect(Collectors.toList());
  }

  private Method getMethod(String mStr) {
    String name = mStr.substring(0, mStr.indexOf("["));
    String paramStr = mStr.substring(mStr.indexOf("[") + 1, mStr.indexOf("]"));
    List<String> classes =
        Arrays.stream(paramStr.split(PARAMS_DELIMITER))
            .filter(t -> !t.equals(""))
            .collect(Collectors.toList());
    Class<?>[] params = classes.stream().map(this::getClassForName).toArray(Class<?>[]::new);
    try {
      return cut.getMethod(name, params);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  private Set<EPAState> getStates(String statesStr) {
    return Arrays.stream(statesStr.split(";"))
        .filter(s -> !s.equals(""))
        .map(EPAState::new)
        .collect(Collectors.toSet());
  }

  private Class<?> getClassForName(String name) {
    try {
      return ClassUtils.getClass(name);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }
}
