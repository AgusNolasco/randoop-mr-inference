package metamorphicRelationsInference.bag;

import java.util.*;
import metamorphicRelationsInference.epa.EPAState;
import metamorphicRelationsInference.util.Pair;
import randoop.sequence.Variable;

public class Bag {

  private final EPAState state;
  // These pairs store the variable and the index of the referred value in the sequence
  private final List<Pair<Variable, Integer>> variablesAndIndexes;

  public Bag(EPAState state) {
    this.state = state;
    variablesAndIndexes = new ArrayList<>();
  }

  public void add(Pair<Variable, Integer> p) {
    variablesAndIndexes.add(p);
  }

  public boolean isInitialStateBag() {
    return state.isInitial();
  }

  public List<Pair<Variable, Integer>> getVariablesAndIndexes() {
    return variablesAndIndexes;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Bag)) return false;

    Bag bag = (Bag) o;

    return state.equals(bag.state);
  }

  @Override
  public int hashCode() {
    return state.hashCode();
  }

  @Override
  public String toString() {
    return state + " -> size: " + variablesAndIndexes.size();
  }
}
