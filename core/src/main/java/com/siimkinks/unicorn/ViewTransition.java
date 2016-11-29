package com.siimkinks.unicorn;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

public abstract class ViewTransition {

  protected abstract void transition(@NonNull ViewGroup container,
                                     @NonNull View entering,
                                     @Nullable View leaving,
                                     boolean push,
                                     @NonNull Runnable transitionCompleted);

  /**
   * Called when transition needs to be cancelled.
   * <p>
   * This happens if view associated with this transition
   * gets popped from stack before this transition has completed.
   */
  protected void cancel() {
  }

  /**
   * Called when transition needs to be completed immediately,
   * without any transition or transition.
   * <p>
   * This happens if any view is pushed to stack before this
   * transition has completed.
   */
  protected void completeImmediately() {
  }
}
