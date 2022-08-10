package metamorphicRelationsInference;

import java.util.*;
import java.util.stream.Collectors;
import metamorphicRelationsInference.bag.Bag;
import metamorphicRelationsInference.bag.BagsBuilder;
import metamorphicRelationsInference.epa.EPAState;
import metamorphicRelationsInference.metamorphicRelation.MetamorphicRelation;
import metamorphicRelationsInference.util.reader.CandidatesReader;
import metamorphicRelationsInference.util.reader.EnabledMethodsReader;
import metamorphicRelationsInference.validator.Validator;
import randoop.generation.AbstractGenerator;
import randoop.sequence.ExecutableSequence;

public class MetamorphicRelationInference {

  private static List<ExecutableSequence> sequences;
  private static final String pathToDir = System.getenv("OUTPUTS_DIR");

  public static void main(Class<?> cut, List<ExecutableSequence> seq, AbstractGenerator explorer) {
    sequences =
        seq.stream().filter(ExecutableSequence::isNormalExecution).collect(Collectors.toList());

    Objects.requireNonNull(cut);
    Objects.requireNonNull(sequences);

    String pathToFile =
        String.join("/", pathToDir, cut.getSimpleName(), "EnabledMethodsPerState.txt");
    Set<EPAState> states = EnabledMethodsReader.readEnabledMethodsPerState(cut, pathToFile);
    BagsBuilder builder = new BagsBuilder(cut, states);
    Map<EPAState, Bag> bags = builder.createBags(sequences);

    pathToFile = String.join("/", pathToDir, cut.getSimpleName(), "Candidates.csv");
    CandidatesReader reader = new CandidatesReader(cut);
    List<MetamorphicRelation> metamorphicRelations = reader.read(pathToFile);

    /* Validation phase */
    Validator validator = new Validator(explorer);
    List<MetamorphicRelation> validMRs = validator.validate(metamorphicRelations, bags);

    /* Output */
    System.out.println("Class: " + cut.getSimpleName() + "\n");

    System.out.println("Valid MRs: \n");
    for (MetamorphicRelation mr : validMRs) {
      System.out.println(mr);
    }
    System.out.println("\nInput:  " + metamorphicRelations.size() + " MRs");
    System.out.println("Output: " + validMRs.size() + " MRs\n");
    System.out.println(
        "% of valid MRs: " + ((float) validMRs.size() / (float) metamorphicRelations.size()) * 100);
    System.out.println(
        "% of invalid MRs: "
            + ((float) (metamorphicRelations.size() - validMRs.size())
                    / (float) metamorphicRelations.size())
                * 100);

    for (EPAState s : bags.keySet()) {
      System.out.println(s + " -> size: " + bags.get(s).getVariablesAndIndexes().size());
    }
  }
}
