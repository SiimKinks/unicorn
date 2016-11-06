package com.siimkinks.unicorn.impl;

import android.support.annotation.NonNull;
import android.view.ViewGroup;

import com.siimkinks.unicorn.ContentViewContract.LifecycleEvent;
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

    public final void unhookFromLifecycle(@NonNull Subscription subscription) {
        removeSubscriptionForLife(subscription);
    }

    public final void addSubscriptionForLife(@NonNull Subscription subscription) {
        mustBeFalse(lifeSubscriptions.isUnsubscribed(), "Tried to leak subscription for life but activity is already dead. Check your code");
        lifeSubscriptions.add(subscription);
    }

    public final void removeSubscriptionForLife(@NonNull Subscription subscription) {
        subscription.unsubscribe();
        lifeSubscriptions.remove(subscription);
    }

    public static final class Builder {
        ViewGroup contentRootView;
        NavigationDetails firstView;

        Builder() {
        }

        public Builder contentRootView(@NonNull ViewGroup contentRootView) {
            this.contentRootView = contentRootView;
            return this;
        }

        public Builder firstView(@NonNull NavigationDetails firstView) {
            this.firstView = firstView;
            return this;
        }

        public RootActivityDelegate build() {
            requireNonNull(contentRootView, "contentRootView == null");
            requireNonNull(firstView, "contentRootView == null");
            return new RootActivityDelegate(contentRootView, firstView);
        }
    }
}
