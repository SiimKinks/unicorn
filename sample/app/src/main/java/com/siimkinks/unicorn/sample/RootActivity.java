package com.siimkinks.unicorn.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;

import com.siimkinks.unicorn.ViewManager;
import com.siimkinks.unicorn.impl.Unicorn;
import com.siimkinks.unicorn.sample.view.FirstViewImpl;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RootActivity extends AppCompatActivity {
    @BindView(R.id.content_root_view)
    ViewGroup contentRootView;

    @Inject
    ViewManager viewManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_root);
        ButterKnife.bind(this);
        final SampleApplication application = (SampleApplication) getApplication();
        application.appComponent().inject(this);
        application.registerForegroundActivity(this);

        Unicorn.builder()
                .activity(this)
                .viewManager(viewManager)
                .contentRootView(contentRootView)
                .firstView(FirstViewImpl.create())
                .build();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ((SampleApplication) getApplication()).unregisterForegroundActivity();
    }

    @Override
    public void onBackPressed() {
        if (!viewManager.handleBackPress()) {
            super.onBackPressed();
        }
    }
}
