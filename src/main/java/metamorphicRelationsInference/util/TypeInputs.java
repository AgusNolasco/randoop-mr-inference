package metamorphicRelationsInference.util;

import java.util.ArrayList;
import java.util.List;
import randoop.sequence.Sequence;
import randoop.sequence.Variable;

public class TypeInputs {

  List<Pair<Variable, Sequence>> inputs;

  public TypeInputs() {
    inputs = new ArrayList<>();
  }

  public void add(Variable var, Sequence seq) {
    inputs.add(new Pair<>(var, seq));
  }

  public Variable getVar(int i) {
    return inputs.get(i).getFst();
  }

  public Sequence getSeq(int i) {
    return inputs.get(i).getSnd();
  }

  public int size() {
    return inputs.size();
  }
}
