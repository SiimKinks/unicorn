package com.siimkinks.unicorn;

import android.support.annotation.NonNull;

public final class Contracts {
  private Contracts() {
    throw new AssertionError("no instances");
  }

  public static void mustBeNull(Object o, @NonNull String message) {
    if (o != null) {
      throw new IllegalStateException(message);
    }
  }

  public static void mustBeTrue(boolean condition, @NonNull String message, Object... args) {
    if (!condition) {
      throw new IllegalStateException(String.format(message, args));
    }
  }

  public static void mustBeFalse(boolean condition, @NonNull String message, Object... args) {
    if (condition) {
      throw new IllegalStateException(String.format(message, args));
    }
  }

  public static void allMustBeBeNull(@NonNull String message, @NonNull Object... objects) {
    for (int i = 0, size = objects.length; i < size; i++) {
      if (objects[i] != null) {
        throw new IllegalStateException(message);
      }
    }
  }
}
