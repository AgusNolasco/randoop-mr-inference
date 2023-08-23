package metamorphicRelationsInference.util.writer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import metamorphicRelationsInference.util.AdditionalOptions;
import randoop.sequence.ExecutableSequence;

public class SequenceWriter {

  private final String OUTPUTS_DIR = "output";
  private final String subject_name;
  private final int seed;

  /** Constructor */
  public SequenceWriter(String subject_name, int seed) {
    Objects.requireNonNull(subject_name);
    this.subject_name = subject_name;
    this.seed = seed;
  }

  public void saveSequences(List<ExecutableSequence> sequences, AdditionalOptions options) {
    String fileName = "test-sequences.txt";
    String dirName =
        OUTPUTS_DIR
            + "/"
            + subject_name
            + "/"
            + "allow_epa_loops_"
            + options.isEPALoopsAllowed()
            + "/"
            + options.generationStrategy()
            + "/"
            + options.mrsToFuzz()
            + "/"
            + seed
            + "/";
    File directory = new File(dirName);
    if (!directory.exists()) {
      directory.mkdirs();
    }
    try (FileWriter fileWriter = new FileWriter(dirName + fileName)) {
      for (ExecutableSequence sequence : sequences) {
        fileWriter.write(sequence.sequence.toParsableString() + "\n");
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
