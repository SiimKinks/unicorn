package com.siimkinks.unicorn;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import static org.mockito.Mockito.spy;

public class MockView implements ContentViewContract {
    private View rootView;
    private LifecycleEvent latestLifecycleEvent;

    public static MockView createNewView() {
        return spy(new MockView());
    }

    @Override
    public int getViewResId() {
        return 0;
    }

    @Override
    public void setRootView(@Nullable View rootView) {
        this.rootView = rootView;
    }

    @Nullable
    @Override
    public ViewGroup getRootView() {
        return (ViewGroup) rootView;
    }

    @NonNull
    @Override
    public ContentViewContract copy() {
        return spy(new MockView());
    }

    @Override
    public void onCreate(@NonNull DependencyGraphProvider provider) {
        latestLifecycleEvent = LifecycleEvent.CREATE;
    }

    @Override
    public void onResume() {
        latestLifecycleEvent = LifecycleEvent.RESUME;
    }

    @Override
    public void onPause() {
        latestLifecycleEvent = LifecycleEvent.PAUSE;
    }

    @Override
    public void onDestroy() {
        latestLifecycleEvent = LifecycleEvent.DESTROY;
    }

    @Override
    public BackPressResult onBackPressed() {
        return BackPressResult.NAVIGATE_BACK;
    }

    @NonNull
    @Override
    public LifecycleEvent latestLifecycleEvent() {
        return latestLifecycleEvent;
    }

    NavigationDetails create() {
        return NavigationDetails.navigateTo(this).build();
    }
}
