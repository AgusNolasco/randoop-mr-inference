package metamorphicRelationsInference;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import metamorphicRelationsInference.bag.Bag;
import metamorphicRelationsInference.bag.BagsBuilder;
import metamorphicRelationsInference.metamorphicRelation.MetamorphicRelation;
import metamorphicRelationsInference.util.reader.CandidatesReader;
import metamorphicRelationsInference.util.reader.EnabledMethodsReader;
import randoop.sequence.ExecutableSequence;

public class MetamorphicRelationInference {

  private static List<ExecutableSequence> sequences;
  private static Map<String, Map<Method, Boolean>> enabledMethodsPerState;

  // TODO: Replace this hard-coded dir for a parameter taken from bash or a env-var
  private static String pathToDir =
      "/Users/agustinnolasco/Documents/university/mfis/metamorphic-relations-inference/output/";
  private static String enabledMethodsPerStateFilename = "EnabledMethodsPerState.txt";
  private static String mrsCandidatesFilename = "Candidates.csv";

  public static void main(Class<?> cut, List<ExecutableSequence> seq) {
    sequences =
        seq.stream().filter(ExecutableSequence::isNormalExecution).collect(Collectors.toList());

    Objects.requireNonNull(cut);
    Objects.requireNonNull(sequences);

    String pathToFile = pathToDir + cut.getSimpleName() + "/" + enabledMethodsPerStateFilename;
    enabledMethodsPerState = EnabledMethodsReader.readEnabledMethodsPerState(cut, pathToFile);
    BagsBuilder builder = new BagsBuilder(cut, enabledMethodsPerState);
    Map<String, Bag> bags = builder.createBags(sequences);

    pathToFile = pathToDir + cut.getSimpleName() + "/" + mrsCandidatesFilename;
    CandidatesReader reader = new CandidatesReader(cut);
    Set<MetamorphicRelation> metamorphicRelations = reader.read(pathToFile);

    System.out.println(bags + "" + metamorphicRelations);
  }
}
