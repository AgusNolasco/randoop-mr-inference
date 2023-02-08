package metamorphicRelationsInference.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ReflectionUtils {

  public static String getProcName(Object proc) {
    String procName;
    String procParams;
    if (proc instanceof Constructor<?>) {
      Constructor<?> constructor = (Constructor<?>) proc;
      procName = constructor.getDeclaringClass().getSimpleName();
      procParams =
          Arrays.stream(constructor.getParameterTypes())
              .map(Class::getSimpleName)
              .collect(Collectors.joining(","));
    } else if (proc instanceof Method) {
      Method method = (Method) proc;
      procName = method.getName();
      procParams =
          Arrays.stream(method.getParameterTypes())
              .map(Class::getSimpleName)
              .collect(Collectors.joining(","));
    } else {
      throw new IllegalArgumentException("The procedure must be a constructor or a method");
    }
    return procName + "(" + procParams + ")";
  }

  public static String getConstructorName(Constructor<?> constructor) {
    Class<?> clazz = constructor.getDeclaringClass();
    StringBuilder constructorName =
        new StringBuilder(constructor.getDeclaringClass().getSimpleName());
    long countOfSameNamedConstructors = Arrays.stream(clazz.getConstructors()).count();
    if (countOfSameNamedConstructors > 1) {
      for (Class<?> param : constructor.getParameterTypes()) {
        if (param.isArray()) {
          String paramName = param.getSimpleName().replaceAll("\\[]", "");
          constructorName.append("ArrayOf").append(paramName);
        } else {
          constructorName.append(param.getSimpleName());
        }
      }
    }

    return constructorName.toString();
  }

  public static String getMethodName(Method method) {
    Class<?> clazz = method.getDeclaringClass();
    StringBuilder methodName = new StringBuilder(method.getName());
    String finalMethodName = methodName.toString();
    long countOfSameNamedMethods =
        Arrays.stream(clazz.getDeclaredMethods())
            .filter(m -> m.getName().equals(finalMethodName))
            .count();
    if (countOfSameNamedMethods > 1) {
      for (Class<?> param : method.getParameterTypes()) {
        if (param.isArray()) {
          String paramName = param.getSimpleName().replaceAll("\\[]", "");
          methodName.append("ArrayOf").append(paramName);
        } else {
          methodName.append(param.getSimpleName());
        }
      }
    }
    return methodName.toString();
  }
}
