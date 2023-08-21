package metamorphicRelationsInference.util.reader;

import java.util.ArrayList;
import java.util.List;
import metamorphicRelationsInference.util.AdditionalOptions;
import metamorphicRelationsInference.util.ReaderUtils;
import randoop.DummyVisitor;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Sequence;
import randoop.sequence.SequenceParseException;
import randoop.test.DummyCheckGenerator;

public class SequenceReader {

  public static List<ExecutableSequence> readSequences(
      String subjectName, int seed, AdditionalOptions options) {
    String pathToFile =
        "output/"
            + subjectName
            + "/"
            + "allow_epa_loops_"
            + options.isEPALoopsAllowed()
            + "/"
            + options.generationStrategy()
            + "/"
            + options.mrsToFuzz()
            + "/"
            + seed
            + "/"
            + "test-sequences.txt";
    List<ExecutableSequence> sequences = new ArrayList<>();
    List<String> newSequence = new ArrayList<>();
    for (String line : ReaderUtils.getLines(pathToFile)) {
      if (line.isEmpty()) {
        try {
          ExecutableSequence seq = new ExecutableSequence(Sequence.parse(newSequence));
          seq.execute(new DummyVisitor(), new DummyCheckGenerator());
          sequences.add(seq);
          newSequence.clear();
        } catch (SequenceParseException e) {
          e.printStackTrace();
        }
      } else {
        newSequence.add(line);
      }
    }
    return sequences;
  }
}
