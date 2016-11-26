package com.siimkinks.unicorn.sample.view;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.widget.TextView;

import com.siimkinks.unicorn.ContentViewContract;
import com.siimkinks.unicorn.NavigationDetails;
import com.siimkinks.unicorn.Navigator;
import com.siimkinks.unicorn.impl.ContentView;
import com.siimkinks.unicorn.sample.R;
import com.siimkinks.unicorn.sample.di.DIProvider;

import javax.inject.Inject;

import butterknife.BindView;

public final class SecondViewImpl extends ContentView<DIProvider, ViewPresenter> implements View {
    @BindView(R.id.content_text)
    TextView contentText;

    private ViewPresenter presenter;

    private SecondViewImpl() {
    }

    public static void go(@NonNull Navigator navigator) {
        create().go(navigator);
    }

    @NonNull
    @CheckResult
    public static NavigationDetails create() {
        return NavigationDetails
                .navigateTo(new SecondViewImpl())
                .singleInstance(true)
                .build();
    }

    @NonNull
    @Override
    public ContentViewContract copy() {
        return new SecondViewImpl();
    }

    @Override
    public int getViewResId() {
        return R.layout.view_second;
    }

    @Inject
    @Override
    protected void initializePresenter(@NonNull ViewPresenter presenter) {
        this.presenter = presenter;
        presenter.hookInto(this);
    }

    @Override
    public void onCreate(@NonNull DIProvider provider) {
        super.onCreate(provider);
        provider.uiComponent().inject(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        presenter.random();
    }

    @Override
    public void renderMessage(@NonNull String message) {
        contentText.setText(message);
    }
}