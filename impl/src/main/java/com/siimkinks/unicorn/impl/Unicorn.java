package com.siimkinks.unicorn.impl;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.ViewGroup;

import com.siimkinks.unicorn.ContentViewContract.LifecycleEvent;
import com.siimkinks.unicorn.NavigationDetails;
import com.siimkinks.unicorn.RootActivityContract;
import com.siimkinks.unicorn.ViewManager;

import rx.Observer;
import rx.Subscription;
import rx.subjects.BehaviorSubject;
import rx.subscriptions.CompositeSubscription;

import static com.siimkinks.unicorn.ContentViewContract.LifecycleEvent.CREATE;
import static com.siimkinks.unicorn.ContentViewContract.LifecycleEvent.DESTROY;
import static com.siimkinks.unicorn.ContentViewContract.LifecycleEvent.STOP;
import static com.siimkinks.unicorn.ContentViewContract.LifecycleEvent.START;
import static com.siimkinks.unicorn.ContentViewContract.LifecycleEvent.UNKNOWN;
import static com.siimkinks.unicorn.Contracts.mustBeFalse;
import static java.util.Objects.requireNonNull;

/**
 * Root activity handler and an implementation of {@link RootActivityContract}.
 */
public class Unicorn implements RootActivityContract, Application.ActivityLifecycleCallbacks {
    @NonNull
    private final BehaviorSubject<LifecycleEvent> lifecycleEvents = BehaviorSubject.create(UNKNOWN);
    @NonNull
    private final CompositeSubscription lifeSubscriptions = new CompositeSubscription();
    @NonNull
    private final ViewManager viewManager;
    @NonNull
    private final ViewGroup contentRootView;
    @NonNull
    private final NavigationDetails firstView;
    @NonNull
    private final Application app;

    private Unicorn(@NonNull ViewManager viewManager,
                    @NonNull Activity activity,
                    @NonNull ViewGroup contentRootView,
                    @NonNull NavigationDetails firstView) {
        this.viewManager = viewManager;
        this.contentRootView = contentRootView;
        this.firstView = firstView;
        this.app = (Application) activity.getApplicationContext();

        viewManager.registerActivity(this, activity);
        lifecycleEvents.onNext(CREATE);

        app.registerActivityLifecycleCallbacks(this);
    }

    /**
     * Builder for {@link Unicorn} object.
     */
    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
        lifecycleEvents.onNext(CREATE);
    }

    @Override
    public void onActivityStarted(Activity activity) {
        lifecycleEvents.onNext(START);
    }

    @Override
    public void onActivityResumed(Activity activity) {
    }

    @Override
    public void onActivityPaused(Activity activity) {
    }

    @Override
    public void onActivityStopped(Activity activity) {
        lifecycleEvents.onNext(STOP);
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        app.unregisterActivityLifecycleCallbacks(this);
        lifecycleEvents.onNext(DESTROY);
        lifeSubscriptions.clear();
        viewManager.unregisterActivity();
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
    }

    @NonNull
    @Override
    public ViewGroup getContentRootView() {
        return contentRootView;
    }

    @NonNull
    @Override
    public NavigationDetails getFirstView() {
        return firstView
                .copy(firstView.view().copy())
                .build();
    }

    @NonNull
    @Override
    public final LifecycleEvent latestLifecycleEvent() {
        return lifecycleEvents.getValue();
    }

    @NonNull
    @Override
    public final Subscription hookIntoLifecycle(@NonNull Observer<LifecycleEvent> subscriber) {
        final Subscription subscription = lifecycleEvents.subscribe(subscriber);
        addSubscriptionForLife(subscription);
        return subscription;
    }

    /**
     * Unsubscribes and removes subscription from the lifecycle observing subscriptions list.
     *
     * @param subscription
     *         Subscription returned from the {@link #hookIntoLifecycle(Observer)} method
     */
    public final void unhookFromLifecycle(@NonNull Subscription subscription) {
        removeSubscriptionForLife(subscription);
    }

    /**
     * Add subscription that should be active as long as the activity is alive.
     *
     * @param subscription
     *         Subscription to track
     */
    public final void addSubscriptionForLife(@NonNull Subscription subscription) {
        mustBeFalse(lifeSubscriptions.isUnsubscribed(), "Tried to leak subscription for life but activity is already dead. Check your code");
        lifeSubscriptions.add(subscription);
    }

    /**
     * Unsubscribes and removes subscription added by the {@link #addSubscriptionForLife(Subscription)}
     * method.
     *
     * @param subscription
     *         Subscription to remove
     */
    public final void removeSubscriptionForLife(@NonNull Subscription subscription) {
        subscription.unsubscribe();
        lifeSubscriptions.remove(subscription);
    }

    public static final class Builder {
        ViewManager viewManager;
        Activity activity;
        ViewGroup contentRootView;
        NavigationDetails firstView;

        Builder() {
        }

        /**
         * Self created {@link ViewManager} instance.
         *
         * @param viewManager
         *         Unicorn ViewManager
         * @return Unicorn builder
         */
        public Builder viewManager(@NonNull ViewManager viewManager) {
            this.viewManager = viewManager;
            return this;
        }

        /**
         * Root activity.
         *
         * @param activity
         *         Root activity
         * @return Unicorn builder
         */
        public Builder activity(@NonNull Activity activity) {
            this.activity = activity;
            return this;
        }

        /**
         * The content root view where all {@link ContentView} views will be inflated.
         *
         * @param contentRootView
         *         Root view group
         * @return Unicorn builder
         */
        public Builder contentRootView(@NonNull ViewGroup contentRootView) {
            this.contentRootView = contentRootView;
            return this;
        }

        /**
         * First view to display.
         *
         * @param firstView
         *         First view navigation details
         * @return Unicorn builder
         */
        public Builder firstView(@NonNull NavigationDetails firstView) {
            this.firstView = firstView;
            return this;
        }

        /**
         * Build a Unicorn.
         *
         * @return Unicorn object
         */
        public Unicorn build() {
            requireNonNull(viewManager, "viewManager == null");
            requireNonNull(activity, "activity == null");
            requireNonNull(contentRootView, "contentRootView == null");
            requireNonNull(firstView, "contentRootView == null");
            return new Unicorn(viewManager, activity, contentRootView, firstView);
        }
    }
}
