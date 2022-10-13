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

  private final Executor executor;
  private final List<MetamorphicRelation> allMRsProcessed;

  public Validator(AbstractGenerator explorer) {
    executor = new Executor(explorer);
    allMRsProcessed = new ArrayList<>();
  }

  public List<MetamorphicRelation> validate(
      List<MetamorphicRelation> mrs, Map<EPAState, Bag> bags) {
    List<MetamorphicRelation> validMRs = new ArrayList<>();

    for (MetamorphicRelation mr : mrs) {
      allMRsProcessed.add(mr);
      System.out.println("----------------------");
      System.out.println("To be evaluated: " + mr);
      Set<Bag> bagsWhereCheck =
          mr.getStatesWhereSurvives().stream().map(bags::get).collect(Collectors.toSet());
      if (isValid(mr, bagsWhereCheck)) {
        System.out.println("Is valid MR for states: " + mr.getStatesWhereSurvives());
        validMRs.add(mr);
      } else {
        if (!mr.hasCounterExample()) {
          System.out.println("All the executed sequences fail for this MR");
        }
        System.out.println("MRs invalidated");
      }
    }

    System.out.println("----------------------\n");
    return validMRs;
  }

  private boolean isValid(MetamorphicRelation mr, Set<Bag> bags) {
    for (Bag bag : bags) {
      if (!isValidBag(mr, bag) || !isValidInBag(mr, bag)) {
        mr.removeFromStatesWhereSurvives(bag.getState());
      }
    }
    return !mr.getStatesWhereSurvives().isEmpty();
  }

  private boolean isValidBag(MetamorphicRelation mr, Bag bag) {
    if (bag.getVariablesAndIndexes().isEmpty()) {
      System.out.println("There's no states to check this MR in bag of state: " + bag.getState());
      return false;
    }
    if (bag.isInitialStateBag() && mr.hasBothConstructors()) {
      return true;
    }
    if (!bag.isInitialStateBag() && !mr.hasBothConstructors()) {
      return true;
    }
    System.out.println("The mr and the bags that it need are incompatible");
    return false;
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
        System.out.println("Counter example found for state: " + bag.getState());
        mr.addCounterExample(bag.getState(), executor.getSequences(), new Pair<>(result1, result2));
        return false;
      }
    }
    return !allFail;
  }

  public Set<MetamorphicRelation> getAllMRsProcessed() {
    Set<MetamorphicRelation> allMRsProcessed = new HashSet<>(this.allMRsProcessed);
    assert allMRsProcessed.size() == this.allMRsProcessed.size();
    return allMRsProcessed;
  }
}
