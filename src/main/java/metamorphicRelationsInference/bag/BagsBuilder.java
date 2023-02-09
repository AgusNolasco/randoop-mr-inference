package metamorphicRelationsInference.bag;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
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

  public Map<EPAState, Bag> createBags(List<ExecutableSequence> sequences) throws Exception {
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
            throw e;
          }
        }
        i++;
      }
    }
    return bags;
  }

  private EPAState computeState(Object object) throws Exception {
    Map<Method, Boolean> methodsAndActualResults = new HashMap<>();
    List<Set<Method>> listOfSetOfMethods =
        states.stream()
            .map(EPAState::getEnabledMethods)
            .map(Map::keySet)
            .collect(Collectors.toList());
    Predicate<Set<Method>> predicate = set -> Objects.equals(listOfSetOfMethods.get(0), set);
    assert listOfSetOfMethods.stream().allMatch(predicate);
    Set<Method> methods = listOfSetOfMethods.get(0);
    for (Method m : methods) {
      m.setAccessible(true);
      methodsAndActualResults.put(m, (Boolean) m.invoke(object));
    }

    for (EPAState state : states) {
      boolean allEqual = true;
      Map<Method, Boolean> methodsAndExpectedResults = state.getEnabledMethods();
      System.out.println("---------------------");
      System.out.println(state);
      for (Method m : methods) {
        System.out.println(
            m.getName()
                + " expected: "
                + methodsAndExpectedResults.get(m)
                + " - actual: "
                + methodsAndActualResults.get(m));
        if (!methodsAndExpectedResults.get(m).equals(methodsAndActualResults.get(m))) {
          allEqual = false;
          break;
        }
      }

      if (allEqual) {
        return state;
      }
    }

    throw new IllegalStateException("Can not exist a object that is not in any state");
  }
}
