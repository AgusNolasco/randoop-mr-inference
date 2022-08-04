package metamorphicRelationsInference.util.reader;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import metamorphicRelationsInference.util.ReaderUtils;

public class EnabledMethodsReader {

  public static Map<String, Map<Method, Boolean>> readEnabledMethodsPerState(
      Class<?> cut, String pathToFile) {
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
    return enabledMethodsPerState;
  }
}
