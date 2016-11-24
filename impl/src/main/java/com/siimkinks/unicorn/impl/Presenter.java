package com.siimkinks.unicorn.impl;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.siimkinks.unicorn.ContentViewContract;
import com.siimkinks.unicorn.Navigator;

import rx.Subscription;
import rx.functions.Action0;
import rx.subscriptions.CompositeSubscription;
import rx.subscriptions.Subscriptions;

import static com.siimkinks.unicorn.Contracts.notNull;

/**
 * An implementation of the {@link PresenterContract}.
 *
 * @param <ViewType>
 *         Associated view interface type
 */
public abstract class Presenter<ViewType extends ContentViewContract> implements PresenterContract<ViewType> {
    @NonNull
    protected final Navigator navigator;
    @Nullable
    protected ViewType view;
    @Nullable
    private CompositeSubscription subscriptions;

    public Presenter(@NonNull Navigator navigator) {
        this.navigator = navigator;
    }

    @CallSuper
    @Override
    public void hookInto(@NonNull ViewType view) {
        this.view = view;
        view.addSubscriptionForLife(Subscriptions.create(new Action0() {
            @Override
            public void call() {
                destroy();
            }
        }));
        subscriptions = new CompositeSubscription();
    }

    @CallSuper
    @Override
    public void destroy() {
        if (subscriptions != null) {
            subscriptions.unsubscribe();
            subscriptions = null;
        }
        view = null;
    }

    /**
     * Add subscription that should be active as long as this presenter is active.
     * <p>
     * This presenter is active as long as the associated view is alive.
     *
     * @param subscription
     *         Subscription to track
     */
    protected final void addSubscription(@NonNull Subscription subscription) {
        notNull(subscriptions, "Tried to leak subscription. Check your code");
        subscriptions.add(subscription);
    }

    /**
     * Unsubscribes and removes subscription added by the {@link #addSubscription(Subscription)} method.
     *
     * @param subscription
     *         Subscription to remove
     */
    protected final void removeSubscription(@NonNull Subscription subscription) {
        subscription.unsubscribe();
        if (subscriptions != null) {
            subscriptions.remove(subscription);
        }
    }
}
