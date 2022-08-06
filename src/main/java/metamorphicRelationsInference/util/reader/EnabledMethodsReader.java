package metamorphicRelationsInference.util.reader;

import java.lang.reflect.Method;
import java.util.*;
import metamorphicRelationsInference.epa.EPAState;
import metamorphicRelationsInference.util.ReaderUtils;

public class EnabledMethodsReader {

  public static Set<EPAState> readEnabledMethodsPerState(Class<?> cut, String pathToFile) {
    Set<EPAState> states = new HashSet<>();
    List<String> lines = ReaderUtils.getLines(pathToFile);
    Map<String, Map<Method, Boolean>> enabledMethodsPerState = new HashMap<>();
    for (String line : lines) {
      String[] components = line.trim().split(" : ");
      String state = components[0];
      Method method;
      try {
        method = cut.getDeclaredMethod(components[1]);
      } catch (NoSuchMethodException e) {
        throw new RuntimeException(e);
      }
      boolean result = Boolean.parseBoolean(components[2]);

      Map<Method, Boolean> methodsAndResults = enabledMethodsPerState.get(state);
      if (methodsAndResults == null) {
        methodsAndResults = new HashMap<>();
      }
      methodsAndResults.put(method, result);
      enabledMethodsPerState.put(state, methodsAndResults);
    }
    for (String stateName : enabledMethodsPerState.keySet()) {
      states.add(new EPAState(stateName, enabledMethodsPerState.get(stateName)));
    }
    if (states.stream().filter(EPAState::isInitial).count() > 1) {
      throw new IllegalArgumentException("Only can exist one initial state");
    }
    return states;
  }
}
