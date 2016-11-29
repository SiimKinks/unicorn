package com.siimkinks.unicorn.impl;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;

import com.siimkinks.unicorn.ViewTransition;

public abstract class AndroidViewTransitionHandler extends ViewTransition {
  @NonNull
  protected abstract Transition createTransition(@NonNull ViewGroup container,
                                                 @NonNull View entering,
                                                 @Nullable View leaving,
                                                 boolean push);

  @Override
  protected void transition(@NonNull ViewGroup container,
                            @NonNull View entering,
                            @Nullable View leaving,
                            boolean push,
                            @NonNull final Runnable transitionCompleted) {

    final Transition transition = createTransition(container, entering, leaving, push);
    transition.addListener(new Transition.TransitionListener() {
      @Override
      public void onTransitionStart(Transition transition) {}

      @Override
      public void onTransitionEnd(Transition transition) {
        transitionCompleted.run();
      }

      @Override
      public void onTransitionCancel(Transition transition) {
        transitionCompleted.run();
      }

      @Override
      public void onTransitionPause(Transition transition) {}

      @Override
      public void onTransitionResume(Transition transition) {}
    });

    TransitionManager.beginDelayedTransition(container, transition);
    if (leaving != null) {
      container.removeView(leaving);
    }
    if (entering.getParent() == null) {
      container.addView(entering);
    }
  }
}
