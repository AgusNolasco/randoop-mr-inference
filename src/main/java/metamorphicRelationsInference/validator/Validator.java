package metamorphicRelationsInference.validator;

import java.util.*;
import java.util.stream.Collectors;
import metamorphicRelationsInference.bag.Bag;
import metamorphicRelationsInference.epa.EPAState;
import metamorphicRelationsInference.metamorphicRelation.MetamorphicRelation;
import metamorphicRelationsInference.util.Pair;
import randoop.generation.AbstractGenerator;
import randoop.sequence.Variable;

public class Validator {

  Executor executor;

  public Validator(AbstractGenerator explorer) {
    executor = new Executor(explorer);
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
          executor.setup(mr, var);
          Object result1 = executor.getLeftResult();
          Object result2 = executor.getRightResult();
          if (!result1.toString().equals(result2.toString())) {
            counterExampleFound = true;
            mr.setCounterExample(executor.getSequences());
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
}
