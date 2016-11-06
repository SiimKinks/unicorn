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

public abstract class Presenter<ViewType extends ContentViewContract> implements PresenterContract<ViewType> {
    protected final Navigator navigator;
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

    protected final void addSubscription(@NonNull Subscription subscription) {
        notNull(subscriptions, "Tried to leak subscription. Check your code");
        subscriptions.add(subscription);
    }

    protected final void removeSubscription(@NonNull Subscription subscription) {
        subscription.unsubscribe();
        if (subscriptions != null) {
            subscriptions.remove(subscription);
        }
    }
}
