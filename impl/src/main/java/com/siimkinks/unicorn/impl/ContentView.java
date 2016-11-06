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
import static com.siimkinks.unicorn.ContentViewContract.LifecycleEvent.PAUSE;
import static com.siimkinks.unicorn.ContentViewContract.LifecycleEvent.RESUME;
import static com.siimkinks.unicorn.ContentViewContract.LifecycleEvent.UNKNOWN;
import static com.siimkinks.unicorn.Contracts.mustBeFalse;
import static com.siimkinks.unicorn.Contracts.mustBeNull;
import static com.siimkinks.unicorn.Contracts.notNull;

public abstract class ContentView<GraphProvider extends DependencyGraphProvider, PresenterType extends PresenterContract> implements ContentViewContract<GraphProvider> {
    @NonNull
    private final BehaviorSubject<LifecycleEvent> lifecycleEvents = BehaviorSubject.create(UNKNOWN);
    @NonNull
    private final CompositeSubscription lifeSubscriptions = new CompositeSubscription();
    @Nullable
    private CompositeSubscription visibilitySubscriptions = null;
    @Nullable
    private View rootView;
    private boolean viewsBound = false;
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
    public void onResume() {
        notNull(rootView, "RootView missing when resuming view??");
        mustBeNull(visibilitySubscriptions, "Visibility subscriptions have entered illegal state");
        visibilitySubscriptions = new CompositeSubscription();
        rootView.requestFocusFromTouch();
        lifecycleEvents.onNext(RESUME);
    }

    @CallSuper
    @Override
    public void onPause() {
        unsubscribeVisibilitySubscriptions();
        lifecycleEvents.onNext(PAUSE);
    }

    @CallSuper
    @Override
    public void onDestroy() {
        lifecycleEvents.onNext(DESTROY);
        unsubscribeVisibilitySubscriptions();
        lifeSubscriptions.unsubscribe();
        setRootView(null);
    }

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

    @MainThread
    protected final void bindViews() {
        notNull(rootView, "Cannot bind views if rootview is null");
        unbinder = ButterKnife.bind(this, rootView);
        viewsBound = true;
    }

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

    public final void finish() {
        navigator.finish(this);
    }

    @NonNull
    public final Context getContext() {
        notNull(rootView, "Cannot get context before onCreate event");
        return rootView.getContext();
    }

    @NonNull
    protected final Resources getResources() {
        notNull(rootView, "Cannot get resources before onCreate event");
        return rootView.getResources();
    }

    @NonNull
    public final Subscription hookIntoLifecycle(@NonNull Observer<LifecycleEvent> subscriber) {
        final Subscription subscription = lifecycleEvents.subscribe(subscriber);
        addSubscriptionForLife(subscription);
        return subscription;
    }

    public final void unhookFromLifecycle(@NonNull Subscription subscription) {
        removeSubscriptionForLife(subscription);
    }

    @NonNull
    @Override
    // not final for testing purposes
    public LifecycleEvent latestLifecycleEvent() {
        return lifecycleEvents.getValue();
    }

    /**
     * Add subscription that should be active as long as view is alive.
     *
     * @param subscription
     *         Subscription to track
     */
    @Override
    public final void addSubscriptionForLife(@NonNull Subscription subscription) {
        mustBeFalse(lifeSubscriptions.isUnsubscribed(), "Tried to leak subscription for life but view is already dead. Check your code");
        lifeSubscriptions.add(subscription);
    }

    /**
     * Remove subscription add by the {@link #addSubscriptionForLife(Subscription)} method.
     *
     * @param subscription
     *         Subscription to remove
     */
    public final void removeSubscriptionForLife(@NonNull Subscription subscription) {
        subscription.unsubscribe();
        lifeSubscriptions.remove(subscription);
    }

    public final void addSubscriptionForVisibility(@NonNull Subscription subscription) {
        notNull(visibilitySubscriptions, "Tried to add subscription when view is not visible. Check your code");
        visibilitySubscriptions.add(subscription);
    }

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

    public final boolean isDestroyed() {
        return latestLifecycleEvent() == DESTROY;
    }

    public final boolean isViewsBound() {
        return viewsBound;
    }
}