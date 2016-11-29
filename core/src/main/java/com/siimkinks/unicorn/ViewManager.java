package com.siimkinks.unicorn;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.CheckResult;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.siimkinks.unicorn.ContentViewContract.LifecycleEvent;

import java.util.ArrayDeque;
import java.util.Iterator;

import rx.Observer;

import static com.siimkinks.unicorn.ContentViewContract.BackPressResult.NAVIGATE_BACK;
import static com.siimkinks.unicorn.ContentViewContract.BackPressResult.STAY_ON_VIEW;
import static com.siimkinks.unicorn.ContentViewContract.LifecycleEvent.DESTROY;
import static com.siimkinks.unicorn.ContentViewContract.LifecycleEvent.START;
import static com.siimkinks.unicorn.ContentViewContract.LifecycleEvent.STOP;
import static com.siimkinks.unicorn.ContentViewContract.LifecycleEvent.UNKNOWN;
import static com.siimkinks.unicorn.Contracts.allMustBeBeNull;
import static com.siimkinks.unicorn.Contracts.mustBeFalse;
import static java.util.Objects.requireNonNull;

/**
 * View stack manager.
 *
 * @param <GraphProvider> Application dependency injection provider
 */
public class ViewManager<GraphProvider extends DependencyGraphProvider> implements Observer<LifecycleEvent> {
  private static final String TAG = ViewManager.class.getSimpleName();

  @VisibleForTesting
  ArrayDeque<NavigationDetails> viewStack = new ArrayDeque<>(5);
  private ArrayMap<String, ViewTransition> inProgressTransitions = new ArrayMap<>(5);

  private final GraphProvider graphProvider;

  @Nullable
  private RootActivityContract activityContract;
  @Nullable
  private Activity foregroundActivity;
  private LayoutInflater inflater;
  private ViewGroup contentRootView;
  private LifecycleEvent latestParentLifecycleEvent = UNKNOWN;

  public ViewManager(@NonNull GraphProvider graphProvider) {
    this.graphProvider = graphProvider;
  }

  /**
   * Call when root activity is available.
   * <p>
   * A good place for that is {@code {@link Activity#onCreate(Bundle)}} for example.
   *
   * @param activityContract that one root activity
   */
  @MainThread
  public final void registerActivity(@NonNull RootActivityContract activityContract,
                                     @NonNull Activity rootActivity) {
    allMustBeBeNull("Trying to register new foreground activity, but old resources are not cleaned",
        foregroundActivity, this.activityContract, contentRootView, inflater);
    this.activityContract = activityContract;
    this.foregroundActivity = rootActivity;
    this.inflater = foregroundActivity.getLayoutInflater();
    this.contentRootView = activityContract.getContentRootView();
    activityContract.hookIntoLifecycle(this);
    if (viewStack.isEmpty()) {
      renderFirstView(activityContract.getFirstView());
    } else {
      reRenderTopView();
    }
  }

  /**
   * Call when root activity is not available anymore.
   * <p>
   * A good place for that is {@code {@link Activity#onDestroy()}} for example.
   */
  @MainThread
  public final void unregisterActivity() {
    activityContract = null;
    foregroundActivity = null;
    contentRootView = null;
    inflater = null;
    final ArrayDeque<NavigationDetails> newStack = new ArrayDeque<>(viewStack.size());
    final Iterator<NavigationDetails> iterator = viewStack.descendingIterator();
    while (iterator.hasNext()) {
      final NavigationDetails next = iterator.next();
      final ContentViewContract view = next.view();
      view.onDestroy();
      view.setRootView(null);
      newStack.push(next.markRestartNeeded());
    }
    viewStack = newStack;
  }

  /**
   * Receive parent activity lifecycle events.
   *
   * @param lifecycleEvent Parent lifecycle event
   */
  @Override
  public void onNext(LifecycleEvent lifecycleEvent) {
    switch (lifecycleEvent) {
      case STOP:
        stopTopView();
        break;
      case START:
        // we only start top view if parents' last lifecycle was STOP -- otherwise
        // we violate view contract
        if (latestParentLifecycleEvent == STOP) {
          startTopView();
        }
        break;
      default:
        break;
    }
    latestParentLifecycleEvent = lifecycleEvent;
  }

  // Parent activity lifecycle onComplete
  @Override
  public void onCompleted() {
  }

  // Parent activity lifecycle onError
  @Override
  public void onError(Throwable e) {
  }

  @MainThread
  void navigate(@NonNull NavigationDetails navDetails) {
    boolean newInstance = true;
    if (navDetails.singleInstance() && !viewStack.isEmpty()) {
      if (viewStack.peek().equals(navDetails)) {
        return;
      }
      final Dual<NavigationDetails, Boolean> removedViewMetadata = removeViewFromStack(navDetails.view());
      if (removedViewMetadata != null) {
        // bring view to top, but with new nav details
        final NavigationDetails removedViewDetails = removedViewMetadata.first();
        if (removedViewDetails.needsRestart()) {
          // if view needs restart then it's a new instance
          // also, prev view needs restart -- therefore view.copy()
          navDetails = navDetails.copy()
              .view(removedViewDetails.view().copy())
              .build();
        } else {
          navDetails = navDetails.copy()
              .view(removedViewDetails.view())
              .build();
          newInstance = false;
        }
      }
    }
    if (navDetails.clearStack()) {
      clearStack();
    }

    // there might not be root view currently -- activity dead or smth like that
    if (contentRootView != null && isActivityShown()) {
      pushViewToStack(navDetails, newInstance);
    } else {
      // we wait for root view to come back -- until then just mark this view as needing restart
      LogUtil.logViewDebug(TAG, "No root view while navigating to view %s. Adding it to stack and waiting for root view to come back",
          navDetails.view().getClass().getSimpleName());
      viewStack.push(navDetails.markRestartNeeded());
    }
  }

  @MainThread
  private void renderFirstView(@NonNull NavigationDetails firstViewNavDetails) {
    viewStack.push(firstViewNavDetails);
    final ContentViewContract firstView = firstViewNavDetails.view();
    inflater.inflate(firstView.getViewResId(), contentRootView);
    firstView.setRootView(getContentRootViewFirstChild());
    callViewStartMethods(firstView, true);
  }

  @MainThread
  private void reRenderTopView() {
    final NavigationDetails first = viewStack.pollFirst();
    navigate(first.restart());
  }

  @MainThread
  private void pushViewToStack(@NonNull NavigationDetails newViewNavigationDetails, boolean newInstance) {
    final ContentViewContract view = newViewNavigationDetails.view();
    final NavigationDetails prevView = stopTopView();
    viewStack.push(newViewNavigationDetails);

    transitionBetween(newViewNavigationDetails, prevView, true);

    callViewStartMethods(view, newInstance);
  }

  @MainThread
  private void callViewStartMethods(@NonNull ContentViewContract view, boolean newInstance) {
    if (newInstance) {
      //noinspection unchecked
      view.onCreate(graphProvider);
    }
    if (view.latestLifecycleEvent() != DESTROY) { // check if view called finish in onCreate
      view.onStart();
    }
  }

  @Nullable
  private NavigationDetails stopTopView() {
    NavigationDetails topView = null;
    if (!viewStack.isEmpty()) {
      topView = viewStack.peek();
      // prev view waits for restart (aka destroyed), so contract
      // permits us from calling any lifecycle methods on it
      if (!topView.needsRestart()) {
        topView.view().onStop();
      }
    }
    return topView;
  }

  /**
   * This method is intended to be called only when activity resumes itself!
   */
  private void startTopView() {
    if (!viewStack.isEmpty()) {
      final NavigationDetails topView = viewStack.peek();

      mustBeFalse(topView.needsRestart(), "Tried to start top view that needs restart");

      topView.view().onStart();
    }
  }

  /**
   * Call from the {@link Activity#onBackPressed()}
   *
   * @return {@code true} when back press was consumed. {@code false} when system method
   * should be called
   */
  @MainThread
  public final boolean handleBackPress() {
    final NavigationDetails top = viewStack.peek();
    final ContentViewContract topView = top.view();
    final ContentViewContract.BackPressResult backPressResult = topView.onBackPressed();
    if (backPressResult == STAY_ON_VIEW) {
      return true;
    }
    if (backPressResult == NAVIGATE_BACK && canNavigateBack()) {
      finish(topView);
      return true;
    }
    return false;
  }

  @MainThread
  void finish(@NonNull ContentViewContract view) {
    final Dual<NavigationDetails, Boolean> removedViewMetadata = removeViewFromStack(view);
    if (removedViewMetadata == null) {
      LogUtil.logViewWarning(TAG, "Tried to remove non-existing view %s; ignore and carry on", view);
      return;
    }
    final Boolean top = removedViewMetadata.second();
    final NavigationDetails removed = removedViewMetadata.first();
    final ContentViewContract removedView = removed.view();
    final LifecycleEvent latestLifecycleEvent = removedView.latestLifecycleEvent();
    // if we are top we enter normal finish flow
    // if last lifecycle event was STOP then view called finish unexpectedly in onStop and we enter unconventional flow
    if (top && latestLifecycleEvent != STOP) {
      removedView.onStop();
    }
    removedView.onDestroy();
    if (top && latestLifecycleEvent != STOP) {
      final NavigationDetails newTop = viewStack.peek();
      if (newTop != null) {
        if (newTop.needsRestart()) {
          final NavigationDetails restartedNavDetails = viewStack.pollFirst().restart();
          viewStack.push(restartedNavDetails);
          transitionBetween(restartedNavDetails, removed, false);
          callViewStartMethods(restartedNavDetails.view(), true);
        } else {
          transitionBetween(newTop, removed, false);
          final ContentViewContract newTopView = newTop.view();
          // if onDestroy new view is opened then start for this newly added view is already called
          if (newTopView.latestLifecycleEvent() != START) {
            newTopView.onStart();
          }
        }
      }
    }
    removedView.setRootView(null);
  }

  @MainThread
  private void clearStack() {
    final Iterator<NavigationDetails> iterator = viewStack.iterator();
    while (iterator.hasNext()) {
      final NavigationDetails next = iterator.next();
      if (!next.needsRestart()) { // this view is already destroyed
        final ContentViewContract view = next.view();
        view.onDestroy();
        view.setRootView(null);
      }
      iterator.remove();
    }
  }

  @SuppressWarnings("deprecation")
  @VisibleForTesting
  @MainThread
  void transitionBetween(@NonNull NavigationDetails entering,
                         @Nullable NavigationDetails leaving,
                         boolean push) {
    final ArrayMap<String, ViewTransition> inProgressTransitions = this.inProgressTransitions;
    final TransitionDetails enteringTransition = entering.transition();
    final ViewTransition transition;
    if (push) {
      transition = enteringTransition.enter;
      inProgressTransitions.put(enteringTransition.uuid, transition);

      if (leaving != null) {
        final ViewTransition inProgressTransition = inProgressTransitions.remove(leaving.transition().uuid);
        if (inProgressTransition != null) {
          inProgressTransition.completeImmediately();
        }
      }
    } else {
      requireNonNull(leaving, "Leaving view cannot be null when poping views");
      final TransitionDetails leavingTransition = leaving.transition();
      transition = leavingTransition.leave;

      final ViewTransition inProgressTransition = inProgressTransitions.remove(leavingTransition.uuid);
      if (inProgressTransition != null) {
        inProgressTransition.cancel();
      }
    }

    final ContentViewContract enteringViewContract = entering.view();
    ViewGroup enteringRootView = enteringViewContract.getRootView();

    if (enteringRootView == null) {
      final int contentViewResId = enteringViewContract.getViewResId();
      enteringRootView = (ViewGroup) inflater.inflate(contentViewResId, contentRootView, false);
      enteringViewContract.setRootView(enteringRootView);
    }


    final ViewGroup leavingRootView = leaving == null ? null : leaving.view().getRootView();
    transition.transition(contentRootView, enteringRootView, leavingRootView, push, new Runnable() {
      @Override
      public void run() {
        inProgressTransitions.remove(enteringTransition.uuid);
      }
    });
  }

  @VisibleForTesting
  @Nullable
  @MainThread
  Dual<NavigationDetails, Boolean> removeViewFromStack(@NonNull ContentViewContract removableView) {
    final Iterator<NavigationDetails> iterator = viewStack.iterator();
    boolean top = true;
    while (iterator.hasNext()) {
      final NavigationDetails next = iterator.next();
      final ContentViewContract nextViewContract = next.view();
      final boolean equal = removableView.equals(nextViewContract);
      if (top) {
        if (equal) {
          iterator.remove();
          return Dual.create(next, Boolean.TRUE);
        }
        top = false;
      } else if (equal) {
        iterator.remove();
        return Dual.create(next, Boolean.FALSE);
      }
    }
    return null;
  }

  private boolean canNavigateBack() {
    return viewStack.size() > 1;
  }

  @NonNull
  private View getContentRootViewFirstChild() {
    return contentRootView.getChildAt(0);
  }

  @CheckResult
  private boolean isActivityShown() {
    if (activityContract == null) {
      return false;
    }
    return activityContract.latestLifecycleEvent().isVisible();
  }
}

