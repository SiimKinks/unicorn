package com.siimkinks.unicorn.sample.view;

import android.support.annotation.NonNull;

import com.siimkinks.unicorn.Navigator;
import com.siimkinks.unicorn.impl.Presenter;
import com.siimkinks.unicorn.sample.di.PerView;

import java.util.Random;

import javax.inject.Inject;

@PerView
final class ViewPresenter extends Presenter<View> {

  @NonNull
  private String latestMessage = "";

  @SuppressWarnings("unchecked")
  @Inject
  ViewPresenter(Navigator navigator) {
    super(navigator);
  }

  @Override
  public void hookInto(@NonNull View view) {
    super.hookInto(view);
    view.renderMessage("Hello world!");
  }

  void goToSecondView() {
    SecondView.go(navigator);
  }

  void goToThirdView() {
    ThirdView
        .create(latestMessage)
        .go(navigator);
  }

  void random() {
    latestMessage = "This is a random number: " + new Random().nextInt();
    view.renderMessage(latestMessage);
  }
}