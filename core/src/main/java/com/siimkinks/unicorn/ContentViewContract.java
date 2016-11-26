package com.siimkinks.unicorn;

import android.support.annotation.CheckResult;
import android.support.annotation.LayoutRes;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import rx.Subscription;

/**
 * Base view contract
 *
 * @param <GraphProvider>
 *         Application dependency injection provider
 */
public interface ContentViewContract<GraphProvider extends DependencyGraphProvider> {
    /**
     * View layout resource id
     *
     * @return view layout resource id.
     */
    @MainThread
    @LayoutRes
    int getViewResId();

    /**
     * Set root view.
     * <p>
     * Called when view manager has inflated layout provided by the {@link #getViewResId()}
     * method.
     *
     * @param view
     *         root layout view
     */
    @MainThread
    void setRootView(@Nullable View view);

    /**
     * Root view provided to the {@link #setRootView(View)}
     *
     * @return root view
     */
    @MainThread
    @Nullable
    ViewGroup getRootView();

    /**
     * Must return <b>new</b> instance of itself. This new instance
     * is used to re-init this view lifecycle.
     *
     * @return New instance of this view
     */
    @NonNull
    @CheckResult
    ContentViewContract copy();

    /**
     * Called when this view is created.
     *
     * @param provider
     *         Dependency graph provider
     */
    @MainThread
    void onCreate(@NonNull GraphProvider provider);

    /**
     * Called when the view has started and it is now visible.
     * <p>
     * Previous lifecycle event could have been either {@link LifecycleEvent#CREATE CREATE}
     * when the view has just been created or {@link LifecycleEvent#STOP STOP} if the view
     * has been brought to foreground and is visible again.
     */
    @MainThread
    void onStart();

    /**
     * Called when the view has stopped.
     * <p>
     * This usually indicates that the view is not visible any more.
     */
    @MainThread
    void onStop();

    /**
     * Called when this view is destroyed.
     */
    @MainThread
    void onDestroy();

    /**
     * Called when user presses BACK button.
     *
     * @return Enum indicating whether view should be destroyed or back press will have no
     * effect
     */
    @MainThread
    BackPressResult onBackPressed();

    /**
     * Get the latest lifecycle event called by the system.
     *
     * @return latest lifecycle event
     */
    @NonNull
    @CheckResult
    LifecycleEvent latestLifecycleEvent();

    /**
     * Add subscription that should be active as long as this view is alive.
     *
     * @param subscription
     *         Subscription to track
     */
    void addSubscriptionForLife(@NonNull Subscription subscription);

    enum LifecycleEvent {
        UNKNOWN, CREATE, START, STOP, DESTROY;

        @CheckResult
        public boolean isVisible() {
            return ordinal() < STOP.ordinal();
        }
    }

    enum BackPressResult {
        NAVIGATE_BACK, STAY_ON_VIEW
    }
}
