package metamorphicRelationInference;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import metamorphicRelationInference.bag.Bag;
import metamorphicRelationInference.bag.BagsBuilder;
import metamorphicRelationInference.util.reader.EnabledMethodsReader;
import randoop.sequence.ExecutableSequence;

public class MetamorphicRelationInference {

  private static Class<?> cut;
  private static List<ExecutableSequence> sequences;
  private static Map<String, Map<Method, Boolean>> enabledMethodsPerState;
  private static Map<String, Bag> bags;

  // TODO: Replace this hard-coded dir for a parameter taken from bash or a env-var
  private static String pathToDir =
      "/Users/agustinnolasco/Documents/university/mfis/metamorphic-relations-inference/output/";
  private static String filename = "EnabledMethodsPerState.txt";

  public static void main(Class<?> clazz, List<ExecutableSequence> seq) {
    cut = clazz;
    sequences = seq.stream().filter(ExecutableSequence::isNormalExecution).collect(Collectors.toList());

    Objects.requireNonNull(cut);
    Objects.requireNonNull(sequences);

    String pathToFile = pathToDir + cut.getSimpleName() + "/" + filename;
    enabledMethodsPerState = EnabledMethodsReader.readEnabledMethodsPerState(cut, pathToFile);
    BagsBuilder builder = new BagsBuilder(cut, enabledMethodsPerState);
    bags = builder.createBags(sequences);

    for (String state : bags.keySet()) {
      for (Object elem : bags.get(state).getElements()) {
        System.out.println(state + " : " + elem);
      }
    }
  }
}
