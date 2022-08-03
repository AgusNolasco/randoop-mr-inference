package metamorphicRelationInference.bag;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import metamorphicRelationInference.util.Pair;
import randoop.DummyVisitor;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.ReferenceValue;
import randoop.test.DummyCheckGenerator;

public class BagsBuilder {

  private Class<?> clazz;
  private Map<String, Map<Method, Boolean>> enabledMethodsPerState;

  public BagsBuilder(Class<?> clazz, Map<String, Map<Method, Boolean>> enabledMethodsPerState) {
    this.clazz = clazz;
    this.enabledMethodsPerState = enabledMethodsPerState;
  }

  public Map<String, Bag> createBags(List<ExecutableSequence> sequences) {
    Map<String, Bag> bags = new HashMap<>();
    for (String state : enabledMethodsPerState.keySet()) {
      bags.put(state, new Bag(state));
    }
    for (ExecutableSequence s : sequences) {
      s.execute(new DummyVisitor(), new DummyCheckGenerator());
      int i = 0;
      for (ReferenceValue referenceValue : s.getAllValues()) {
        if (clazz.getName().equals(referenceValue.getObjectValue().getClass().getName())) {
          bags.get(computeState(referenceValue.getObjectValue())).add(new Pair<>(s, i));
        }
        i++;
      }
    }
    return bags;
  }

  private String computeState(Object obj) {
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
