package metamorphicRelationsInference.alloy;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import metamorphicRelationsInference.metamorphicRelation.MetamorphicRelation;

public class MRsToAlloyPred {

  private final Class<?> clazz;
  private final String DELIMITER = " # ";

  public MRsToAlloyPred(Class<?> clazz) {
    this.clazz = clazz;
  }

  public void save(List<MetamorphicRelation> mrs) {
    File file = new File("output/" + clazz.getSimpleName() + "/alloy_predicates.als");
    try (Writer writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
      for (MetamorphicRelation mr : mrs) {
        writer.write(mr + DELIMITER + mr.toAlloyPred(clazz) + "\n");
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
