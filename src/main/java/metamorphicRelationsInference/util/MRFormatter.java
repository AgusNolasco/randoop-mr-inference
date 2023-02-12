package metamorphicRelationsInference.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import metamorphicRelationsInference.epa.EPAState;
import metamorphicRelationsInference.metamorphicRelation.MetamorphicRelation;

public class MRFormatter {

  private final String ACTIONS_DELIMITER = "'";
  private final String STATES_DELIMITER = ";";
  private final String PARAMS_DELIMITER = ":";
  private final String COMPONENTS_DELIMITER = ",";

  public String formatMR(MetamorphicRelation mr) {
    List<String> components = new ArrayList<>();
    components.add(constructorParamsToString(mr.getLeftConstructor()));
    components.add(methodsToString(mr.getLeftMethods()));
    components.add(constructorParamsToString(mr.getRightConstructor()));
    components.add(methodsToString(mr.getRightMethods()));
    components.add(statesWhereSurvivesToString(mr.getStatesWhereSurvives()));
    return String.join(COMPONENTS_DELIMITER, components);
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
    return states.stream().map(EPAState::toString).collect(Collectors.joining(STATES_DELIMITER));
  }
}
