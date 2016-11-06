package com.siimkinks.unicorn;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.transition.Fade;
import android.transition.Transition;

import com.google.auto.value.AutoValue;

/**
 * Animation details for view transitions
 */
@AutoValue
public abstract class AnimationDetails {
    public static final AnimationDetails DEFAULT = builder()
            .enter(new Fade(Fade.IN))
            .leave(new Fade(Fade.OUT))
            .build();

    AnimationDetails() {
    }

    /**
     * View enter transition animation.
     *
     * @return Enter transition animation
     */
    public abstract Transition enter();

    /**
     * View leave transition animation.
     * Can be {@code null} -- then default leave transition will be used
     *
     * @return Leave transition animation
     */
    @Nullable
    public abstract Transition leave();

    @NonNull
    @CheckResult
    public static Builder builder() {
        return new AutoValue_AnimationDetails.Builder();
    }

    @AutoValue.Builder
    public static abstract class Builder {
        /**
         * View enter transition.
         *
         * @param enter
         *         Enter transition
         * @return Animation details builder
         */
        @NonNull
        public abstract Builder enter(@NonNull Transition enter);

        /**
         * View leave transition.
         *
         * @param leave
         *         Leave transition
         * @return Animation details builder
         */
        @NonNull
        public abstract Builder leave(@Nullable Transition leave);

        /**
         * Build immutable animation details.
         *
         * @return Immutable animation details
         */
        @NonNull
        @CheckResult
        public abstract AnimationDetails build();
    }
}