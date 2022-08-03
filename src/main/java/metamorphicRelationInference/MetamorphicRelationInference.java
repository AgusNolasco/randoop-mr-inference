package metamorphicRelationInference;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import metamorphicRelationInference.utils.Pair;
import metamorphicRelationInference.utils.ReaderUtils;
import randoop.DummyVisitor;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.ReferenceValue;
import randoop.test.DummyCheckGenerator;

public class MetamorphicRelationInference {

  private static Class<?> cut;
  private static Map<String, Map<Method, Boolean>> enabledMethodsPerState;
  private static Map<String, List<Pair<ExecutableSequence, Integer>>> bags;

  public static void setCut(Class<?> clazz) {
    cut = clazz;
  }

  public static void loadEnabledMethodsPerState(String pathToFile) {
    List<String> lines = ReaderUtils.getLines(pathToFile);
    enabledMethodsPerState = new HashMap<>();
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
  }

  public static void createBagsPerState(
      List<ExecutableSequence> regressionSequences, Set<String> classnames) {
    List<ExecutableSequence> normalExecutionSeq =
        regressionSequences.stream()
            .filter(ExecutableSequence::isNormalExecution)
            .collect(Collectors.toList());

    bags = new HashMap<>();
    for (String state : enabledMethodsPerState.keySet()) {
      bags.put(state, new ArrayList<>());
    }
    for (ExecutableSequence s : normalExecutionSeq) {
      s.execute(new DummyVisitor(), new DummyCheckGenerator());
      int i = 0;
      for (ReferenceValue referenceValue : s.getAllValues()) {
        if (classnames.contains(referenceValue.getObjectValue().getClass().getName())) {
          bags.get(computeState(referenceValue.getObjectValue())).add(new Pair<>(s, i));
        }
        i++;
      }
    }
  }

  public static Map<String, List<Pair<ExecutableSequence, Integer>>> getBags() {
    return bags;
  }

  public static Object getObject(ExecutableSequence sequence, Integer index) {
    sequence.execute(new DummyVisitor(), new DummyCheckGenerator());
    return sequence.getAllValues().get(index).getObjectValue();
  }

  private static String computeState(Object obj) {
    for (String state : enabledMethodsPerState.keySet()) {
      boolean allEqual = true;

      Map<Method, Boolean> methodsAndResults = enabledMethodsPerState.get(state);
      for (Method m : methodsAndResults.keySet()) {
        m.setAccessible(true);
        boolean result;
        try {
          result = (Boolean) m.invoke(obj);
        } catch (IllegalAccessException | InvocationTargetException e) {
          throw new RuntimeException(e);
        }
        allEqual &= methodsAndResults.get(m) == result;
      }

      if (allEqual) {
        return state;
      }
    }

    throw new IllegalStateException("Can not exist a object that is not in any state");
  }
}
