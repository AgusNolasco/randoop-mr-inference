package metamorphicRelationsInference.util.writer;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import metamorphicRelationsInference.epa.EPAState;
import metamorphicRelationsInference.metamorphicRelation.MetamorphicRelation;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class InferredMRsWriter {

  private final Class<?> cut;
  private static final String[] HEADERS = {"Metamorphic Relation", "Survives EPA"};
  private final String OUTPUTS_DIR = "output";
  private final String fileName = "randoop_mrs.csv";

  /**
   * Constructor
   *
   * @param cut is the class under test
   */
  public InferredMRsWriter(Class<?> cut) {
    Objects.requireNonNull(cut);
    this.cut = cut;
  }

  public void writeAllMRsProcessed(Set<MetamorphicRelation> mrs, Set<EPAState> states) {
    String dirName = OUTPUTS_DIR + "/" + cut.getSimpleName() + "/";
    File directory = new File(dirName);
    if (!directory.exists()) {
      directory.mkdirs();
    }
    try (Writer out =
        Files.newBufferedWriter(Paths.get(dirName + fileName), StandardCharsets.UTF_8)) {
      try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.EXCEL); ) {
        printer.printRecord(HEADERS[0], HEADERS[1]);
        for (MetamorphicRelation mr : mrs) {
          for (EPAState s : states) {
            try {
              if (mr.getStatesWhereSurvives().contains(s)) {
                printer.printRecord(s + " : " + mr.toFullString(), "1");
              } else {
                printer.printRecord(s + " : " + mr.toFullString(), "0");
              }
            } catch (IOException e) {
              e.printStackTrace();
              throw new IllegalStateException("Unable to persist the inferred mrs!");
            }
          }
        }
      }
    } catch (IOException e) {
      throw new IllegalStateException("Unable to write inferred mrs!", e);
    }
  }
}
