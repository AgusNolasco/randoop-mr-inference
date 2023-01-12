package metamorphicRelationsInference;

import java.util.*;
import java.util.stream.Collectors;
import metamorphicRelationsInference.alloy.MRsToAlloyPred;
import metamorphicRelationsInference.bag.Bag;
import metamorphicRelationsInference.bag.BagsBuilder;
import metamorphicRelationsInference.epa.EPAState;
import metamorphicRelationsInference.metamorphicRelation.MetamorphicRelation;
import metamorphicRelationsInference.util.AdditionalOptions;
import metamorphicRelationsInference.util.reader.CandidatesReader;
import metamorphicRelationsInference.util.reader.EnabledMethodsReader;
import metamorphicRelationsInference.util.writer.InferredMRsWriter;
import metamorphicRelationsInference.validator.Validator;
import randoop.generation.AbstractGenerator;
import randoop.sequence.ExecutableSequence;

public class MetamorphicRelationInference {

  private static List<ExecutableSequence> sequences;
  private static final String pathToDir = System.getenv("OUTPUTS_DIR");

  public static void main(
      Class<?> cut,
      List<ExecutableSequence> seq,
      AbstractGenerator explorer,
      AdditionalOptions options) {

    final String mrsToEvalFileName;
    if (options.runOverFuzzerMRs()) {
      // Use this for precision and recall computation
      mrsToEvalFileName = "fuzzed-mrs.csv";
    } else {
      mrsToEvalFileName = "candidates.csv";
    }

    sequences =
        seq.stream().filter(ExecutableSequence::isNormalExecution).collect(Collectors.toList());

    Objects.requireNonNull(cut);
    Objects.requireNonNull(sequences);

    /* Read the file to know where each sequence correspond */
    String pathToEnabledMethodsPerState =
        String.join("/", pathToDir, cut.getSimpleName(), "enabled-methods-per-state.txt");
    Set<EPAState> states =
        EnabledMethodsReader.readEnabledMethodsPerState(cut, pathToEnabledMethodsPerState);
    BagsBuilder builder = new BagsBuilder(cut, states);
    Map<EPAState, Bag> bags = builder.createBags(sequences);

    /* Take the MRs from the previous phase */
    String pathToCandidates =
        String.join(
            "/",
            pathToDir,
            cut.getSimpleName(),
            "allow_epa_loops_" + options.isEPALoopsAllowed(),
            options.generationStrategy().toString(),
            String.valueOf(options.mrsToFuzz()),
            mrsToEvalFileName);
    CandidatesReader reader = new CandidatesReader(cut);
    List<MetamorphicRelation> metamorphicRelations = reader.read(pathToCandidates);
    int totalInput =
        metamorphicRelations.stream()
            .map(MetamorphicRelation::getStatesWhereSurvives)
            .map(Set::size)
            .mapToInt(i -> i)
            .sum();

    /* Validation phase */
    Validator validator = new Validator(explorer);
    List<MetamorphicRelation> validMRs = validator.validate(metamorphicRelations, bags);
    int totalOutput =
        validMRs.stream()
            .map(MetamorphicRelation::getStatesWhereSurvives)
            .map(Set::size)
            .mapToInt(i -> i)
            .sum();

    /* Output */
    System.out.println("Class: " + cut.getSimpleName() + "\n");

    System.out.println("Valid MRs: \n");
    for (MetamorphicRelation mr : validMRs) {
      System.out.println(mr);
    }
    System.out.println("\nInput:  " + totalInput + " MRs");
    System.out.println("Output: " + totalOutput + " MRs\n");
    System.out.println(
        "% of valid MRs: " + ((float) validMRs.size() / (float) metamorphicRelations.size()) * 100);
    System.out.println(
        "% of invalid MRs: "
            + ((float) (metamorphicRelations.size() - validMRs.size())
                    / (float) metamorphicRelations.size())
                * 100);
    System.out.println();
    for (EPAState s : bags.keySet()) {
      System.out.println(s + " -> size: " + bags.get(s).getVariablesAndIndexes().size());
    }

    InferredMRsWriter writer = new InferredMRsWriter(cut);
    writer.writeAllMRsProcessed(validator.getAllMRsProcessed(), bags.keySet(), options);

    MRsToAlloyPred alloyPred = new MRsToAlloyPred(cut);
    alloyPred.save(validMRs, options);
  }
}
