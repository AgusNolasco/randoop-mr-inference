package metamorphicRelationsInference.alloy;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import metamorphicRelationsInference.epa.EPAState;
import metamorphicRelationsInference.metamorphicRelation.MetamorphicRelation;
import metamorphicRelationsInference.util.AdditionalOptions;

public class MRsToAlloyPred {

  private final String OUTPUT_DIR = "output";
  private final Class<?> clazz;
  private final String DELIMITER = " # ";
  private final String COMPONENTS_DELIMITER = ",";
  private final String ACTIONS_DELIMITER = ";";
  private final String PARAMS_DELIMITER = ":";

  public MRsToAlloyPred(Class<?> clazz) {
    this.clazz = clazz;
  }

  public void save(List<MetamorphicRelation> mrs, AdditionalOptions options) {
    String fileName;
    if (options.runOverFuzzerMRs()) {
      fileName = "fuzzed-valid-mrs-alloy-predicates.als";
    } else {
      fileName = "mrs-alloy-predicates.als";
    }
    File file =
        new File(
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
                + "/"
                + fileName);
    try (Writer writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
      for (MetamorphicRelation mr : mrs) {
        List<String> components = new ArrayList<>();
        components.add(constructorParamsToString(mr.getLeftConstructor()));
        components.add(methodsToString(mr.getLeftMethods()));
        components.add(constructorParamsToString(mr.getRightConstructor()));
        components.add(methodsToString(mr.getRightMethods()));
        components.add(statesWhereSurvivesToString(mr.getStatesWhereSurvives()));
        String formattedMr = String.join(COMPONENTS_DELIMITER, components);
        writer.write(mr + DELIMITER + mr.toAlloyPred(clazz) + DELIMITER + formattedMr + "\n");
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private String constructorParamsToString(Constructor<?> constructor) {
    return constructor == null ? "" : parametersToString(constructor.getParameterTypes());
  }

  private String methodsToString(List<Method> methods) {
    return methods.stream()
        .map(m -> m.getName() + parametersToString(m.getParameterTypes()))
        .collect(Collectors.joining(ACTIONS_DELIMITER));
  }

  private String parametersToString(Class<?>[] params) {
    return "["
        + Arrays.stream(params).map(Class::getName).collect(Collectors.joining(PARAMS_DELIMITER))
        + "]";
  }

  private String statesWhereSurvivesToString(Set<EPAState> states) {
    return states.stream().map(EPAState::toString).collect(Collectors.joining(ACTIONS_DELIMITER));
  }
}
