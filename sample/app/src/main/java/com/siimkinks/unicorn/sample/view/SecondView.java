package com.siimkinks.unicorn.sample.view;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.widget.TextView;

import com.siimkinks.unicorn.ContentViewContract;
import com.siimkinks.unicorn.NavigationDetails;
import com.siimkinks.unicorn.Navigator;
import com.siimkinks.unicorn.TransitionDetails;
import com.siimkinks.unicorn.impl.ContentView;
import com.siimkinks.unicorn.sample.R;
import com.siimkinks.unicorn.sample.di.DIProvider;
import com.siimkinks.unicorn.sample.view.transition.FadeTransition;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;

public final class SecondView extends ContentView<DIProvider, ViewPresenter> implements View {
  @BindView(R.id.content_text)
  TextView contentText;

  private ViewPresenter presenter;

  private SecondView() {
  }

  public static void go(@NonNull Navigator navigator) {
    create().go(navigator);
  }

  @NonNull
  @CheckResult
  public static NavigationDetails create() {
    return NavigationDetails
        .navigateTo(new SecondView())
        .transition(TransitionDetails.builder()
            .enter(new FadeTransition())
            .leave(new FadeTransition())
            .build())
        .singleInstance(true)
        .build();
  }

  @NonNull
  @Override
  public ContentViewContract copy() {
    return new SecondView();
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

  @OnClick(R.id.go_to_next_view_btn)
  void onNextViewClick() {
    presenter.goToThirdView();
  }
}