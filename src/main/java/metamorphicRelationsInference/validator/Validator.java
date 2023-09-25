package metamorphicRelationsInference.validator;

import java.util.*;
import java.util.stream.Collectors;
import metamorphicRelationsInference.bag.Bag;
import metamorphicRelationsInference.epa.EPAState;
import metamorphicRelationsInference.metamorphicRelation.MetamorphicRelation;
import metamorphicRelationsInference.util.AdditionalOptions;
import metamorphicRelationsInference.util.Pair;
import randoop.generation.AbstractGenerator;
import randoop.sequence.Variable;

public class Validator {

  private final Executor executor;
  private final List<MetamorphicRelation> allMRsProcessed;
  private final AdditionalOptions options;

  public Validator(AbstractGenerator explorer, AdditionalOptions options) {
    executor = new Executor(explorer);
    allMRsProcessed = new ArrayList<>();
    this.options = options;
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
        if (!mr.hasCounterExamples()) {
          System.out.println("All the executions fail for a given trace");
        }
        System.out.println("MRs invalidated");
      }
    }

    System.out.println("----------------------\n");
    return validMRs;
  }

  private boolean isValid(MetamorphicRelation mr, Set<Bag> bags) {
    for (Bag bag : bags) {
      if (bag.getVariablesAndIndexes().isEmpty() && options.isRunOverMutant()) {
        continue; // in mutation, if the precondition is false we say the property is valid
      }
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
    boolean allSetUpsFail = true;
    for (Pair<Variable, Integer> pair : bag.getVariablesAndIndexes()) {
      Variable var = pair.getFst();
      boolean isValid;
      try {
        boolean setUpFails = !executor.setup(mr, var);
        allSetUpsFail &= setUpFails;
        if (setUpFails) {
          continue;
        }
        isValid = executor.checkProperty(25);
        if (executor.allFail()) {
          System.out.println("MR failing in:\n" + var.sequence);
          System.out.println(
              "The exceptions were: \n"
                  + executor.getExceptions().stream()
                      .map(Objects::toString)
                      .collect(Collectors.joining("\n")));
          return false;
        }
      } catch (Exception e) {
        continue;
      }
      if (!isValid) {
        System.out.println("Counter example found for state: " + bag.getState());
        mr.addCounterExample(bag.getState(), executor.getSequences(), executor.getCounterExample());
        return false;
      }
    }
    return !allSetUpsFail;
  }

  public Set<MetamorphicRelation> getAllMRsProcessed() {
    Set<MetamorphicRelation> allMRsProcessed = new HashSet<>(this.allMRsProcessed);
    assert allMRsProcessed.size() == this.allMRsProcessed.size();
    return allMRsProcessed;
  }
}
