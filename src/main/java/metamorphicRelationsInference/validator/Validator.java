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
      if (isValid(mr, bagsWhereCheck)) {
        System.out.println("Is valid MR");
        for (EPAState state : mr.getCounterExampledStates()) {
          System.out.println("Was counter exampled for state: " + state);
          System.out.println(mr.getCounterExampleSequences(state).getFst());
          System.out.println(mr.getCounterExampleSequences(state).getSnd());
        }
        validMRs.add(mr);
      } else {
        if (mr.hasCounterExample()) {
          System.out.println("Counter example found");
        } else {
          System.out.println("All the executed sequences fail for this MR");
        }
      }
    }

    System.out.println("----------------------\n");
    return validMRs;
  }

  private boolean isValidBag(MetamorphicRelation mr, Bag bag) {
    if (bag.getVariablesAndIndexes().isEmpty()) {
      System.out.println("There's no states to check this MR in bag of state: " + bag.getState());
      return false;
    }
    if (bag.isInitialStateBag() && mr.hasBothConstructors()) {
      return true;
    }
    System.out.println("The mr and the bags that it need are incompatible");
    return false;
  }

  private boolean isValid(MetamorphicRelation mr, Set<Bag> bags) {
    for (Bag bag : bags) {
      if (!isValidBag(mr, bag) || !isValidInBag(mr, bag)) {
        mr.removeFromStatesWhereSurvives(bag.getState());
      }
    }
    return !mr.getStatesWhereSurvives().isEmpty();
  }

  private boolean isValidInBag(MetamorphicRelation mr, Bag bag) {
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
        mr.addCounterExample(bag.getState(), executor.getSequences(), new Pair<>(result1, result2));
        return false;
      }
    }
    return !allFail;
  }
}
