package com.siimkinks.unicorn;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.UUID;

/**
 * Transition details for view transitions.
 */
public final class TransitionDetails {
  public static final TransitionDetails DEFAULT = builder().build();

  @NonNull
  final ViewTransition enter;
  @NonNull
  final ViewTransition leave;
  final String uuid = UUID.randomUUID().toString();

  TransitionDetails(@NonNull ViewTransition enter,
                    @NonNull ViewTransition leave) {
    this.enter = enter;
    this.leave = leave;
  }

  @NonNull
  @CheckResult
  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    @Nullable
    private ViewTransition enter;
    @Nullable
    private ViewTransition leave;

    Builder() {
    }

    /**
     * View enter transition.
     * <p>
     * Can be {@code null} -- then default leave transition will be used
     *
     * @return Enter transition
     */
    @NonNull
    public Builder enter(@NonNull ViewTransition enter) {
      this.enter = enter;
      return this;
    }

    /**
     * View leave transition.
     * <p>
     * Can be {@code null} -- then default leave transition will be used
     *
     * @return Leave transition
     */
    @NonNull
    public Builder leave(@Nullable ViewTransition leave) {
      this.leave = leave;
      return this;
    }

    /**
     * Build immutable transition details.
     *
     * @return Immutable transition details
     */
    @NonNull
    @CheckResult
    public TransitionDetails build() {
      ViewTransition enter = this.enter;
      if (enter == null) {
        enter = new SwapTransition();
      }
      ViewTransition leave = this.leave;
      if (leave == null) {
        leave = new SwapTransition();
      }
      return new TransitionDetails(enter, leave);
    }
  }
}