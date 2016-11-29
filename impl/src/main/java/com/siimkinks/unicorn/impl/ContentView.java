package com.siimkinks.unicorn.impl;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.CallSuper;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import com.siimkinks.unicorn.ContentViewContract;
import com.siimkinks.unicorn.DependencyGraphProvider;
import com.siimkinks.unicorn.Navigator;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observer;
import rx.Subscription;
import rx.subjects.BehaviorSubject;
import rx.subscriptions.CompositeSubscription;

import static com.siimkinks.unicorn.ContentViewContract.LifecycleEvent.CREATE;
import static com.siimkinks.unicorn.ContentViewContract.LifecycleEvent.DESTROY;
import static com.siimkinks.unicorn.ContentViewContract.LifecycleEvent.START;
import static com.siimkinks.unicorn.ContentViewContract.LifecycleEvent.STOP;
import static com.siimkinks.unicorn.ContentViewContract.LifecycleEvent.UNKNOWN;
import static com.siimkinks.unicorn.Contracts.mustBeFalse;
import static com.siimkinks.unicorn.Contracts.mustBeNull;
import static java.util.Objects.requireNonNull;

/**
 * An implementation of the {@link ContentViewContract}.
 *
 * @param <GraphProvider> Dependency graph provider type
 * @param <PresenterType> View presenter type
 */
public abstract class ContentView<GraphProvider extends DependencyGraphProvider, PresenterType extends PresenterContract> implements ContentViewContract<GraphProvider> {
  @NonNull
  private final BehaviorSubject<LifecycleEvent> lifecycleEvents = BehaviorSubject.create(UNKNOWN);
  @NonNull
  private final CompositeSubscription lifeSubscriptions = new CompositeSubscription();
  @Nullable
  private CompositeSubscription visibilitySubscriptions = null;
  @Nullable
  private View rootView;
  private volatile boolean viewsBound = false;
  @Nullable
  private Unbinder unbinder;

  protected Navigator navigator;

  protected ContentView() {
  }

  @CallSuper
  @Override
  public void onCreate(@NonNull GraphProvider provider) {
    this.navigator = provider.navigator();
    lifecycleEvents.onNext(CREATE);
  }

  @CallSuper
  @Override
  public void onStart() {
    requireNonNull(rootView, "RootView missing when resuming view??");
    mustBeNull(visibilitySubscriptions, "Visibility subscriptions have entered illegal state");
    visibilitySubscriptions = new CompositeSubscription();
    lifecycleEvents.onNext(START);
  }

  @CallSuper
  @Override
  public void onStop() {
    unsubscribeVisibilitySubscriptions();
    lifecycleEvents.onNext(STOP);
  }

  @CallSuper
  @Override
  public void onDestroy() {
    lifecycleEvents.onNext(DESTROY);
    unsubscribeVisibilitySubscriptions();
    lifeSubscriptions.unsubscribe();
  }

  /**
   * Injection point for initializing this view's presenter.
   * <p>
   * Annotate this method with {@code @Inject} annotation and your DI should do the rest.
   *
   * @param presenter This view's presenter
   */
  protected abstract void initializePresenter(@NonNull PresenterType presenter);

  @Override
  public final void setRootView(@Nullable View rootView) {
    this.rootView = rootView;
    if (rootView != null) {
      bindViews();
    } else {
      unbindViews();
    }
  }

  @Nullable
  @Override
  public final ViewGroup getRootView() {
    return (ViewGroup) rootView;
  }

  /**
   * Binds all views and handlers to their {@link ButterKnife} annotated fields.
   */
  @MainThread
  protected final void bindViews() {
    requireNonNull(rootView, "Cannot bind views if rootview is null");
    unbinder = ButterKnife.bind(this, rootView);
    viewsBound = true;
  }

  /**
   * Unbinds all {@link ButterKnife} views and handlers.
   */
  @MainThread
  protected final void unbindViews() {
    viewsBound = false;
    if (unbinder != null) {
      unbinder.unbind();
      unbinder = null;
    }
  }

  @NonNull
  @Override
  public BackPressResult onBackPressed() {
    return BackPressResult.NAVIGATE_BACK;
  }

  /**
   * Finish this view.
   */
  public final void finish() {
    navigator.finish(this);
  }

  @NonNull
  public final Context getContext() {
    requireNonNull(rootView, "Cannot get context before onCreate event");
    return rootView.getContext();
  }

  /**
   * Convenience method for getting {@link Resources}.
   *
   * @return Android resources
   */
  @NonNull
  protected final Resources getResources() {
    requireNonNull(rootView, "Cannot get resources before onCreate event");
    return rootView.getResources();
  }

  /**
   * Hook {@link Observer} into this view's lifecycle.
   *
   * @param subscriber Lifecycle observer
   * @return Subscription for hooked lifecycle observing
   */
  @NonNull
  public final Subscription hookIntoLifecycle(@NonNull Observer<LifecycleEvent> subscriber) {
    final Subscription subscription = lifecycleEvents.subscribe(subscriber);
    addSubscriptionForLife(subscription);
    return subscription;
  }

  /**
   * Unsubscribes and removes subscription from the lifecycle observing subscriptions list.
   *
   * @param subscription Subscription returned from the {@link #hookIntoLifecycle(Observer)} method
   */
  public final void unhookFromLifecycle(@NonNull Subscription subscription) {
    removeSubscriptionForLife(subscription);
  }

  @NonNull
  @Override
  // not final for testing purposes
  public LifecycleEvent latestLifecycleEvent() {
    return lifecycleEvents.getValue();
  }

  @Override
  public final void addSubscriptionForLife(@NonNull Subscription subscription) {
    mustBeFalse(lifeSubscriptions.isUnsubscribed(), "Tried to leak subscription for life but view is already dead. Check your code");
    lifeSubscriptions.add(subscription);
  }

  /**
   * Unsubscribes and removes subscription added by the {@link #addSubscriptionForLife(Subscription)}
   * method.
   *
   * @param subscription Subscription to remove
   */
  public final void removeSubscriptionForLife(@NonNull Subscription subscription) {
    subscription.unsubscribe();
    lifeSubscriptions.remove(subscription);
  }

  /**
   * Add subscription that should be active as long as view is visible.
   *
   * @param subscription Subscription to track
   */
  public final void addSubscriptionForVisibility(@NonNull Subscription subscription) {
    requireNonNull(visibilitySubscriptions, "Tried to add subscription when view is not visible. Check your code");
    visibilitySubscriptions.add(subscription);
  }

  /**
   * Unsubscribes and removes subscription added by the {@link #addSubscriptionForVisibility(Subscription)}
   * method.
   *
   * @param subscription Subscription to remove
   */
  public final void removeSubscriptionForVisibility(@NonNull Subscription subscription) {
    subscription.unsubscribe();
    if (visibilitySubscriptions != null) {
      visibilitySubscriptions.remove(subscription);
    }
  }

  private void unsubscribeVisibilitySubscriptions() {
    if (visibilitySubscriptions != null) {
      visibilitySubscriptions.unsubscribe();
      visibilitySubscriptions = null;
    }
  }

  @Override
  public boolean equals(Object o) {
    return super.equals(o) || o.getClass().getSimpleName().equals(getClass().getSimpleName());
  }

  /**
   * @return {@code true} when this view is destroyed
   */
  public final boolean isDestroyed() {
    return latestLifecycleEvent() == DESTROY;
  }

  /**
   * @return {@code true} when the views are bound
   */
  public final boolean isViewsBound() {
    return viewsBound;
  }
}
