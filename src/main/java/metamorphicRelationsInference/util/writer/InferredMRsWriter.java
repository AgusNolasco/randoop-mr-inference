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
import metamorphicRelationsInference.util.AdditionalOptions;
import metamorphicRelationsInference.util.MRFormatter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class InferredMRsWriter {

  private static final String[] HEADERS = {"Metamorphic Relation", "Survives EPA"};
  private final String OUTPUTS_DIR = "output";
  private final String subject_name;

  /** Constructor */
  public InferredMRsWriter(String subject_name) {
    Objects.requireNonNull(subject_name);
    this.subject_name = subject_name;
  }

  public void writeAllMRsProcessed(
      Set<MetamorphicRelation> mrs, Set<EPAState> states, AdditionalOptions options) {
    String fileName;
    if (options.isRunOverFuzzedMRs()) {
      fileName = "randoop-run-over-fuzzed-mrs.csv";
    } else {
      fileName = "randoop-mrs.csv";
    }
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
            + "/";
    if (options.isRandom()) {
      dirName += "random/";
    }
    File directory = new File(dirName);
    if (!directory.exists()) {
      directory.mkdirs();
    }
    try (Writer out =
        Files.newBufferedWriter(Paths.get(dirName + fileName), StandardCharsets.UTF_8)) {
      try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.EXCEL)) {
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

  public void writeAllMRsProcessedFormatted(
      List<MetamorphicRelation> mrs, AdditionalOptions options) {
    String fileName = "formatted-mrs.csv";
    if (options.isRunOverFuzzedMRs()) {
      return; // Do nothing if the execution is over all fuzzed mrs
    }
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
            + "/";
    if (options.isRandom()) {
      dirName += "random/";
    }
    File directory = new File(dirName);
    if (!directory.exists()) {
      directory.mkdirs();
    }
    File file = new File(dirName + fileName);
    try (Writer writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
      MRFormatter formatter = new MRFormatter();
      for (MetamorphicRelation mr : mrs) {
        String formattedMr = formatter.formatMR(mr);
        writer.write(formattedMr + "\n");
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
