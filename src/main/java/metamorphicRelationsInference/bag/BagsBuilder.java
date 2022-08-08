package metamorphicRelationsInference.bag;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import metamorphicRelationsInference.epa.EPAState;
import metamorphicRelationsInference.util.Pair;
import randoop.DummyVisitor;
import randoop.sequence.*;
import randoop.test.DummyCheckGenerator;

public class BagsBuilder {

  private final Class<?> clazz;
  private final Set<EPAState> states;
  private final EPAState initialState;

  public BagsBuilder(Class<?> clazz, Set<EPAState> states) {
    this.clazz = clazz;
    this.states = states;
    Optional<EPAState> optionalInitialState =
        states.stream().filter(EPAState::isInitial).findFirst();
    if (!optionalInitialState.isPresent()) {
      throw new IllegalArgumentException("An initial state is required");
    }
    this.initialState = optionalInitialState.get();
  }

  public Map<EPAState, Bag> createBags(List<ExecutableSequence> sequences) {
    Map<EPAState, Bag> bags = new HashMap<>();
    for (EPAState state : states) {
      bags.put(state, new Bag(state));
    }
    for (ExecutableSequence s : sequences) {
      s.execute(new DummyVisitor(), new DummyCheckGenerator());
      if (s.getAllValues().stream()
          .noneMatch(rv -> rv.getType().getCanonicalName().equals(clazz.getName()))) {
        bags.get(initialState).add(new Pair<>(s.sequence.getLastVariable(), null));
        continue;
      }
      int i = 0;
      for (ReferenceValue referenceValue : s.getAllValues()) {
        if (referenceValue.getType().getCanonicalName().equals(clazz.getName())) {
          Variable var = s.getVariable(referenceValue.getObjectValue());
          bags.get(computeState(referenceValue.getObjectValue())).add(new Pair<>(var, i));
        }
        i++;
      }
    }
    if (bags.get(initialState).getVariablesAndIndexes().isEmpty()) {
      Optional<ExecutableSequence> optionalSeq = sequences.stream().findAny();
      if (optionalSeq.isPresent()) {
        bags.get(initialState).add(new Pair<>(optionalSeq.get().sequence.getLastVariable(), null));
      } else {
        throw new IllegalArgumentException("There's no sequences!");
      }
    }
    return bags;
  }

  private EPAState computeState(Object obj) {
    for (EPAState state : states) {
      boolean allEqual = true;

      Map<Method, Boolean> methodsAndResults = state.getEnabledMethods();
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
