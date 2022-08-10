package metamorphicRelationsInference.distance;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;
import metamorphicRelationsInference.distance.util.ReflectionUtils;

public class Distance {

  public static final double DIFFERENT_CLASSES_WEIGHT = 20.0d;
  public static final double ARRAY_CELL_FACTOR = 5.0d;
  public static final double NULL_WEIGHT = 10.0d;

  private static final List<DistancePair> worklist = new ArrayList<>();
  private static final IdentityHashMap<Object, Integer> visited = new IdentityHashMap<>();

  public static final double NAN_WEIGHT = 20.0d;

  private Distance() {}

  public static double distance(Object o1, Object o2) {
    if (o1 == null && o2 == null) {
      return 0.0d;
    } else if (o1 == null ^ o2 == null) {
      return ObjectDistance.getNullDistance(o1, o2);
    }

    worklist.clear();
    visited.clear();

    return calculate(o1, o2);
  }

  private static double calculate(Object o1, Object o2) {
    double distance = 0.0d;

    worklist.add(new DistancePair(o1, o2));

    while (!worklist.isEmpty()) {
      DistancePair pair = worklist.remove(0);
      Object obj1 = pair.o1;
      Object obj2 = pair.o2;

      // ========================================CORNER
      // CASES========================================
      // ------------------NULL-------------------
      if (obj1 == null && obj2 == null) {
        continue;
      } else if (obj1 == null ^ obj2 == null) {
        distance += ObjectDistance.getNullDistance(obj1, obj2);
        continue;
      }

      // ------------DIFFERENT CLASSES------------
      else if (!obj1.getClass().equals(obj2.getClass())) {
        distance += DIFFERENT_CLASSES_WEIGHT;
        continue;
      }

      // ---------------CLASS-TYPE----------------
      if (obj1.getClass().equals(Class.class)) {
        continue;
      }

      // ----------------PRIMITIVE----------------
      // this definition of primitive contains also
      // primitive classes (e.g. Integer)
      else if (ReflectionUtils.isPrimitive(obj1)) {
        distance += PrimitiveDistance.distance(obj1, obj2);
        continue;
      }

      // ------------------STRING-----------------
      else if (ReflectionUtils.isString(obj1)) {
        distance += LevenshteinDistance.calculateDistance((String) obj1, (String) obj2);
        continue;
      }

      // -----------------ARRAYS------------------
      else if (ReflectionUtils.isArray(obj1)) {
        distance += handleArray(obj1, obj2);
        continue;
      }

      // ----------CIRCULAR DEPENDENCIES----------
      else if (visited.put(obj1, 1) != null && visited.put(obj2, 2) != null) {
        continue;
      }

      // ------------------OBJECT-----------------
      List<Field> fs1 = ReflectionUtils.getInheritedPrivateFields(obj1.getClass());
      List<Field> fs2 = ReflectionUtils.getInheritedPrivateFields(obj2.getClass());
      for (int i = 0; i < fs1.size(); i++) {
        Field f1 = fs1.get(i);
        Field f2 = fs2.get(i);

        f1.setAccessible(true);
        f2.setAccessible(true);

        // skip comparison of constants
        if (ReflectionUtils.isConstant(f1) && ReflectionUtils.isConstant(f2)) {
          continue;
        } else if (FieldFilter.exclude(f1) || FieldFilter.exclude(f2)) {
          continue;
        }

        ComparisonType type = getComparisonType(f1.getType());
        switch (type) {
          case PRIMITIVE:
            // this definition of primitives contains only real
            // primitive values (e.g int, char, ..) primitive
            // classes (e.g. Integer) are treated as object and
            // handled in the subsequent iteration as corner case
            distance += PrimitiveDistance.distance(f1, obj1, f2, obj2);
            break;
          case STRING:
            try {
              distance +=
                  LevenshteinDistance.calculateDistance(
                      (String) f1.get(obj1), (String) f2.get(obj2));
            } catch (IllegalAccessException e) {
              throw new RuntimeException(e);
            }
            break;
          case ARRAY:
            distance += handleArray(f1, obj1, f2, obj2);
            break;
          case OBJECT:
            // null values and corner cases are managed at the
            // beginning of the iteration
            Object obj1value;
            Object obj2value;
            try {
              obj1value = f1.get(obj1);
              obj2value = f2.get(obj2);
            } catch (IllegalAccessException e) {
              throw new RuntimeException(e);
            }
            worklist.add(new DistancePair(obj1value, obj2value));
            break;
          default:
            break;
        }
      }
    }

    return distance;
  }

  private static double handleArray(Object obj1, Object obj2) {
    double distance = 0.0d;

    ComparisonType arrayType = getComparisonType(obj1.getClass().getComponentType());
    switch (arrayType) {
      case OBJECT:
        try {
          Object[] castedF1 = (Object[]) obj1;
          Object[] castedF2 = (Object[]) obj2;
          int length = Math.min(Array.getLength(castedF1), Array.getLength(castedF2));
          for (int i = 0; i < length; i++) {
            worklist.add(new DistancePair(castedF1[i], castedF2[i]));
          }
          distance +=
              (Math.max(Array.getLength(castedF1), Array.getLength(castedF2)) - length)
                  * Distance.ARRAY_CELL_FACTOR;
        } catch (IllegalArgumentException e) {
          throw new RuntimeException(e);
        }
        break;
      case PRIMITIVE:
        distance += PrimitiveDistance.distance(obj1, obj2);
        break;
      case STRING:
        try {
          String[] castedF1 = (String[]) obj1;
          String[] castedF2 = (String[]) obj2;
          int length = Math.min(Array.getLength(castedF1), Array.getLength(castedF2));
          for (int i = 0; i < length; i++) {
            distance += LevenshteinDistance.calculateDistance(castedF1[i], castedF2[i]);
          }
          distance +=
              (Math.max(Array.getLength(castedF1), Array.getLength(castedF2)) - length)
                  * Distance.ARRAY_CELL_FACTOR;
        } catch (IllegalArgumentException e) {
          throw new RuntimeException(e);
        }
        break;
      case ARRAY:
        int length = Math.min(Array.getLength(obj1), Array.getLength(obj2));
        for (int i = 0; i < length; i++) {
          distance += handleArray(Array.get(obj1, i), Array.get(obj2, i));
        }
        distance +=
            (Math.max(Array.getLength(obj1), Array.getLength(obj2)) - length)
                * Distance.ARRAY_CELL_FACTOR;
        break;
      default:
        break;
    }

    return distance;
  }

  private static double handleArray(Field f1, Object obj1, Field f2, Object obj2) {
    double distance = 0.0d;

    try {
      if (f1.get(obj1) == null || f2.get(obj2) == null) {
        if (!(f1.get(obj1) == f2.get(obj2))) {
          distance += NULL_WEIGHT;
        }
        return distance;
      }
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }

    ComparisonType arrayType = getComparisonType(f1.getType().getComponentType());
    switch (arrayType) {
      case OBJECT:
        try {
          Object[] castedF1 = (Object[]) f1.get(obj1);
          Object[] castedF2 = (Object[]) f2.get(obj2);
          int length = Math.min(Array.getLength(castedF1), Array.getLength(castedF2));
          for (int i = 0; i < length; i++) {
            worklist.add(new DistancePair(castedF1[i], castedF2[i]));
          }
          distance +=
              (Math.max(Array.getLength(castedF1), Array.getLength(castedF2)) - length)
                  * Distance.ARRAY_CELL_FACTOR;
        } catch (IllegalArgumentException | IllegalAccessException e) {
          throw new RuntimeException(e);
        }
        break;
      case PRIMITIVE:
        try {
          Class<?> f1Type =
              f1.getType().getComponentType() == null
                  ? f1.getType()
                  : f1.getType().getComponentType();
          if (f1Type.equals(int.class)) {
            int[] castedF1 = (int[]) f1.get(obj1);
            int[] castedF2 = (int[]) f2.get(obj2);

            int length = Math.min(Array.getLength(castedF1), Array.getLength(castedF2));
            for (int i = 0; i < length; i++) {
              worklist.add(new DistancePair(castedF1[i], castedF2[i]));
            }
            distance +=
                (Math.max(Array.getLength(castedF1), Array.getLength(castedF2)) - length)
                    * Distance.ARRAY_CELL_FACTOR;
          } else if (f1Type.equals(char.class)) {
            char[] castedF1 = (char[]) f1.get(obj1);
            char[] castedF2 = (char[]) f2.get(obj2);

            int length = Math.min(Array.getLength(castedF1), Array.getLength(castedF2));
            for (int i = 0; i < length; i++) {
              worklist.add(new DistancePair(castedF1[i], castedF2[i]));
            }
            distance +=
                (Math.max(Array.getLength(castedF1), Array.getLength(castedF2)) - length)
                    * Distance.ARRAY_CELL_FACTOR;
          } else if (f1Type.equals(short.class)) {
            short[] castedF1 = (short[]) f1.get(obj1);
            short[] castedF2 = (short[]) f2.get(obj2);

            int length = Math.min(Array.getLength(castedF1), Array.getLength(castedF2));
            for (int i = 0; i < length; i++) {
              worklist.add(new DistancePair(castedF1[i], castedF2[i]));
            }
            distance +=
                (Math.max(Array.getLength(castedF1), Array.getLength(castedF2)) - length)
                    * Distance.ARRAY_CELL_FACTOR;
          } else if (f1Type.equals(long.class)) {
            long[] castedF1 = (long[]) f1.get(obj1);
            long[] castedF2 = (long[]) f2.get(obj2);

            int length = Math.min(Array.getLength(castedF1), Array.getLength(castedF2));
            for (int i = 0; i < length; i++) {
              worklist.add(new DistancePair(castedF1[i], castedF2[i]));
            }
            distance +=
                (Math.max(Array.getLength(castedF1), Array.getLength(castedF2)) - length)
                    * Distance.ARRAY_CELL_FACTOR;
          } else if (f1Type.equals(float.class)) {
            float[] castedF1 = (float[]) f1.get(obj1);
            float[] castedF2 = (float[]) f2.get(obj2);

            int length = Math.min(Array.getLength(castedF1), Array.getLength(castedF2));
            for (int i = 0; i < length; i++) {
              worklist.add(new DistancePair(castedF1[i], castedF2[i]));
            }
            distance +=
                (Math.max(Array.getLength(castedF1), Array.getLength(castedF2)) - length)
                    * Distance.ARRAY_CELL_FACTOR;
          } else if (f1Type.equals(double.class)) {
            double[] castedF1 = (double[]) f1.get(obj1);
            double[] castedF2 = (double[]) f2.get(obj2);

            int length = Math.min(Array.getLength(castedF1), Array.getLength(castedF2));
            for (int i = 0; i < length; i++) {
              worklist.add(new DistancePair(castedF1[i], castedF2[i]));
            }
            distance +=
                (Math.max(Array.getLength(castedF1), Array.getLength(castedF2)) - length)
                    * Distance.ARRAY_CELL_FACTOR;
          } else if (f1Type.equals(boolean.class)) {
            boolean[] castedF1 = (boolean[]) f1.get(obj1);
            boolean[] castedF2 = (boolean[]) f2.get(obj2);

            int length = Math.min(Array.getLength(castedF1), Array.getLength(castedF2));
            for (int i = 0; i < length; i++) {
              worklist.add(new DistancePair(castedF1[i], castedF2[i]));
            }
            distance +=
                (Math.max(Array.getLength(castedF1), Array.getLength(castedF2)) - length)
                    * Distance.ARRAY_CELL_FACTOR;
          } else if (f1Type.equals(byte.class)) {
            byte[] castedF1 = (byte[]) f1.get(obj1);
            byte[] castedF2 = (byte[]) f2.get(obj2);

            int length = Math.min(Array.getLength(castedF1), Array.getLength(castedF2));
            for (int i = 0; i < length; i++) {
              worklist.add(new DistancePair(castedF1[i], castedF2[i]));
            }
            distance +=
                (Math.max(Array.getLength(castedF1), Array.getLength(castedF2)) - length)
                    * Distance.ARRAY_CELL_FACTOR;
          }
        } catch (IllegalArgumentException | IllegalAccessException e) {
          throw new RuntimeException(e);
        }
        // distance += PrimitiveDistance.distance(f1, obj1, f2, obj2);
        break;
      case STRING:
        try {
          String[] castedF1 = (String[]) f1.get(obj1);
          String[] castedF2 = (String[]) f2.get(obj2);
          int length = Math.min(Array.getLength(castedF1), Array.getLength(castedF2));
          for (int i = 0; i < length; i++) {
            distance += LevenshteinDistance.calculateDistance(castedF1[i], castedF2[i]);
          }
          distance +=
              (Math.max(Array.getLength(castedF1), Array.getLength(castedF2)) - length)
                  * Distance.ARRAY_CELL_FACTOR;
        } catch (IllegalArgumentException | IllegalAccessException e) {
          throw new RuntimeException(e);
        }
        break;
      case ARRAY:
        break;
      default:
        break;
    }

    return distance;
  }

  private static ComparisonType getComparisonType(Class<?> f1) {
    if (f1.isPrimitive()) {
      return ComparisonType.PRIMITIVE;
    } else if (f1.isArray()) {
      return ComparisonType.ARRAY;
    } else if (f1.equals(String.class)) {
      return ComparisonType.STRING;
    }
    return ComparisonType.OBJECT;
  }
}

enum ComparisonType {
  ARRAY,
  PRIMITIVE,
  STRING,
  OBJECT
}

class DistancePair {
  Object o1;
  Object o2;

  public DistancePair(Object o1, Object o2) {
    this.o1 = o1;
    this.o2 = o2;
  }
}
