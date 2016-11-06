package com.siimkinks.unicorn;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.transition.Transition;
import android.transition.TransitionSet;

final class ViewTransition extends TransitionSet {
    private ViewTransition(@NonNull Transition enter, @Nullable Transition leave) {
        setOrdering(ORDERING_TOGETHER);
        addTransition(leave)
                .addTransition(enter);
    }

    @NonNull
    @CheckResult
    static TransitionSet from(@NonNull NavigationDetails entering, @Nullable NavigationDetails leaving) {
        return new ViewTransition(entering.animation().enter(),
                leaving != null ? leaving.animation().leave() : AnimationDetails.DEFAULT.leave());
    }
}
