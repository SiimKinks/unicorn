package com.siimkinks.unicorn.sample.view;

import android.support.annotation.NonNull;

import com.siimkinks.unicorn.Navigator;
import com.siimkinks.unicorn.impl.Presenter;
import com.siimkinks.unicorn.sample.di.PerView;

import java.util.Random;

import javax.inject.Inject;

@PerView
final class ViewPresenter extends Presenter<View> {

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
        SecondViewImpl.go(navigator);
    }

    void random() {
        view.renderMessage("This is a random number: " + new Random().nextInt());
    }
}