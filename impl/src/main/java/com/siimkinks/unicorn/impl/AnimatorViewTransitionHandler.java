package com.siimkinks.unicorn.impl;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.siimkinks.unicorn.ViewTransition;

public abstract class AnimatorViewTransitionHandler extends ViewTransition {
  @Nullable
  private Animator animator;
  private volatile boolean canceled;
  private volatile boolean completed;
  private boolean removeViewOnPush = true;

  @NonNull
  protected abstract Animator createAnimator(@NonNull ViewGroup container,
                                             @NonNull View entering,
                                             boolean enteringViewAdded,
                                             @Nullable View leaving,
                                             boolean push);

  protected abstract void resetView(@NonNull View view);

  @Override
  protected void transition(@NonNull final ViewGroup container,
                            @NonNull final View entering,
                            @Nullable final View leaving,
                            final boolean push,
                            @NonNull final Runnable transitionCompleted) {
    boolean readyToAnimate = true;
    final boolean enteringViewAdded = entering.getParent() == null;
    if (enteringViewAdded) {
      if (push || leaving == null) {
        container.addView(entering);
      } else {
        container.addView(entering, container.indexOfChild(leaving));
      }

      if (entering.getMeasuredWidth() <= 0 && entering.getMeasuredHeight() <= 0) {
        readyToAnimate = false;
        entering.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
          @Override
          public boolean onPreDraw() {
            entering.getViewTreeObserver().removeOnPreDrawListener(this);
            animate(container, entering, enteringViewAdded, leaving, push, transitionCompleted);
            return true;
          }
        });
      }
    } else {
      resetView(entering);
    }

    if (readyToAnimate) {
      animate(container, entering, enteringViewAdded, leaving, push, transitionCompleted);
    }
  }

  void animate(@NonNull final ViewGroup container,
               @NonNull final View entering,
               boolean enteringViewAdded,
               @Nullable final View leaving,
               final boolean push,
               @NonNull final Runnable transitionCompleted) {
    if (canceled) {
      return;
    }

    final Animator animator = createAnimator(container, entering, enteringViewAdded, leaving, push);
    animator.addListener(new AnimatorListenerAdapter() {
      @Override
      public void onAnimationCancel(Animator animation) {
        super.onAnimationCancel(animation);
        if (leaving != null && (!push || removeViewOnPush)) {
          container.removeView(leaving);
        }

        complete(transitionCompleted, this);
      }

      @Override
      public void onAnimationEnd(Animator animation) {
        super.onAnimationEnd(animation);
        if (canceled || AnimatorViewTransitionHandler.this.animator == null) {
          return;
        }
        if (leaving != null && (!push || removeViewOnPush)) {
          container.removeView(leaving);
        }

        complete(transitionCompleted, this);

        if (push && leaving != null) {
          resetView(leaving);
        }
      }
    });
    this.animator = animator;
    animator.start();
  }

  void complete(@NonNull Runnable transitionCompleted,
                @NonNull AnimatorListenerAdapter animatorListener) {
    if (!completed) {
      completed = true;
      transitionCompleted.run();
    }

    final Animator animator = this.animator;
    this.animator = null;
    if (animator != null) {
      animator.removeListener(animatorListener);
    }
  }

  @Override
  protected void cancel() {
    canceled = true;
    super.cancel();

    final Animator animator = this.animator;
    if (animator != null) {
      animator.cancel();
    }
  }

  @Override
  protected void completeImmediately() {
    super.completeImmediately();

    final Animator animator = this.animator;
    if (animator != null) {
      animator.end();
    }
  }

  protected void setRemoveViewOnPush(boolean removeViewOnPush) {
    this.removeViewOnPush = removeViewOnPush;
  }
}
