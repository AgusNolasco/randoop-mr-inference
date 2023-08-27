package metamorphicRelationsInference.validator;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import metamorphicRelationsInference.distance.Distance;
import metamorphicRelationsInference.metamorphicRelation.MetamorphicRelation;
import metamorphicRelationsInference.util.OperationInputs;
import metamorphicRelationsInference.util.Pair;
import org.plumelib.util.CollectionsPlume;
import randoop.DummyVisitor;
import randoop.generation.AbstractGenerator;
import randoop.generation.InputsAndSuccessFlag;
import randoop.operation.TypedOperation;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Sequence;
import randoop.sequence.Variable;
import randoop.test.DummyCheckGenerator;
import randoop.types.*;

public class Executor {

  private final AbstractGenerator explorer;
  private Variable origVar;
  private List<OperationInputs> leftValues, rightValues;
  private Constructor<?> leftConstr, rightConstr;
  private List<Method> leftMethods, rightMethods;
  private Sequence leftSeq, rightSeq;
  private Pair<Object, Object> counterExample;
  private boolean allFail;

  public Executor(AbstractGenerator explorer) {
    this.explorer = explorer;
  }

  public void setup(MetamorphicRelation mr, Variable var) {
    if (!getObjectFromVar(var).equals(getObjectFromVar(var))) {
      throw new RuntimeException("The object to be compared must be the same");
    }

    int sampleSize = 5;
    origVar = var;
    leftConstr = mr.getLeftConstructor();
    rightConstr = mr.getRightConstructor();
    leftMethods = mr.getLeftMethods();
    rightMethods = mr.getRightMethods();
    leftValues = getListOfArgs(leftConstr, leftMethods, sampleSize);
    rightValues = getListOfArgs(rightConstr, rightMethods, sampleSize);
  }

  private List<OperationInputs> getListOfArgs(
      Constructor<?> constr, List<Method> methods, int count) {
    List<OperationInputs> listOfArgs = new ArrayList<>();
    if (constr != null) {
      listOfArgs.add(explorer.getInputsFor(TypedOperation.forConstructor(constr), count));
    }
    for (Method m : methods) {
      listOfArgs.add(explorer.getInputsFor(TypedOperation.forMethod(m), count));
    }
    return listOfArgs;
  }

  private Pair<Sequence, Variable> extendSequence(
      Variable var,
      Constructor<?> constructor,
      List<Method> methods,
      List<InputsAndSuccessFlag> inputs) {
    Sequence sequence = var.sequence;
    Substitution s = null;
    if (constructor == null && var.getType().isParameterized() && !var.getType().isGeneric()) {
      ParameterizedType t = (InstantiatedType) var.getType();
      List<ReferenceType> raList =
          t.getTypeArguments().stream()
              .map(type -> ((ReferenceArgument) type).getReferenceType())
              .collect(Collectors.toList());
      s = new Substitution(t.getGenericClassType().getTypeParameters(), raList);
    }

    int inputsIndex = 0;
    int varIndex = var.index;
    if (constructor != null) {
      Pair<Sequence, Integer> pair1 =
          appendConstructor(constructor, sequence, inputs.get(inputsIndex++));
      sequence = pair1.getFst();
      varIndex = pair1.getSnd();
    }

    for (Method m : methods) {
      sequence = appendMethod(m, sequence, varIndex, s, inputs.get(inputsIndex++));
    }

    assert sequence != null;
    return new Pair<>(sequence, sequence.getVariable(varIndex));
  }

  public Pair<Sequence, Sequence> getSequences() {
    return new Pair<>(leftSeq, rightSeq);
  }

  private Object computeResult(Sequence sequence, Variable var) throws NonNormalExecutionException {
    ExecutableSequence executableSequence = new ExecutableSequence(sequence);
    executableSequence.execute(new DummyVisitor(), new DummyCheckGenerator());
    if (!executableSequence.isNormalExecution()) {
      throw new NonNormalExecutionException(
          "Unable to execute this sequence because throws exceptions");
    }
    Object[] values =
        ExecutableSequence.getRuntimeValuesForVars(
            Collections.singletonList(var), executableSequence.executionResults);
    return values[0];
  }

  private Pair<Sequence, Integer> appendConstructor(
      Constructor<?> constructor, Sequence sequence, InputsAndSuccessFlag input) {
    TypedOperation operation = TypedOperation.forConstructor(constructor);
    Sequence auxSeq = null;
    boolean isNormalExec = false;
    for (int i = 0; i < 1000 && !isNormalExec; i++) {
      auxSeq = Sequence.concatenate(Collections.singletonList(sequence));
      Sequence concatSeq = Sequence.concatenate(input.sequences);
      List<Integer> indices = adjustIndices(input.indices, auxSeq.getLastVariable().index + 1);
      auxSeq = Sequence.concatenate(Arrays.asList(auxSeq, concatSeq));
      List<Variable> vars = CollectionsPlume.mapList(auxSeq::getVariable, indices);
      auxSeq = auxSeq.extend(operation, vars);
      ExecutableSequence executableSequence = new ExecutableSequence(auxSeq);
      executableSequence.execute(new DummyVisitor(), new DummyCheckGenerator());
      isNormalExec = executableSequence.isNormalExecution();
    }

    sequence = auxSeq;
    // This section adds a call of .getClass over the new object to use it
    int newObjVarIndex = sequence.getLastVariable().index;
    TypedOperation op = TypedOperation.forMethod(getClassMethod());
    sequence = sequence.extend(op, sequence.getVariable(newObjVarIndex));
    return new Pair<>(sequence, newObjVarIndex);
  }

  private Sequence appendMethod(
      Method m, Sequence sequence, int varIndex, Substitution s, InputsAndSuccessFlag input) {
    TypedOperation operation = TypedOperation.forMethod(m);
    if (s != null) {
      operation = operation.substitute(s);
    }
    Sequence concatSeq = Sequence.concatenate(input.sequences);
    List<Integer> indices = adjustIndices(input.indices, sequence.getLastVariable().index + 1);
    sequence = Sequence.concatenate(Arrays.asList(sequence, concatSeq));
    List<Variable> vars = CollectionsPlume.mapList(sequence::getVariable, indices);
    vars.add(0, sequence.getVariable(varIndex));
    return sequence.extend(operation, vars);
  }

  private List<Integer> adjustIndices(List<Integer> indices, Integer adjustValue) {
    return indices.stream().map(i -> i + adjustValue).collect(Collectors.toList());
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

  private static Object getObjectFromVar(Variable var) {
    ExecutableSequence excSeq = new ExecutableSequence(var.sequence);
    excSeq.execute(new DummyVisitor(), new DummyCheckGenerator());
    if (!excSeq.isNormalExecution()) {
      throw new IllegalStateException("Unable to execute this sequence because throws exceptions");
    }
    Object[] values =
        ExecutableSequence.getRuntimeValuesForVars(
            Collections.singletonList(var), excSeq.executionResults);
    return values[0];
  }

  public boolean checkProperty(int times) {
    counterExample = null;
    int[] leftIndexes = new int[leftValues.size()];
    int[] rightIndexes = new int[rightValues.size()];
    int maxCombinations = maxCombinationsOf(leftValues, rightValues);
    allFail = true;
    for (int i = 0; i < times && i < maxCombinations; i++) {
      List<InputsAndSuccessFlag> leftInputs = selectInputs(leftValues, leftIndexes);
      List<InputsAndSuccessFlag> rightInputs = selectInputs(rightValues, rightIndexes);
      Pair<Sequence, Variable> leftSeqAndVar =
          extendSequence(origVar, leftConstr, leftMethods, leftInputs);
      Pair<Sequence, Variable> rightSeqAndVar =
          extendSequence(origVar, rightConstr, rightMethods, rightInputs);
      leftSeq = leftSeqAndVar.getFst();
      rightSeq = rightSeqAndVar.getFst();
      Object leftResult;
      Object rightResult;
      try {
        leftResult = computeResult(leftSeq, leftSeqAndVar.getSnd());
        rightResult = computeResult(rightSeq, rightSeqAndVar.getSnd());
      } catch (NonNormalExecutionException e) {
        System.out.println(e.getMessage());
        continue;
      }
      allFail = false;
      if (!Distance.strongEquals(leftResult, rightResult)) {
        counterExample = new Pair<>(leftResult, rightResult);
        return false;
      }
    }
    return true;
  }

  private int maxCombinationsOf(
      List<OperationInputs> leftValues, List<OperationInputs> rightValues) {
    int comb1 = 1, comb2 = 1;
    for (OperationInputs inputs : leftValues) {
      comb1 *= inputs.size();
    }
    for (OperationInputs inputs : rightValues) {
      comb2 *= inputs.size();
    }
    return Math.max(comb1, comb2);
  }

  private List<InputsAndSuccessFlag> selectInputs(List<OperationInputs> values, int[] indexes) {
    List<InputsAndSuccessFlag> inputs = new ArrayList<>();
    for (int i = 0; i < values.size(); i++) {
      OperationInputs operationInput = values.get(i);
      int j = indexes[i];
      inputs.add(operationInput.get(j));
    }
    increaseIndexes(indexes, values);
    return inputs;
  }

  private void increaseIndexes(int[] indexes, List<OperationInputs> inputs) {
    for (int i = 0; i < indexes.length; i++) {
      if (indexes[i] < inputs.get(i).size() - 1) {
        indexes[i]++;
        return;
      }
      indexes[i] = 0;
    }
  }

  public Pair<Object, Object> getCounterExample() {
    return counterExample;
  }

  public boolean allFail() {
    return allFail;
  }
}
