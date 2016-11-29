package com.siimkinks.unicorn.sample.view;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.widget.TextView;

import com.siimkinks.unicorn.ContentViewContract;
import com.siimkinks.unicorn.NavigationDetails;
import com.siimkinks.unicorn.TransitionDetails;
import com.siimkinks.unicorn.impl.ContentView;
import com.siimkinks.unicorn.sample.R;
import com.siimkinks.unicorn.sample.di.DIProvider;
import com.siimkinks.unicorn.sample.view.transition.ArcMoveTransitionHandler;

import butterknife.BindView;

public class ThirdView extends ContentView<DIProvider, ViewPresenter> implements View {
  @BindView(R.id.content_text)
  TextView contentText;

  @NonNull
  private final String prevViewMessage;

  private ThirdView(@NonNull String prevViewMessage) {
    this.prevViewMessage = prevViewMessage;
  }

  @NonNull
  @CheckResult
  public static NavigationDetails create(@NonNull String prevViewMessage) {
    return NavigationDetails
        .navigateTo(new ThirdView(prevViewMessage))
        .transition(TransitionDetails.builder()
            .enter(ArcMoveTransitionHandler.INSTANCE)
            .leave(ArcMoveTransitionHandler.INSTANCE)
            .build())
        .singleInstance(true)
        .build();
  }

  @NonNull
  @Override
  public ContentViewContract copy() {
    return new ThirdView(prevViewMessage);
  }

  @Override
  public int getViewResId() {
    return R.layout.view_third;
  }

  @Override
  protected void initializePresenter(@NonNull ViewPresenter presenter) {
    presenter.hookInto(this);
  }

  @Override
  public void onStart() {
    super.onStart();
    renderMessage(prevViewMessage);
  }

  @Override
  public void renderMessage(@NonNull String message) {
    contentText.setText(message);
  }
}
