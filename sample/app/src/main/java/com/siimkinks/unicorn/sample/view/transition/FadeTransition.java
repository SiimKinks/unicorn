package com.siimkinks.unicorn.sample.view.transition;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import com.siimkinks.unicorn.impl.AnimatorViewTransitionHandler;

public class FadeTransition extends AnimatorViewTransitionHandler {
  @NonNull
  @Override
  protected Animator createAnimator(@NonNull ViewGroup container,
                                    @NonNull View entering,
                                    boolean enteringViewAdded,
                                    @Nullable View leaving,
                                    boolean push) {
    final AnimatorSet animator = new AnimatorSet();
    if (enteringViewAdded) {
      animator.play(ObjectAnimator.ofFloat(entering, View.ALPHA, 0, 1));
    }

    if (leaving != null) {
      animator.play(ObjectAnimator.ofFloat(leaving, View.ALPHA, 0));
    }
    return animator;
  }

  @Override
  protected void resetView(@NonNull View view) {
    view.setAlpha(1);
  }
}
