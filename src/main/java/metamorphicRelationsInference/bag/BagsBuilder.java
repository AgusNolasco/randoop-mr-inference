package metamorphicRelationsInference.bag;

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
      // Constructors are applicable in any sequence
      bags.get(initialState).add(new Pair<>(s.sequence.getLastVariable(), null));
      int i = 0;
      for (ReferenceValue referenceValue : s.getAllValues()) {
        if (referenceValue.getType().getCanonicalName().equals(clazz.getName())) {
          Variable var = s.getVariable(referenceValue.getObjectValue());
          try {
            bags.get(computeState(referenceValue.getObjectValue())).add(new Pair<>(var, i));
          } catch (Exception e) {
            System.out.println("The state could not be computed for the next sequence: \n\n");
            System.out.println(s.sequence);
            System.out.println("Caused by: " + e + "\n");
          }
        }
        i++;
      }
    }
    return bags;
  }

  private EPAState computeState(Object obj) throws Exception {
    for (EPAState state : states) {
      boolean allEqual = true;

      Map<Method, Boolean> methodsAndResults = state.getEnabledMethods();
      for (Method m : methodsAndResults.keySet()) {
        m.setAccessible(true);
        boolean result;
        result = (Boolean) m.invoke(obj);
        allEqual &= methodsAndResults.get(m) == result;
      }

      if (allEqual) {
        return state;
      }
    }

    throw new IllegalStateException("Can not exist a object that is not in any state");
  }
}
