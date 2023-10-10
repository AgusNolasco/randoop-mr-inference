package metamorphicRelationsInference;

import java.nio.file.Files;
import java.nio.file.Paths;
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
import metamorphicRelationsInference.util.reader.EvoSuiteTestReader;
import metamorphicRelationsInference.util.writer.InferredMRsWriter;
import metamorphicRelationsInference.util.writer.SequenceWriter;
import metamorphicRelationsInference.validator.Validator;
import randoop.generation.AbstractGenerator;
import randoop.sequence.ExecutableSequence;

public class MetamorphicRelationInference {

  private static List<ExecutableSequence> sequences;
  private static final String pathToOutput = System.getenv("MRS_DIR");
  private static final String subjectName = System.getenv("SUBJECT_NAME");
  private static final String evosuiteTests = System.getenv("EVOSUITE_TESTS");

  public static void main(
      Class<?> cut, AbstractGenerator explorer, AdditionalOptions options, int seed) {

    final String mrsToEvalFileName;
    if (options.isRunOverFuzzedMRs()) {
      mrsToEvalFileName = "fuzzed-mrs.csv";
    } else {
      mrsToEvalFileName = "candidates.csv";
    }

    // if (options.isRunOverMutant()) {
    //   sequences = SequenceReader.readSequences(subjectName, seed, options);
    // } else {
    sequences = explorer.getRegressionSequences();
    if (evosuiteTests != null
        && !evosuiteTests.isEmpty()
        && Files.exists(Paths.get(evosuiteTests))) {
      List<ExecutableSequence> evoSuiteTests = EvoSuiteTestReader.readFromFile(evosuiteTests);
      sequences.addAll(evoSuiteTests);
      System.out.println(evoSuiteTests.size() + " Evo+EPA tests added successfully");
    }
    // }

    sequences =
        sequences.stream()
            .filter(ExecutableSequence::isNormalExecution)
            .collect(Collectors.toList());

    Objects.requireNonNull(cut);
    Objects.requireNonNull(sequences);

    /* Read the file to know where each sequence correspond */
    String pathToEnabledMethodsPerState =
        String.join(
            "/",
            System.getenv("EPA_INFERENCE_DIR"),
            "output",
            subjectName,
            "enabled-methods-per-state.txt");
    Set<EPAState> states =
        EnabledMethodsReader.readEnabledMethodsPerState(cut, pathToEnabledMethodsPerState);
    BagsBuilder builder = new BagsBuilder(cut, states);
    boolean isEpaBroken = false;
    Map<EPAState, Bag> bags = null;
    try {
      bags = builder.createBags(sequences, options.isRunOverMutant());
    } catch (Exception e) {
      System.out.println("The EPA is broken");
      if (!options.isRunOverMutant()) {
        System.exit(1);
      }
      isEpaBroken = true;
    }

    /* Take the MRs from the previous phase */
    String pathToMRs =
        String.join(
            "/",
            pathToOutput,
            subjectName,
            "allow_epa_loops_" + options.isEPALoopsAllowed(),
            options.generationStrategy().toString(),
            String.valueOf(options.mrsToFuzz()),
            String.valueOf(seed));
    if (options.isRandom()) {
      pathToMRs += "/random";
    }

    String pathToCandidates = pathToMRs + "/" + mrsToEvalFileName;
    CandidatesReader reader = new CandidatesReader(cut);
    List<MetamorphicRelation> metamorphicRelations = reader.read(pathToCandidates);
    int totalInput =
        metamorphicRelations.stream()
            .map(MetamorphicRelation::getStatesWhereSurvives)
            .map(Set::size)
            .mapToInt(i -> i)
            .sum();

    /* Validation phase */
    Validator validator = new Validator(explorer, options);
    List<MetamorphicRelation> validMRs = new ArrayList<>();
    if (!isEpaBroken || !options.isRunOverMutant()) {
      validMRs = validator.validate(metamorphicRelations, bags);
    }
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
    assert bags != null;
    for (EPAState s : bags.keySet()) {
      System.out.println(s + " -> size: " + bags.get(s).getVariablesAndIndexes().size());
    }

    if (!options.isRunOverMutant()) {
      InferredMRsWriter writer = new InferredMRsWriter(subjectName, seed);
      writer.writeAllMRsProcessed(validator.getAllMRsProcessed(), bags.keySet(), options);
      writer.writeAllMRsProcessedFormatted(validMRs, options);

      MRsToAlloyPred alloyPred = new MRsToAlloyPred(subjectName, cut, seed);
      alloyPred.save(validMRs, options);
    }

    if (!options.isRunOverMutant()) {
      SequenceWriter sequenceWriter = new SequenceWriter(subjectName, seed);
      sequenceWriter.saveSequences(sequences, options);
    }
  }
}
