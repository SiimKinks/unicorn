package com.siimkinks.unicorn;

import android.util.Log;

public final class LogUtil {
  private static final String TAG_COMPANY_PREFIX = "UNICORN - ";
  private static final String TAG_VIEW = TAG_COMPANY_PREFIX + "VIEW: ";

  public static void logViewDebug(String TAG, String message, Object... args) {
    logDebug(TAG_VIEW, TAG, message, args);
  }

  public static void logViewInfo(String TAG, String message, Object... args) {
    logInfo(TAG_VIEW, TAG, message, args);
  }

  public static void logViewWarning(String TAG, String message, Object... args) {
    logWarning(TAG_VIEW, TAG, message, args);
  }

  public static void logViewError(String TAG, String message, Object... args) {
    logError(TAG_VIEW, TAG, message, args);
  }

  public static void logViewError(String TAG, Throwable e, String message, Object... args) {
    logError(TAG_VIEW, TAG, e, message, args);
  }

  private static void logInfo(String debugTag, String TAG, String message, Object... args) {
    if (args.length > 0) message = String.format(message, args);
    Log.i(TAG, debugTag + message);
  }

  private static void logDebug(String debugTag, String TAG, String message, Object... args) {
    if (args.length > 0) message = String.format(message, args);
    Log.d(TAG, debugTag + message);
  }

  private static void logWarning(String debugTag, String TAG, String message, Object... args) {
    if (args.length > 0) message = String.format(message, args);
    Log.w(TAG, debugTag + message);
  }

  private static void logError(String debugTag, String TAG, String message, Object... args) {
    if (args.length > 0) message = String.format(message, args);
    Log.e(TAG, debugTag + message);
  }

  private static void logError(String debugTag, String TAG, Throwable e, String message, Object... args) {
    if (args.length > 0) message = String.format(message, args);
    Log.e(TAG, debugTag + message, e);
  }
}
