package metamorphicRelationInference.bag;

import java.util.*;
import metamorphicRelationInference.util.Pair;
import randoop.DummyVisitor;
import randoop.sequence.ExecutableSequence;
import randoop.test.DummyCheckGenerator;

public class Bag {

  private String state;
  private List<Pair<ExecutableSequence, Integer>> getters;

  public Bag(String state) {
    this.state = state;
    getters = new ArrayList<>();
  }

  public void add(Pair<ExecutableSequence, Integer> p) {
    getters.add(p);
  }

  public List<Object> getElements() {
    List<Object> elements = new ArrayList<>();
    for (Pair<ExecutableSequence, Integer> p : getters) {
      elements.add(getObject(p.getFst(), p.getSnd()));
    }
    return elements;
  }

  public static Object getObject(ExecutableSequence sequence, Integer index) {
    sequence.execute(new DummyVisitor(), new DummyCheckGenerator());
    return sequence.getAllValues().get(index).getObjectValue();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || !(o instanceof Bag)) return false;

    Bag bag = (Bag) o;

    return state.equals(bag.state);
  }

  @Override
  public int hashCode() {
    return state.hashCode();
  }
}
