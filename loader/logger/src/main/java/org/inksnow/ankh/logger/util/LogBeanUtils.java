package org.inksnow.ankh.logger.util;

import lombok.experimental.UtilityClass;
import lombok.val;
import org.slf4j.Marker;
import org.slf4j.helpers.Util;

import java.util.*;

@UtilityClass
public class LogBeanUtils {
  public static String[] dumpAllMarkerAsString(Marker marker) {
    if (marker == null) {
      return new String[0];
    }
    val result = new LinkedHashSet<String>();
    val stack = new Stack<Marker>();
    stack.push(marker);
    while (!stack.isEmpty()) {
      Marker current = stack.pop();
      if (current.hasReferences() && result.add(current.getName())) {
        Iterator<Marker> currentIterator = current.iterator();
        while (currentIterator.hasNext()) {
          stack.push(currentIterator.next());
        }
      }
    }
    return result.toArray(new String[0]);
  }

  public static String[] formatArguments(Object[] args) {
    if (args == null || args.length == 0) {
      return new String[0];
    }
    val result = new String[args.length];
    for (int i = 0; i < args.length; i++) {
      val builder = new StringBuilder();
      deeplyAppendParameter(builder, args[i], new HashMap<>());
      result[i] = builder.toString();
    }
    return result;
  }

  // special treatment of array values was suggested by 'lizongbo'
  private static void deeplyAppendParameter(StringBuilder sbuf, Object o, Map<Object[], Object> seenMap) {
    if (o == null) {
      sbuf.append("null");
      return;
    }
    if (!o.getClass().isArray()) {
      safeObjectAppend(sbuf, o);
    } else {
      // check for primitive array types because they
      // unfortunately cannot be cast to Object[]
      if (o instanceof boolean[]) {
        booleanArrayAppend(sbuf, (boolean[]) o);
      } else if (o instanceof byte[]) {
        byteArrayAppend(sbuf, (byte[]) o);
      } else if (o instanceof char[]) {
        charArrayAppend(sbuf, (char[]) o);
      } else if (o instanceof short[]) {
        shortArrayAppend(sbuf, (short[]) o);
      } else if (o instanceof int[]) {
        intArrayAppend(sbuf, (int[]) o);
      } else if (o instanceof long[]) {
        longArrayAppend(sbuf, (long[]) o);
      } else if (o instanceof float[]) {
        floatArrayAppend(sbuf, (float[]) o);
      } else if (o instanceof double[]) {
        doubleArrayAppend(sbuf, (double[]) o);
      } else {
        objectArrayAppend(sbuf, (Object[]) o, seenMap);
      }
    }
  }

  private static void safeObjectAppend(StringBuilder sbuf, Object o) {
    try {
      String oAsString = o.toString();
      sbuf.append(oAsString);
    } catch (Throwable t) {
      Util.report("SLF4J: Failed toString() invocation on an object of type [" + o.getClass().getName() + "]", t);
      sbuf.append("[FAILED toString()]");
    }

  }

  private static void objectArrayAppend(StringBuilder sbuf, Object[] a, Map<Object[], Object> seenMap) {
    sbuf.append('[');
    if (!seenMap.containsKey(a)) {
      seenMap.put(a, null);
      final int len = a.length;
      for (int i = 0; i < len; i++) {
        deeplyAppendParameter(sbuf, a[i], seenMap);
        if (i != len - 1)
          sbuf.append(", ");
      }
      // allow repeats in siblings
      seenMap.remove(a);
    } else {
      sbuf.append("...");
    }
    sbuf.append(']');
  }

  private static void booleanArrayAppend(StringBuilder sbuf, boolean[] a) {
    sbuf.append('[');
    final int len = a.length;
    for (int i = 0; i < len; i++) {
      sbuf.append(a[i]);
      if (i != len - 1)
        sbuf.append(", ");
    }
    sbuf.append(']');
  }

  private static void byteArrayAppend(StringBuilder sbuf, byte[] a) {
    sbuf.append('[');
    final int len = a.length;
    for (int i = 0; i < len; i++) {
      sbuf.append(a[i]);
      if (i != len - 1)
        sbuf.append(", ");
    }
    sbuf.append(']');
  }

  private static void charArrayAppend(StringBuilder sbuf, char[] a) {
    sbuf.append('[');
    final int len = a.length;
    for (int i = 0; i < len; i++) {
      sbuf.append(a[i]);
      if (i != len - 1)
        sbuf.append(", ");
    }
    sbuf.append(']');
  }

  private static void shortArrayAppend(StringBuilder sbuf, short[] a) {
    sbuf.append('[');
    final int len = a.length;
    for (int i = 0; i < len; i++) {
      sbuf.append(a[i]);
      if (i != len - 1)
        sbuf.append(", ");
    }
    sbuf.append(']');
  }

  private static void intArrayAppend(StringBuilder sbuf, int[] a) {
    sbuf.append('[');
    final int len = a.length;
    for (int i = 0; i < len; i++) {
      sbuf.append(a[i]);
      if (i != len - 1)
        sbuf.append(", ");
    }
    sbuf.append(']');
  }

  private static void longArrayAppend(StringBuilder sbuf, long[] a) {
    sbuf.append('[');
    final int len = a.length;
    for (int i = 0; i < len; i++) {
      sbuf.append(a[i]);
      if (i != len - 1)
        sbuf.append(", ");
    }
    sbuf.append(']');
  }

  private static void floatArrayAppend(StringBuilder sbuf, float[] a) {
    sbuf.append('[');
    final int len = a.length;
    for (int i = 0; i < len; i++) {
      sbuf.append(a[i]);
      if (i != len - 1)
        sbuf.append(", ");
    }
    sbuf.append(']');
  }

  private static void doubleArrayAppend(StringBuilder sbuf, double[] a) {
    sbuf.append('[');
    final int len = a.length;
    for (int i = 0; i < len; i++) {
      sbuf.append(a[i]);
      if (i != len - 1)
        sbuf.append(", ");
    }
    sbuf.append(']');
  }
}
