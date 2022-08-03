package metamorphicRelationInference.util;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

public class ReaderUtils {

  public static List<String> getLines(String pathToFile) {
    File file = new File(pathToFile);
    try (BufferedReader br = Files.newBufferedReader(file.toPath(), Charset.defaultCharset())) {
      return br.lines().collect(Collectors.toList());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
