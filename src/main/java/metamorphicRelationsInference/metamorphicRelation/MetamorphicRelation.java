package metamorphicRelationsInference.metamorphicRelation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class MetamorphicRelation {

  private Constructor<?> leftConstructor;
  private List<Method> leftMethods;
  private Constructor<?> rightConstructor;
  private List<Method> rightMethods;
  private Set<String> statesWhereSurvives;

  public MetamorphicRelation(
      Constructor<?> leftConstructor,
      List<Method> leftMethods,
      Constructor<?> rightConstructor,
      List<Method> rightMethods,
      Set<String> statesWhereSurvives) {
    Objects.requireNonNull(leftMethods);
    Objects.requireNonNull(rightMethods);
    Objects.requireNonNull(statesWhereSurvives);
    if (statesWhereSurvives.isEmpty()) {
      throw new IllegalArgumentException("The states where the mr survives must be not empty");
    }

    this.leftConstructor = leftConstructor;
    this.leftMethods = leftMethods;
    this.rightConstructor = rightConstructor;
    this.rightMethods = rightMethods;
    this.statesWhereSurvives = statesWhereSurvives;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof MetamorphicRelation)) return false;

    MetamorphicRelation that = (MetamorphicRelation) o;

    if (!Objects.equals(leftConstructor, that.leftConstructor)) return false;
    if (!leftMethods.equals(that.leftMethods)) return false;
    if (!Objects.equals(rightConstructor, that.rightConstructor)) return false;
    if (!rightMethods.equals(that.rightMethods)) return false;
    return statesWhereSurvives.equals(that.statesWhereSurvives);
  }

  @Override
  public int hashCode() {
    int result = leftConstructor != null ? leftConstructor.hashCode() : 0;
    result = 31 * result + leftMethods.hashCode();
    result = 31 * result + (rightConstructor != null ? rightConstructor.hashCode() : 0);
    result = 31 * result + rightMethods.hashCode();
    result = 31 * result + statesWhereSurvives.hashCode();
    return result;
  }

  @Override
  public String toString() {
    StringBuilder str = new StringBuilder();
    if (leftConstructor != null) {
      str.append(leftConstructor.getName()).append("\n");
    }
    for (Method m : leftMethods) {
      str.append(m.getName()).append("\n");
    }
    str.append("=\n");
    if (rightConstructor != null) {
      str.append(rightConstructor.getName()).append("\n");
    }
    for (Method m : rightMethods) {
      str.append(m.getName()).append("\n");
    }
    str.append(statesWhereSurvives);
    return str.toString();
  }
}
