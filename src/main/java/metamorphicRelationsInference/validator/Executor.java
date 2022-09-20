package metamorphicRelationsInference.validator;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import metamorphicRelationsInference.metamorphicRelation.MetamorphicRelation;
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

public class Executor {

  private final AbstractGenerator explorer;
  private Sequence leftSeq, rightSeq;
  private Variable leftVar, rightVar;

  public Executor(AbstractGenerator explorer) {
    this.explorer = explorer;
  }

  public void setup(MetamorphicRelation mr, Variable var) throws Exception {
    initAttr();
    Pair<Sequence, Variable> left =
        extendSequence(var, mr.getLeftConstructor(), mr.getLeftMethods());
    Pair<Sequence, Variable> right =
        extendSequence(var, mr.getRightConstructor(), mr.getRightMethods());
    leftSeq = left.getFst();
    leftVar = left.getSnd();
    rightSeq = right.getFst();
    rightVar = right.getSnd();
  }

  private void initAttr() {
    leftSeq = null;
    rightSeq = null;
    leftVar = null;
    rightVar = null;
  }

  private Pair<Sequence, Variable> extendSequence(
      Variable var, Constructor<?> constructor, List<Method> methods) {
    Sequence sequence = var.sequence;
    Pair<Sequence, Integer> pair1 = constructorSequence(constructor, sequence);
    sequence = pair1.getFst();
    Integer newObjVarIndex = pair1.getSnd();

    int varIndex = newObjVarIndex == null ? var.index : newObjVarIndex;
    sequence = methodsSequence(methods, sequence, varIndex);

    return new Pair<>(sequence, sequence.getVariable(varIndex));
  }

  public Pair<Sequence, Sequence> getSequences() {
    return new Pair<>(leftSeq, rightSeq);
  }

  public Object getLeftResult() {
    return computeResult(leftSeq, leftVar);
  }

  public Object getRightResult() {
    return computeResult(rightSeq, rightVar);
  }

  private Object computeResult(Sequence sequence, Variable var) {
    ExecutableSequence executableSequence = new ExecutableSequence(sequence);
    executableSequence.execute(new DummyVisitor(), new DummyCheckGenerator());
    if (!executableSequence.isNormalExecution()) {
      throw new IllegalStateException("Unable to execute this sequence because throws exceptions");
    }
    Object[] values =
        ExecutableSequence.getRuntimeValuesForVars(
            Collections.singletonList(var), executableSequence.executionResults);
    return values[0];
  }

  private Pair<Sequence, Integer> constructorSequence(
      Constructor<?> constructor, Sequence sequence) {
    Integer newObjVarIndex = null;
    if (constructor != null) {
      TypedOperation operation = TypedOperation.forConstructor(constructor);

      Sequence auxSeq = null;
      boolean isNormalExec = false;
      for (int i = 0; i < 1000 && !isNormalExec; i++) {
        auxSeq = Sequence.concatenate(Collections.singletonList(sequence));
        InputsAndSuccessFlag inputs = explorer.selectInputs(operation, false);
        Sequence concatSeq = Sequence.concatenate(inputs.sequences);
        List<Integer> indices = adjustIndices(inputs.indices, auxSeq.getLastVariable().index + 1);
        auxSeq = Sequence.concatenate(Arrays.asList(auxSeq, concatSeq));
        List<Variable> vars = CollectionsPlume.mapList(auxSeq::getVariable, indices);
        auxSeq = auxSeq.extend(operation, vars);
        ExecutableSequence executableSequence = new ExecutableSequence(auxSeq);
        executableSequence.execute(new DummyVisitor(), new DummyCheckGenerator());
        isNormalExec = executableSequence.isNormalExecution();
      }

      sequence = auxSeq;
      // This section adds a call of .getClass over the new object to use it
      newObjVarIndex = sequence.getLastVariable().index;
      TypedOperation op = TypedOperation.forMethod(getClassMethod());
      sequence = sequence.extend(op, sequence.getVariable(newObjVarIndex));
    }
    return new Pair<>(sequence, newObjVarIndex);
  }

  private Sequence methodsSequence(List<Method> methods, Sequence sequence, int varIndex) {
    for (Method m : methods) {
      TypedOperation operation = TypedOperation.forMethod(m);
      InputsAndSuccessFlag inputs = explorer.selectInputs(operation, true);
      Sequence concatSeq = Sequence.concatenate(inputs.sequences);
      List<Integer> indices = adjustIndices(inputs.indices, sequence.getLastVariable().index + 1);
      sequence = Sequence.concatenate(Arrays.asList(sequence, concatSeq));
      List<Variable> vars = CollectionsPlume.mapList(sequence::getVariable, indices);
      vars.add(0, sequence.getVariable(varIndex));
      sequence = sequence.extend(operation, vars);
    }
    return sequence;
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
}
