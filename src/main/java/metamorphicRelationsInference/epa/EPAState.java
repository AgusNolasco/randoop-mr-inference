package metamorphicRelationsInference.epa;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class EPAState {

  private final String name;
  private final boolean isInitial;
  private final Map<Method, Boolean> enabledMethods;

  public EPAState(String name, Map<Method, Boolean> enabledMethods) {
    Objects.requireNonNull(name, "State name cannot be null");
    this.name = name;
    isInitial = enabledMethods.values().stream().noneMatch(v -> v.equals(true));
    this.enabledMethods = enabledMethods;
  }

  public EPAState(String name) {
    this.name = name;
    this.isInitial = false;
    enabledMethods = null;
  }

  public boolean isInitial() {
    return isInitial;
  }

  public String getName() {
    return name;
  }

  public Map<Method, Boolean> getEnabledMethods() {
    return new HashMap<>(enabledMethods);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + name.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof EPAState)) {
      return false;
    }
    EPAState other = (EPAState) o;
    return name.equals(other.name);
  }

  @Override
  public String toString() {
    return name;
  }
}
