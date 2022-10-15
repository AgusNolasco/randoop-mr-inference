package metamorphicRelationsInference.metamorphicRelation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import metamorphicRelationsInference.epa.EPAState;
import metamorphicRelationsInference.util.Pair;
import randoop.sequence.Sequence;

public class MetamorphicRelation {

  private final Constructor<?> leftConstructor;
  private final List<Method> leftMethods;
  private final Constructor<?> rightConstructor;
  private final List<Method> rightMethods;
  private final Set<EPAState> statesWhereSurvives;
  private final Map<EPAState, Pair<Sequence, Sequence>> counterExampleSequencesPerState;
  private final Map<EPAState, Pair<Object, Object>> counterExampleObjectsPerState;

  public MetamorphicRelation(
      Constructor<?> leftConstructor,
      List<Method> leftMethods,
      Constructor<?> rightConstructor,
      List<Method> rightMethods,
      Set<EPAState> statesWhereSurvives) {
    Objects.requireNonNull(leftMethods);
    Objects.requireNonNull(rightMethods);
    Objects.requireNonNull(statesWhereSurvives);
    if (statesWhereSurvives.isEmpty()) {
      throw new IllegalArgumentException("The states where the mr survives must be not empty");
    }

    this.leftConstructor = leftConstructor;
    this.leftMethods = leftMethods;
    this.rightConstructor = rightConstructor;
    this.rightMethods = rightMethods;
    this.statesWhereSurvives = statesWhereSurvives;
    counterExampleSequencesPerState = new HashMap<>();
    counterExampleObjectsPerState = new HashMap<>();
  }

  public Constructor<?> getLeftConstructor() {
    return leftConstructor;
  }

  public Constructor<?> getRightConstructor() {
    return rightConstructor;
  }

  public List<Method> getLeftMethods() {
    return leftMethods;
  }

  public List<Method> getRightMethods() {
    return rightMethods;
  }

  public Set<EPAState> getStatesWhereSurvives() {
    return new HashSet<>(statesWhereSurvives);
  }

  public void removeFromStatesWhereSurvives(EPAState state) {
    statesWhereSurvives.remove(state);
  }

  public Set<EPAState> getCounterExampledStates() {
    return counterExampleObjectsPerState.keySet();
  }

  public void addCounterExample(
      EPAState state,
      Pair<Sequence, Sequence> counterExampleSequences,
      Pair<Object, Object> counterExampleObjects) {

    counterExampleSequencesPerState.put(state, counterExampleSequences);
    counterExampleObjectsPerState.put(state, counterExampleObjects);
  }

  public boolean hasBothConstructors() {
    return getLeftConstructor() != null && getRightConstructor() != null;
  }

  public Pair<Sequence, Sequence> getCounterExampleSequences(EPAState state) {
    return counterExampleSequencesPerState.get(state);
  }

  public Pair<Object, Object> getCounterExampleObjects(EPAState state) {
    return counterExampleObjectsPerState.get(state);
  }

  public boolean hasCounterExample() {
    return counterExampleObjectsPerState != null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof MetamorphicRelation)) return false;

    MetamorphicRelation that = (MetamorphicRelation) o;

    if (!Objects.equals(leftConstructor, that.leftConstructor)) return false;
    if (!leftMethods.equals(that.leftMethods)) return false;
    if (!Objects.equals(rightConstructor, that.rightConstructor)) return false;
    if (!rightMethods.equals(that.rightMethods)) return false;
    return statesWhereSurvives.equals(that.statesWhereSurvives);
  }

  @Override
  public int hashCode() {
    int result = leftConstructor != null ? leftConstructor.hashCode() : 0;
    result = 31 * result + leftMethods.hashCode();
    result = 31 * result + (rightConstructor != null ? rightConstructor.hashCode() : 0);
    result = 31 * result + rightMethods.hashCode();
    result = 31 * result + statesWhereSurvives.hashCode();
    return result;
  }

  @Override
  public String toString() {
    String left = leftMethods.stream().map(Method::getName).collect(Collectors.joining(" "));
    String right = rightMethods.stream().map(Method::getName).collect(Collectors.joining(" "));
    if (leftConstructor != null) {
      if (!left.isEmpty()) {
        left = leftConstructor.getDeclaringClass().getSimpleName() + " " + left;
      } else {
        left = leftConstructor.getDeclaringClass().getSimpleName();
      }
    }
    if (rightConstructor != null) {
      if (!right.isEmpty()) {
        right = rightConstructor.getDeclaringClass().getSimpleName() + " " + right;
      } else {
        right = rightConstructor.getDeclaringClass().getSimpleName();
      }
    }
    if (left.isEmpty()) {
      left = "λ";
    }
    if (right.isEmpty()) {
      right = "λ";
    }
    return statesWhereSurvives + " -> " + left + " = " + right;
  }

  public String toFullString() {
    String left = leftMethods.stream().map(this::fullMethodName).collect(Collectors.joining(";"));
    String right = rightMethods.stream().map(this::fullMethodName).collect(Collectors.joining(";"));
    if (leftConstructor != null) {
      if (!left.isEmpty()) {
        left = fullConstructorName(leftConstructor) + ";" + left;
      } else {
        left = fullConstructorName(leftConstructor);
      }
    }
    if (rightConstructor != null) {
      if (!right.isEmpty()) {
        right = fullConstructorName(rightConstructor) + ";" + right;
      } else {
        right = fullConstructorName(rightConstructor);
      }
    }
    if (left.isEmpty()) {
      left = "null";
    }
    if (right.isEmpty()) {
      right = "null";
    }
    return left + " = " + right;
  }

  private String fullMethodName(Method m) {
    return m.getName() + getPrettyParams(m.getParameterTypes());
  }

  private String fullConstructorName(Constructor<?> c) {
    return c.getDeclaringClass().getSimpleName() + getPrettyParams(c.getParameterTypes());
  }

  private String getPrettyParams(Class<?>[] params) {
    String paramStr = "(";
    paramStr += Arrays.stream(params).map(Class::getSimpleName).collect(Collectors.joining(","));
    return paramStr + ")";
  }

  public String toAlloyPred(Class<?> clazz) {
    String left = leftMethods.stream().map(Method::getName).collect(Collectors.joining("."));
    String right = rightMethods.stream().map(Method::getName).collect(Collectors.joining("."));
    if (leftConstructor != null) {
      if (!left.isEmpty()) {
        left = leftConstructor.getDeclaringClass().getSimpleName() + "." + left;
      } else {
        left = leftConstructor.getDeclaringClass().getSimpleName();
      }
    }
    if (rightConstructor != null) {
      if (!right.isEmpty()) {
        right = rightConstructor.getDeclaringClass().getSimpleName() + "." + right;
      } else {
        right = rightConstructor.getDeclaringClass().getSimpleName();
      }
    }
    String mrPred = "";
    assert !right.isEmpty();
    if (left.isEmpty()) {
      mrPred += right + " iff e1 = e2";
    } else {
      mrPred += left + " iff (e1 -> e2) in " + right;
    }
    String statesWhereSurvivesStr =
        statesWhereSurvives.stream().map(EPAState::getName).collect(Collectors.joining("+"));
    return "all e1, e2: "
        + clazz.getSimpleName()
        + "Class | "
        + "e1 in ("
        + statesWhereSurvivesStr
        + ") implies ("
        + "(e1 -> e2) in "
        + mrPred
        + ")";
  }
}
