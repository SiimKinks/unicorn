package com.siimkinks.unicorn;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

/**
 * A {@link ViewTransition} that will instantly swap
 * Views with no animations or transitions.
 */
public class SwapTransition extends ViewTransition {
  private boolean removeViewOnPush = true;

  @Override
  protected void transition(@NonNull ViewGroup container,
                            @NonNull View entering,
                            @Nullable View leaving,
                            boolean push,
                            @NonNull final Runnable transitionCompleted) {
    if (leaving != null && (!push || removeViewOnPush)) {
      container.removeView(leaving);
    }
    if (entering.getParent() == null) {
      container.addView(entering);
    }
    if (container.getWindowToken() != null) {
      transitionCompleted.run();
    } else {
      container.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
        @Override
        public void onViewAttachedToWindow(View view) {
          view.removeOnAttachStateChangeListener(this);
          transitionCompleted.run();
        }

        @Override
        public void onViewDetachedFromWindow(View view) {
        }
      });
    }
  }

  protected void setRemoveViewOnPush(boolean removeViewOnPush) {
    this.removeViewOnPush = removeViewOnPush;
  }
}
