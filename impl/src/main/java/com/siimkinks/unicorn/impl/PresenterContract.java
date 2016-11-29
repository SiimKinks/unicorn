package com.siimkinks.unicorn.impl;

import android.support.annotation.NonNull;

import com.siimkinks.unicorn.ContentViewContract;

/**
 * Base presenter contract.
 *
 * @param <ViewType> Associated view interface type
 */
public interface PresenterContract<ViewType extends ContentViewContract> {
  /**
   * @param view
   */
  void hookInto(@NonNull ViewType view);

  /**
   * Called when associated view gets destroyed, making this presenter inactive.
   */
  void destroy();
}
