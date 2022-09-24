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
      List<MetamorphicRelation> mrs, Map<EPAState, Bag> bags) {
    List<MetamorphicRelation> validMRs = new ArrayList<>();

    for (MetamorphicRelation mr : mrs) {
      System.out.println("----------------------");
      System.out.println("To be evaluated: " + mr);
      Set<Bag> bagsWhereCheck =
          mr.getStatesWhereSurvives().stream().map(bags::get).collect(Collectors.toSet());
      if (!areValidBags(mr, bagsWhereCheck)) {
        continue;
      }
      if (isValid(mr, bagsWhereCheck)) {
        System.out.println("Is valid MR");
        validMRs.add(mr);
      } else {
        if (mr.hasCounterExample()) {
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

  private boolean areValidBags(MetamorphicRelation mr, Set<Bag> bags) {
    if (bags.stream().allMatch(b -> b.getVariablesAndIndexes().isEmpty())) {
      System.out.println("There's no states to check this MR");
      return false;
    }
    boolean haveInitialStateBag = bags.stream().anyMatch(Bag::isInitialStateBag);
    boolean haveNonInitialStateBag = bags.stream().anyMatch(b -> !b.isInitialStateBag());
    if ((haveInitialStateBag && (!mr.hasLeftConstructor() || !mr.hasRightConstructor()))
        || (haveNonInitialStateBag && mr.hasLeftConstructor() && mr.hasRightConstructor())) {
      System.out.println("The mr and the bags that it need are incompatible");
      return false;
    }
    return true;
  }

  private boolean isValid(MetamorphicRelation mr, Set<Bag> bags) {
    for (Bag bag : bags) {
      System.out.println("In: " + bag.toString());
      boolean allFail = true;
      for (Pair<Variable, Integer> pair : bag.getVariablesAndIndexes()) {
        Variable var = pair.getFst();
        Object result1, result2;
        try {
          executor.setup(mr, var);
          result1 = executor.getLeftResult();
          result2 = executor.getRightResult();
          allFail = false;
        } catch (Exception e) {
          continue;
        }
        if (!Distance.strongEquals(result1, result2)) {
          mr.setCounterExample(executor.getSequences(), new Pair<>(result1, result2));
          return false;
        }
      }
      if (allFail) {
        return false;
      }
    }
    return true;
  }
}
