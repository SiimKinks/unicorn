package com.siimkinks.unicorn.impl;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.ViewGroup;

import com.siimkinks.unicorn.ContentViewContract.LifecycleEvent;
import com.siimkinks.unicorn.NavigationDetails;
import com.siimkinks.unicorn.RootActivityContract;

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
import static java.util.Objects.requireNonNull;

/**
 * Root activity delegate and an implementation of {@link RootActivityContract}.
 * <p>
 * In order for this class to work properly the following methods must be delegated to this class:
 * <p>
 * <ul>
 * <li>{@link Activity#onCreate(Bundle)}</li>
 * <li>{@link Activity#onPause()}</li>
 * <li>{@link Activity#onResume()}</li>
 * <li>{@link Activity#onDestroy()}</li>
 * </ul>
 */
public class RootActivityDelegate implements RootActivityContract {
    @NonNull
    private final BehaviorSubject<LifecycleEvent> lifecycleEvents = BehaviorSubject.create(UNKNOWN);
    @NonNull
    private final CompositeSubscription lifeSubscriptions = new CompositeSubscription();
    @NonNull
    private final ViewGroup contentRootView;
    @NonNull
    private final NavigationDetails firstView;

    private RootActivityDelegate(@NonNull ViewGroup contentRootView,
                                 @NonNull NavigationDetails firstView) {

        this.contentRootView = contentRootView;
        this.firstView = firstView;
    }

    /**
     * Builder for {@link RootActivityDelegate}.
     */
    public static Builder builder() {
        return new Builder();
    }

    public void onCreate() {
        lifecycleEvents.onNext(CREATE);
    }

    public void onStart() {
        lifecycleEvents.onNext(RESUME);
    }

    public void onStop() {
        lifecycleEvents.onNext(PAUSE);
    }

    public void onDestroy() {
        lifecycleEvents.onNext(DESTROY);
        lifeSubscriptions.clear();
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
        ViewGroup contentRootView;
        NavigationDetails firstView;

        Builder() {
        }

        /**
         * The content root view where all {@link ContentView} views will be inflated.
         *
         * @param contentRootView
         *         Root view group
         * @return Root activity delegate builder
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
         * @return Root activity delegate builder
         */
        public Builder firstView(@NonNull NavigationDetails firstView) {
            this.firstView = firstView;
            return this;
        }

        /**
         * Build this root activity delegate object where all the required methods must delegate to.
         *
         * @return Root activity delegate object
         */
        public RootActivityDelegate build() {
            requireNonNull(contentRootView, "contentRootView == null");
            requireNonNull(firstView, "contentRootView == null");
            return new RootActivityDelegate(contentRootView, firstView);
        }
    }
}
