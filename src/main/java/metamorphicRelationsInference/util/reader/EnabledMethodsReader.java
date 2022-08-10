package metamorphicRelationsInference.util.reader;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import metamorphicRelationsInference.epa.EPAState;
import metamorphicRelationsInference.util.ReaderUtils;

public class EnabledMethodsReader {

  public static Set<EPAState> readEnabledMethodsPerState(Class<?> cut, String pathToFile) {
    Set<EPAState> states = new HashSet<>();
    List<String> lines = ReaderUtils.getLines(pathToFile);
    Map<String, Map<Method, Boolean>> enabledMethodsPerState = new HashMap<>();
    Map<String, Boolean> isInitial = new HashMap<>();
    for (String line : lines) {
      String[] components = line.trim().split(" : ");
      String state = components[0];
      if (components.length == 2) {
        isInitial.put(state, Boolean.parseBoolean(components[1]));
      } else {
        Method method = getMethod(cut, components[1]);
        boolean result = Boolean.parseBoolean(components[2]);

        Map<Method, Boolean> methodsAndResults = enabledMethodsPerState.get(state);
        if (methodsAndResults == null) {
          methodsAndResults = new HashMap<>();
        }
        methodsAndResults.put(method, result);
        enabledMethodsPerState.put(state, methodsAndResults);
      }
    }

    for (String stateName : enabledMethodsPerState.keySet()) {
      states.add(
          new EPAState(stateName, isInitial.get(stateName), enabledMethodsPerState.get(stateName)));
    }

    if (states.stream().filter(EPAState::isInitial).count() > 1) {
      System.out.println(states.stream().filter(EPAState::isInitial).collect(Collectors.toList()));
      throw new IllegalArgumentException("Only can exist one initial state");
    }

    return states;
  }

  private static Method getMethod(Class<?> cut, String name) {
    try {
      return cut.getDeclaredMethod(name);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }
}
