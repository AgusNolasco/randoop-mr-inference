package metamorphicRelationsInference.metamorphicRelation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import metamorphicRelationsInference.epa.EPAState;
import metamorphicRelationsInference.util.Pair;
import randoop.operation.TypedOperation;
import randoop.sequence.Sequence;
import randoop.types.JavaTypes;
import randoop.types.PrimitiveType;

public class MetamorphicRelation {

  private final Constructor<?> leftConstructor;
  private final List<Method> leftMethods;
  private final Constructor<?> rightConstructor;
  private final List<Method> rightMethods;
  private final Set<EPAState> statesWhereSurvives;

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

  public Pair<Sequence, Sequence> createSequences(Sequence sequence, Integer varIndex) {
    Sequence leftSeq = sequence;
    Sequence rightSeq = sequence;

    Pair<Sequence, Integer> pair1 = constructorSequence(leftConstructor, leftSeq);
    leftSeq = pair1.getFst();
    Integer leftNewObjVarIndex = pair1.getSnd();

    int leftVarIndex = leftNewObjVarIndex == null ? varIndex : leftNewObjVarIndex;
    leftSeq = methodsSequence(leftMethods, leftSeq, leftVarIndex);

    Pair<Sequence, Integer> pair2 = constructorSequence(rightConstructor, rightSeq);
    rightSeq = pair2.getFst();
    Integer rightNewObjVarIndex = pair2.getSnd();

    int rightVarIndex = rightNewObjVarIndex == null ? varIndex : rightNewObjVarIndex;
    rightSeq = methodsSequence(rightMethods, rightSeq, rightVarIndex);

    return new Pair<>(leftSeq, rightSeq);
  }

  private Pair<Sequence, Integer> constructorSequence(
      Constructor<?> constructor, Sequence sequence) {
    Integer newObjVarIndex = null;
    if (constructor != null) {
      sequence = sequence.extend(TypedOperation.forConstructor(constructor));
      newObjVarIndex = sequence.getLastVariable().index;
      TypedOperation op = TypedOperation.forMethod(getClassMethod());
      sequence = sequence.extend(op, sequence.getVariable(newObjVarIndex));
    }
    return new Pair<>(sequence, newObjVarIndex);
  }

  private Sequence methodsSequence(List<Method> methods, Sequence sequence, int varIndex) {
    for (Method m : methods) {
      if (m.getParameterTypes().length > 0) {
        // TODO: Replace this for a random-generated object
        TypedOperation op = generateRandomObjectConstructionOperation();
        sequence = sequence.extend(op);
        sequence =
            sequence.extend(
                TypedOperation.forMethod(m),
                sequence.getVariable(varIndex),
                sequence.getLastVariable());
      } else {
        sequence = sequence.extend(TypedOperation.forMethod(m), sequence.getVariable(varIndex));
      }
    }
    return sequence;
  }

  private TypedOperation generateRandomObjectConstructionOperation() {
    Random rand = new Random();
    PrimitiveType type =
        JavaTypes.getPrimitiveTypes().get(rand.nextInt(JavaTypes.getPrimitiveTypes().size()));
    return TypedOperation.createNullOrZeroInitializationForType(type);
  }

  /**
   * This method is used to return a method, in particular "getClass" to be used in the constructor
   * sequence generation to make at least one operation with the new object for generate the value
   * of it. Also is used to wrap the exception in case of no encounter the method.
   *
   * @return getClass method
   */
  private Method getClassMethod() {
    try {
      return Object.class.getMethod("getClass");
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  public Set<EPAState> getStatesWhereSurvives() {
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
