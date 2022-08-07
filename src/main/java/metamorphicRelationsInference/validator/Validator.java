package metamorphicRelationsInference.validator;

import java.util.*;
import java.util.stream.Collectors;
import metamorphicRelationsInference.bag.Bag;
import metamorphicRelationsInference.epa.EPAState;
import metamorphicRelationsInference.metamorphicRelation.MetamorphicRelation;
import metamorphicRelationsInference.util.Pair;
import randoop.DummyVisitor;
import randoop.generation.AbstractGenerator;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Sequence;
import randoop.sequence.Variable;
import randoop.test.DummyCheckGenerator;

public class Validator {

  private final AbstractGenerator explorer;

  public Validator(AbstractGenerator explorer) {
    this.explorer = explorer;
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
          Pair<Sequence, Sequence> sequences =
              mr.createSequences(var.sequence, var.index, explorer);
          ExecutableSequence seq1 = new ExecutableSequence(sequences.getFst());
          ExecutableSequence seq2 = new ExecutableSequence(sequences.getSnd());
          Object result1 = getObjectToCompare(seq1, mr.getVariablesToCompare().getFst());
          Object result2 = getObjectToCompare(seq2, mr.getVariablesToCompare().getSnd());
          System.out.println(result1.toString() + " - " + result2.toString());
          System.out.println(seq1.sequence);
          System.out.println(seq2.sequence);
          System.out.println("**********************");
          if (!result1.toString().equals(result2.toString())) {
            counterExampleFound = true;
            mr.setCounterExample(new Pair<>(seq1.sequence, seq2.sequence));
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

  private Object getObjectToCompare(ExecutableSequence sequence, Variable var) {
    sequence.execute(new DummyVisitor(), new DummyCheckGenerator());
    Object[] values =
        ExecutableSequence.getRuntimeValuesForVars(
            Collections.singletonList(var), sequence.executionResults);
    return values[0];
  }
}
