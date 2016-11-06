package com.siimkinks.unicorn.sample;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;

import com.siimkinks.unicorn.ContentViewContract;
import com.siimkinks.unicorn.NavigationDetails;
import com.siimkinks.unicorn.RootActivityContract;
import com.siimkinks.unicorn.impl.RootActivityDelegate;
import com.siimkinks.unicorn.sample.view.FirstViewImpl;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observer;
import rx.Subscription;

public class RootActivity extends AppCompatActivity implements RootActivityContract {
    @BindView(R.id.content_root_view)
    ViewGroup contentRootView;

    private RootActivityDelegate activityDelegate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_root);
        ButterKnife.bind(this);
        activityDelegate = RootActivityDelegate.builder()
                .contentRootView(contentRootView)
                .firstView(FirstViewImpl.create())
                .build();
        final SampleApplication app = (SampleApplication) getApplication();
        app.registerForegroundActivity(this);
        activityDelegate.onCreate();
    }

    @Override
    protected void onStart() {
        super.onStart();
        activityDelegate.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        activityDelegate.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        activityDelegate.onDestroy();
        ((SampleApplication) getApplication()).unregisterForegroundActivity();
    }

    @Override
    public void onBackPressed() {
        if (!((SampleApplication) getApplication()).handleBackPress()) {
            super.onBackPressed();
        }
    }

    @NonNull
    @Override
    public ViewGroup getContentRootView() {
        return activityDelegate.getContentRootView();
    }

    @NonNull
    @Override
    public NavigationDetails getFirstView() {
        return activityDelegate.getFirstView();
    }

    @NonNull
    @Override
    public ContentViewContract.LifecycleEvent latestLifecycleEvent() {
        return activityDelegate.latestLifecycleEvent();
    }

    @NonNull
    @Override
    public Subscription hookIntoLifecycle(@NonNull Observer<ContentViewContract.LifecycleEvent> subscriber) {
        return activityDelegate.hookIntoLifecycle(subscriber);
    }
}
