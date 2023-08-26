package metamorphicRelationsInference.util;

import java.util.ArrayList;
import java.util.List;
import randoop.generation.InputsAndSuccessFlag;

public class OperationInputs {

  List<InputsAndSuccessFlag> inputsPerOperation;

  public OperationInputs() {
    inputsPerOperation = new ArrayList<>();
  }

  public void add(InputsAndSuccessFlag inputs) {
    inputsPerOperation.add(inputs);
  }

  public InputsAndSuccessFlag get(int i) {
    return inputsPerOperation.get(i);
  }

  public boolean isEmpty() {
    return inputsPerOperation.isEmpty();
  }

  public int size() {
    return inputsPerOperation.size();
  }
}
