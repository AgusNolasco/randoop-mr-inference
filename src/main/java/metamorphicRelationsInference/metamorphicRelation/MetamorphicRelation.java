package metamorphicRelationsInference.metamorphicRelation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import metamorphicRelationsInference.util.Pair;
import randoop.operation.TypedOperation;
import randoop.sequence.Sequence;
import randoop.types.JavaTypes;

public class MetamorphicRelation {

  private final Constructor<?> leftConstructor;
  private final List<Method> leftMethods;
  private final Constructor<?> rightConstructor;
  private final List<Method> rightMethods;
  private final Set<String> statesWhereSurvives;

  public MetamorphicRelation(
      Constructor<?> leftConstructor,
      List<Method> leftMethods,
      Constructor<?> rightConstructor,
      List<Method> rightMethods,
      Set<String> statesWhereSurvives) {
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

  public Pair<Sequence, Sequence> createSequences(Sequence sequence, Integer varIndex) {
    Sequence leftSeq = sequence;
    Sequence rightSeq = sequence;

    Integer leftNewObjVarIndex = null;
    if (leftConstructor != null) {
      leftSeq = leftSeq.extend(TypedOperation.forConstructor(leftConstructor));
      leftNewObjVarIndex = leftSeq.getLastVariable().index;
      try {
        leftSeq =
            leftSeq.extend(
                TypedOperation.forMethod(Object.class.getMethod("getClass")),
                leftSeq.getVariable(leftNewObjVarIndex));
      } catch (NoSuchMethodException e) {
        throw new RuntimeException(e);
      }
    }

    for (Method m : leftMethods) {
      if (m.getParameterTypes().length > 0) {
        leftSeq =
            leftSeq.extend(
                TypedOperation.createPrimitiveInitialization(JavaTypes.STRING_TYPE, "hi!"));
        leftSeq =
            leftSeq.extend(
                TypedOperation.forMethod(m),
                leftNewObjVarIndex == null
                    ? leftSeq.getVariable(varIndex)
                    : leftSeq.getVariable(leftNewObjVarIndex),
                leftSeq.getLastVariable());
      } else {
        leftSeq =
            leftSeq.extend(
                TypedOperation.forMethod(m),
                leftNewObjVarIndex == null
                    ? leftSeq.getVariable(varIndex)
                    : leftSeq.getVariable(leftNewObjVarIndex));
      }
    }

    Integer rightNewObjVarIndex = null;
    if (rightConstructor != null) {
      rightSeq = rightSeq.extend(TypedOperation.forConstructor(rightConstructor));
      rightNewObjVarIndex = rightSeq.getLastVariable().index;
      try {
        rightSeq =
            rightSeq.extend(
                TypedOperation.forMethod(Object.class.getMethod("getClass")),
                rightSeq.getVariable(rightNewObjVarIndex));
      } catch (NoSuchMethodException e) {
        throw new RuntimeException(e);
      }
    }

    for (Method m : rightMethods) {
      if (m.getParameterTypes().length > 0) {
        rightSeq =
            rightSeq.extend(
                TypedOperation.createPrimitiveInitialization(JavaTypes.STRING_TYPE, "bye!"));
        rightSeq =
            rightSeq.extend(
                TypedOperation.forMethod(m),
                rightNewObjVarIndex == null
                    ? rightSeq.getVariable(varIndex)
                    : rightSeq.getVariable(rightNewObjVarIndex),
                rightSeq.getLastVariable());
      } else {
        rightSeq =
            rightSeq.extend(
                TypedOperation.forMethod(m),
                rightNewObjVarIndex == null
                    ? rightSeq.getVariable(varIndex)
                    : rightSeq.getVariable(rightNewObjVarIndex));
      }
    }

    return new Pair<>(leftSeq, rightSeq);
  }

  public Set<String> getStatesWhereSurvives() {
    return statesWhereSurvives;
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
