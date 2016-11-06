package com.siimkinks.unicorn;

import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

/**
 * API to navigate between views and finish them
 */
public final class Navigator {
    @NonNull
    private final ViewManager viewManager;

    public Navigator(@NonNull ViewManager viewManager) {
        this.viewManager = viewManager;
    }

    /**
     * Navigate to view according to the provided navigation details.
     *
     * @param navDetails
     *         Navigation details defining how view manager should handle
     *         view's enter and leave animations, its stack modifications, etc
     */
    @MainThread
    public final void navigateTo(@NonNull NavigationDetails navDetails) {
        viewManager.navigate(navDetails);
    }

    /**
     * Finish provided view.
     * <p>
     * Provided view will be popped from the view stack and appropriate lifecycle methods
     * will be called on it.
     *
     * @param view
     *         View to finish
     */
    @MainThread
    public final void finish(@NonNull ContentViewContract view) {
        viewManager.finish(view);
    }
}
