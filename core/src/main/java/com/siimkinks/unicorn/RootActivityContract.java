package com.siimkinks.unicorn;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.view.ViewGroup;

import com.siimkinks.unicorn.ContentViewContract.LifecycleEvent;

import rx.Observer;
import rx.Subscription;

/**
 * That one root activity.
 */
public interface RootActivityContract {
  /**
   * @return Root view
   */
  @NonNull
  ViewGroup getContentRootView();

  /**
   * @return First view to display when application is started
   */
  @NonNull
  NavigationDetails getFirstView();

  /**
   * @return latest lifecycle event called by the system
   */
  @NonNull
  @CheckResult
  LifecycleEvent latestLifecycleEvent();

  /**
   * Hook {@link Observer} into this activity's lifecycle.
   *
   * @param subscriber Lifecycle observer
   * @return Subscription for hooked lifecycle observing
   */
  @NonNull
  Subscription hookIntoLifecycle(@NonNull Observer<LifecycleEvent> subscriber);
}
