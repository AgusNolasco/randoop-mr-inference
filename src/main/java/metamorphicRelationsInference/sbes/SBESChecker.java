package metamorphicRelationsInference.sbes;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ClassUtils;
import randoop.DummyVisitor;
import randoop.generation.ComponentManager;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.ReferenceValue;
import randoop.sequence.Sequence;
import randoop.sequence.Variable;
import randoop.test.DummyCheckGenerator;
import randoop.types.Type;

public class SBESChecker {

  private static ComponentManager componentManager;
  private static final Random rand = new Random(0);
  private static final String mutantsNum = System.getenv("MUTANT_NUM");

  public static void checkMRs(
      Class<?> cut, List<ExecutableSequence> seqs, ComponentManager compManager) {
    componentManager = compManager;
    System.out.println("********** SBES-Checker **********");
    List<ExecutableSequence> sequences =
        seqs.stream().filter(ExecutableSequence::isNormalExecution).collect(Collectors.toList());

    List<Variable> vars = loadCUTVars(cut, sequences);
    Set<String> mrsKillingMutant = new HashSet<>();

    try {
      Class<?> checker = ClassUtils.getClass("sbes.SBESChecker" + cut.getSimpleName());
      System.out.println(checker.getSimpleName());

      try {
        vars =
            vars.stream()
                .filter(var -> getObjectFromVar(var).equals(getObjectFromVar(var)))
                .collect(Collectors.toList());
        assert vars.stream().allMatch(var -> getObjectFromVar(var).equals(getObjectFromVar(var)));
      } catch (Exception e) {
        mrsKillingMutant.addAll(
            Arrays.stream(checker.getDeclaredMethods())
                .map(Method::getName)
                .collect(Collectors.toSet()));
        writeResults(cut, checker, mrsKillingMutant);
        System.exit(0);
      }

      for (Method method : checker.getDeclaredMethods()) {
        if (vars.isEmpty()) {
          break;
        }
        List<Class<?>> params =
            Arrays.stream(method.getParameterTypes()).skip(2).collect(Collectors.toList());
        System.out.println("Checking: " + method.getName());
        boolean counterExampleFound = false;
        boolean allFail = true;
        for (Variable var : vars) {
          List<Object> args = new ArrayList<>();
          for (Class<?> param : params) {
            args.add(getObjectOfType(param));
          }
          Object obj = getObjectFromVar(var);
          Object copy = getObjectFromVar(var);
          args.add(0, obj);
          args.add(0, copy);
          boolean result;
          try {
            result = (boolean) method.invoke(null, args.toArray());
            allFail = false;
          } catch (Exception e) {
            continue;
          }
          if (!result) {
            counterExampleFound = true;
            break;
          }
        }
        if (allFail) {
          mrsKillingMutant.add(method.getName());
          break;
        }
        if (counterExampleFound) {
          mrsKillingMutant.add(method.getName());
          break;
        }
      }

      writeResults(cut, checker, mrsKillingMutant);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  private static void writeResults(Class<?> cut, Class<?> checker, Set<String> mrsKillingMutant) {
    String fileName = "SBES-mutant-results.txt";
    String dirName = "output/" + cut.getSimpleName() + "/sbes-mutation/" + mutantsNum + "/";
    File directory = new File(dirName);
    if (!directory.exists()) {
      directory.mkdirs();
    }
    File file = new File(dirName + fileName);
    try (Writer writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
      writer.write(
          "Mutant " + mutantsNum + " killed? : " + (mrsKillingMutant.isEmpty() ? 0 : 1) + "\n");
      for (String mr :
          Arrays.stream(checker.getDeclaredMethods())
              .map(Method::getName)
              .collect(Collectors.toList())) {
        writer.write(mr + " : " + (mrsKillingMutant.contains(mr) ? 1 : 0) + "\n");
      }
    } catch (IOException e) {
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

  private static Object getObjectOfType(Class<?> c) {
    List<Sequence> seqs = componentManager.getSequencesForType(Type.forClass(c)).toJDKList();
    seqs = executableSequences(seqs);

    Sequence seq = seqs.get(rand.nextInt(seqs.size()));
    ExecutableSequence excSeq = new ExecutableSequence(seq);
    excSeq.execute(new DummyVisitor(), new DummyCheckGenerator());
    Object[] values =
        ExecutableSequence.getRuntimeValuesForVars(
            Collections.singletonList(seq.getVariable(0)), excSeq.executionResults);
    assert values[0] != null;
    return values[0];
  }

  private static List<Variable> loadCUTVars(Class<?> cut, List<ExecutableSequence> seqs) {
    List<Variable> vars = new ArrayList<>();
    for (ExecutableSequence seq : seqs) {
      seq.execute(new DummyVisitor(), new DummyCheckGenerator());
      for (ReferenceValue referenceValue : seq.getAllValues()) {
        if (referenceValue.getType().getCanonicalName().equals(cut.getName())) {
          Variable var = seq.getVariable(referenceValue.getObjectValue());
          vars.add(var);
        }
      }
    }
    return vars;
  }

  private static List<Sequence> executableSequences(List<Sequence> sequences) {
    List<Sequence> executableSeqs = new ArrayList<>();
    for (Sequence seq : sequences) {
      ExecutableSequence excSeq = new ExecutableSequence(seq);
      excSeq.execute(new DummyVisitor(), new DummyCheckGenerator());
      if (excSeq.isNormalExecution()) {
        executableSeqs.add(seq);
      }
    }
    return executableSeqs;
  }
}
