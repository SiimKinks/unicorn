package com.siimkinks.unicorn;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.ViewGroup;

import com.siimkinks.unicorn.ContentViewContract.LifecycleEvent;

import rx.Observer;
import rx.Subscription;
import rx.subjects.BehaviorSubject;

import static com.siimkinks.unicorn.ContentViewContract.LifecycleEvent.CREATE;
import static com.siimkinks.unicorn.ContentViewContract.LifecycleEvent.UNKNOWN;


public class MockRootActivity extends Activity implements RootActivityContract {
  private final BehaviorSubject<LifecycleEvent> lifecycleEvents = BehaviorSubject.create(UNKNOWN);

  final MockView firstView = MockView.createNewView();

  @NonNull
  @Override
  public ViewGroup getContentRootView() {
    return null;
  }

  @NonNull
  @Override
  public NavigationDetails getFirstView() {
    return NavigationDetails.navigateTo(firstView).build();
  }

  @NonNull
  @Override
  public LifecycleEvent latestLifecycleEvent() {
    return CREATE;
  }

  @NonNull
  @Override
  public Subscription hookIntoLifecycle(@NonNull Observer<LifecycleEvent> subscriber) {
    return lifecycleEvents.subscribe(subscriber);
  }

  public void unhookFromLifecycle(@NonNull Subscription subscription) {
  }

  void emitLifecycleEvent(@NonNull LifecycleEvent lifecycleEvent) {
    lifecycleEvents.onNext(lifecycleEvent);
  }
}
