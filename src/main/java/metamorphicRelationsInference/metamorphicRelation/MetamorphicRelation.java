package metamorphicRelationsInference.metamorphicRelation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import metamorphicRelationsInference.epa.EPAState;
import metamorphicRelationsInference.util.Pair;
import randoop.sequence.Sequence;

public class MetamorphicRelation {

  private final Constructor<?> leftConstructor;
  private final List<Method> leftMethods;
  private final Constructor<?> rightConstructor;
  private final List<Method> rightMethods;
  private final Set<EPAState> statesWhereSurvives;
  private Pair<Sequence, Sequence> counterExample;

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
    return statesWhereSurvives;
  }

  public void setCounterExample(Pair<Sequence, Sequence> counterExamples) {
    this.counterExample = counterExamples;
  }

  public Pair<Sequence, Sequence> getCounterExample() {
    return counterExample;
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
    StringBuilder str = new StringBuilder();
    str.append(statesWhereSurvives).append(" -> ");
    if (leftConstructor != null) {
      String[] splitName = leftConstructor.getName().split("\\.");
      str.append(splitName[splitName.length - 1]).append(" ");
    }
    for (Method m : leftMethods) {
      str.append(m.getName()).append(" ");
    }
    if (leftConstructor == null && leftMethods.isEmpty()) {
      str.append("λ ");
    }
    str.append("= ");
    if (rightConstructor != null) {
      String[] splitName = rightConstructor.getName().split("\\.");
      str.append(splitName[splitName.length - 1]).append(" ");
    }
    for (Method m : rightMethods) {
      str.append(m.getName()).append(" ");
    }
    return str.toString();
  }
}
