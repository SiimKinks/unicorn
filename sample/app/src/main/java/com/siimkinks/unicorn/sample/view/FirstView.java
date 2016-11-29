package com.siimkinks.unicorn.sample.view;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.widget.TextView;

import com.siimkinks.unicorn.ContentViewContract;
import com.siimkinks.unicorn.NavigationDetails;
import com.siimkinks.unicorn.impl.ContentView;
import com.siimkinks.unicorn.sample.R;
import com.siimkinks.unicorn.sample.di.DIProvider;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;

public final class FirstView extends ContentView<DIProvider, ViewPresenter> implements View {
  @BindView(R.id.content_text)
  TextView contentText;

  private ViewPresenter presenter;

  private FirstView() {
  }

  @NonNull
  @CheckResult
  public static NavigationDetails create() {
    return NavigationDetails
        .navigateTo(new FirstView())
        .build();
  }

  @NonNull
  @Override
  public ContentViewContract copy() {
    return new FirstView();
  }

  @Override
  public int getViewResId() {
    return R.layout.view_first;
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
  public void renderMessage(@NonNull String message) {
    contentText.setText(message);
  }

  @OnClick(R.id.go_to_next_view_btn)
  void onNextViewClick() {
    presenter.goToSecondView();
  }
}