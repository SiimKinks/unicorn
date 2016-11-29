package com.siimkinks.unicorn;

import android.support.annotation.CheckResult;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

import com.google.auto.value.AutoValue;

import static com.siimkinks.unicorn.TransitionDetails.DEFAULT;
import static com.siimkinks.unicorn.Contracts.mustBeTrue;

/**
 * Immutable navigation details defining all the details how one view should be
 * navigated to and stay in the view stack
 */
@AutoValue
public abstract class NavigationDetails {

  NavigationDetails() {
  }

  /**
   * @return Content view
   */
  public abstract ContentViewContract view();

  /**
   * @return Transition details
   */
  public abstract TransitionDetails transition();

  /**
   * @return Whether there should always be only one instance of view in stack at all times
   */
  public abstract boolean singleInstance();

  /**
   * @return Whether view stack should be cleared when navigating this view
   */
  public abstract boolean clearStack();

  abstract boolean needsRestart();

  @NonNull
  @CheckResult
  public static Builder navigateTo(@NonNull ContentViewContract view) {
    return new AutoValue_NavigationDetails.Builder()
        .view(view)
        .transition(DEFAULT)
        .singleInstance(false)
        .clearStack(false)
        .needsRestart(false);
  }

  @NonNull
  @CheckResult
  public final Builder copy() {
    return new AutoValue_NavigationDetails.Builder(this);
  }

  @NonNull
  @CheckResult
  public final Builder copy(@NonNull ContentViewContract view) {
    return new AutoValue_NavigationDetails.Builder(this)
        .view(view);
  }

  @NonNull
  @CheckResult
  final NavigationDetails markRestartNeeded() {
    if (needsRestart()) {
      return this;
    }
    return copy()
        .needsRestart(true)
        .build();
  }

  @NonNull
  @CheckResult
  final NavigationDetails restart() {
    mustBeTrue(needsRestart(), "This view does not need a restart [%s]", view());
    return copy()
        .view(view().copy())
        .needsRestart(false)
        .build();
  }

  @AutoValue.Builder
  public static abstract class Builder {
    /**
     * Define view to navigate to.
     *
     * @param view View instance to navigate to
     * @return Navigation details builder
     */
    @NonNull
    abstract Builder view(@NonNull ContentViewContract view);

    /**
     * Transition details that define how this view should be transitioned when entering
     * or leaving from it.
     *
     * @param details Animation details
     * @return Navigation details builder
     */
    @NonNull
    public abstract Builder transition(@NonNull TransitionDetails details);

    /**
     * Define whether there should always be only one instance of this
     * view in stack at all times.
     *
     * @param singleInstance {@code true} when there should be only one instance if this
     *                       view in stack at all times
     * @return Navigation details builder
     */
    @NonNull
    public abstract Builder singleInstance(boolean singleInstance);

    /**
     * Define whether view stack should be cleared when navigating to this view
     *
     * @param clearStack {@code true} when view stack should be cleared when
     *                   navigating to this view
     * @return Navigation details builder
     */
    @NonNull
    public abstract Builder clearStack(boolean clearStack);

    @NonNull
    abstract Builder needsRestart(boolean needsRestart);

    /**
     * Build immutable navigation details.
     *
     * @return immutable navigation details defining how view manager should handle
     * this view
     */
    @NonNull
    @CheckResult
    public abstract NavigationDetails build();

    /**
     * Navigate to this view.
     *
     * @param navigator navigator
     */
    @MainThread
    public final void go(@NonNull Navigator navigator) {
      navigator.navigateTo(build());
    }
  }

  /**
   * Navigate to this view.
   *
   * @param navigator navigator
   */
  @MainThread
  public final void go(@NonNull Navigator navigator) {
    navigator.navigateTo(this);
  }

  @Override
  public final boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof NavigationDetails) {
      NavigationDetails that = (NavigationDetails) o;
      return this.view().equals(that.view());
    }
    return false;
  }
}