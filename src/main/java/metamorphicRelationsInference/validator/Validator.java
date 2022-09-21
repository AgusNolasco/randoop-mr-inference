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
      boolean allFails = true;
      Set<Bag> bagsWhereCheck =
          mr.getStatesWhereSurvives().stream().map(bags::get).collect(Collectors.toSet());
      System.out.println("----------------------");
      System.out.println("To be evaluated: " + mr);
      if (bagsWhereCheck.stream().allMatch(b -> b.getVariablesAndIndexes().isEmpty())) {
        System.out.println("There's no states to check this MR");
        continue;
      }
      boolean hasInitialStateBag = false;
      boolean hasNotInitialStateBag = false;
      for (Bag bag : bagsWhereCheck) {
        System.out.println("In: " + bag.toString());
        if (bag.isInitialStateBag()) {
          hasInitialStateBag = true;
        } else {
          hasNotInitialStateBag = true;
        }
        if (hasNotInitialStateBag && mr.hasLeftConstructor() && mr.hasRightConstructor()) {
          allFails = true;
          continue;
        }
        if (hasInitialStateBag && (!mr.hasLeftConstructor() || !mr.hasRightConstructor())) {
          allFails = true;
          continue;
        }
        if (counterExampleFound) {
          continue;
        }
        for (Pair<Variable, Integer> pair : bag.getVariablesAndIndexes()) {
          if (counterExampleFound) {
            continue;
          }
          Variable var = pair.getFst();
          Object result1, result2;
          try {
            executor.setup(mr, var);
            result1 = executor.getLeftResult();
            result2 = executor.getRightResult();
            allFails = false;
          } catch (Exception e) {
            continue;
          }
          if (!Distance.strongEquals(result1, result2)) {
            counterExampleFound = true;
            mr.setCounterExample(executor.getSequences(), new Pair<>(result1, result2));
          }
        }
      }
      if (!counterExampleFound && !allFails) {
        validMRs.add(mr);
      } else {
        if (counterExampleFound) {
          System.out.println("Counter example found");
          // System.out.println("Counter-example: \n");
          // System.out.println(mr.getCounterExampleSequences().getFst());
          // System.out.println(mr.getCounterExampleSequences().getSnd());
        } else {
          System.out.println("All the executed sequences fail for this MR");
        }
      }
    }
    System.out.println("----------------------\n");
    return validMRs;
  }
}
