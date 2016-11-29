package com.siimkinks.unicorn.sample.view.transition;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.transition.ArcMotion;
import android.transition.ChangeBounds;
import android.transition.ChangeClipBounds;
import android.transition.ChangeTransform;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionSet;
import android.view.View;
import android.view.ViewGroup;

import com.siimkinks.unicorn.impl.AndroidViewTransitionHandler;

import static android.transition.TransitionSet.ORDERING_TOGETHER;

public class ArcMoveTransitionHandler extends AndroidViewTransitionHandler {
  public static final ArcMoveTransitionHandler INSTANCE = new ArcMoveTransitionHandler();

  private ArcMoveTransitionHandler() {
  }

  @NonNull
  @Override
  protected Transition createTransition(@NonNull ViewGroup container, @NonNull View entering, @Nullable View leaving, boolean push) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
      return new TransitionSet()
          .setOrdering(ORDERING_TOGETHER)
          .addTransition(new Fade(Fade.OUT))
          .addTransition(new Fade(Fade.IN));
    }
    final TransitionSet transition = new TransitionSet()
        .addTransition(new ChangeBounds())
        .addTransition(new ChangeClipBounds())
        .addTransition(new ChangeTransform());

    transition.setPathMotion(new ArcMotion());

    return transition;
  }
}
