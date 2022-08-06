package metamorphicRelationsInference.validator;

import java.util.*;
import java.util.stream.Collectors;
import metamorphicRelationsInference.bag.Bag;
import metamorphicRelationsInference.epa.EPAState;
import metamorphicRelationsInference.metamorphicRelation.MetamorphicRelation;
import metamorphicRelationsInference.util.Pair;
import randoop.DummyVisitor;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Sequence;
import randoop.sequence.Variable;
import randoop.test.DummyCheckGenerator;

public class Validator {

  private final Class<?> cut;

  public Validator(Class<?> cut) {
    this.cut = cut;
  }

  public List<MetamorphicRelation> validate(
      List<MetamorphicRelation> metamorphicRelations, Map<EPAState, Bag> bags) {
    List<MetamorphicRelation> validMRs = new ArrayList<>();

    for (MetamorphicRelation mr : metamorphicRelations) {
      boolean counterExampleFound = false;
      Set<Bag> bagsWhereCheck =
          mr.getStatesWhereSurvives().stream().map(bags::get).collect(Collectors.toSet());
      System.out.println("----------------------");
      System.out.println("To be evaluated: " + mr);
      for (Bag bag : bagsWhereCheck) {
        System.out.println("In: " + bag.toString());
        if (counterExampleFound) {
          continue;
        }
        for (Pair<Variable, Integer> pair : bag.getVariablesAndIndexes()) {
          if (counterExampleFound) {
            continue;
          }
          Variable var = pair.getFst();
          Pair<Sequence, Sequence> sequences = mr.createSequences(var.sequence, var.index);
          ExecutableSequence seq1 = new ExecutableSequence(sequences.getFst());
          ExecutableSequence seq2 = new ExecutableSequence(sequences.getSnd());
          Object result1 = getObjectToCompare(seq1, pair.getSnd(), mr.getLeftConstructor() == null);
          Object result2 =
              getObjectToCompare(seq2, pair.getSnd(), mr.getRightConstructor() == null);
          if (!result1.toString().equals(result2.toString())) {
            counterExampleFound = true;
          }
        }
      }
      if (!counterExampleFound) {
        validMRs.add(mr);
      } else {
        System.out.println("falsified");
      }
    }
    System.out.println("----------------------");
    return validMRs;
  }

  private Object getObjectToCompare(
      ExecutableSequence sequence, Integer index, boolean nullConstructor) {
    sequence.execute(new DummyVisitor(), new DummyCheckGenerator());
    if (nullConstructor) {
      return sequence.getAllValues().get(index);
    }
    List<Object> values =
        sequence.getAllValues().stream()
            .filter(v -> v.getType().getCanonicalName().equals(cut.getName()))
            .collect(Collectors.toList());
    return values.get(values.size() - 1);
  }
}
