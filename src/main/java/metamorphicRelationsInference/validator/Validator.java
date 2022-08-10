package metamorphicRelationsInference.validator;

import java.util.*;
import java.util.stream.Collectors;
import metamorphicRelationsInference.bag.Bag;
import metamorphicRelationsInference.distance.Distance;
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
      if (bagsWhereCheck.stream().allMatch(b -> b.getVariablesAndIndexes().isEmpty())) {
        System.out.println("There's no states to check this MR");
        continue;
      }
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
          // System.out.println(executor.getSequences().getFst());
          // System.out.println(executor.getSequences().getSnd());
          // System.out.println(result1.toString() + " - " + result2.toString());
          if (Distance.distance(result1, result2) != 0.0d) {
            System.out.println("Objects distance:" + Distance.distance(result1, result2));
            counterExampleFound = true;
            mr.setCounterExample(executor.getSequences(), new Pair<>(result1, result2));
          }
        }
      }
      if (!counterExampleFound) {
        validMRs.add(mr);
      } else {
        System.out.println("falsified");
        System.out.println("Counter-example: \n");
        System.out.println(mr.getCounterExampleSequences().getFst());
        System.out.println(mr.getCounterExampleSequences().getSnd());
        System.out.println(
            mr.getCounterExampleObjects().getFst()
                + " - "
                + mr.getCounterExampleObjects().getSnd());
      }
    }
    System.out.println("----------------------\n");
    return validMRs;
  }
}
