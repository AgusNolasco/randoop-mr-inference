package metamorphicRelationsInference.alloy;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import metamorphicRelationsInference.metamorphicRelation.MetamorphicRelation;
import metamorphicRelationsInference.util.AdditionalOptions;
import metamorphicRelationsInference.util.MRFormatter;

public class MRsToAlloyPred {

  private final String OUTPUT_DIR = "output";
  private final Class<?> clazz;
  private final String DELIMITER = " # ";

  public MRsToAlloyPred(Class<?> clazz) {
    this.clazz = clazz;
  }

  public void save(List<MetamorphicRelation> mrs, AdditionalOptions options) {
    String fileName;
    if (options.isRunOverFuzzedMRs()) {
      fileName = "fuzzed-valid-mrs-alloy-predicates.als";
    } else {
      fileName = "mrs-alloy-predicates.als";
    }
    String dirName =
        OUTPUT_DIR
            + "/"
            + clazz.getSimpleName()
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
    File file = new File(dirName + fileName);
    try (Writer writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
      MRFormatter formatter = new MRFormatter();
      for (MetamorphicRelation mr : mrs) {
        String formattedMr = formatter.formatMR(mr);
        writer.write(mr + DELIMITER + mr.toAlloyPred(clazz) + DELIMITER + formattedMr + "\n");
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
