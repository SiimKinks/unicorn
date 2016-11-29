package com.siimkinks.unicorn.sample.di.component;

import com.siimkinks.unicorn.sample.di.PerView;
import com.siimkinks.unicorn.sample.di.module.UiModule;
import com.siimkinks.unicorn.sample.view.FirstView;
import com.siimkinks.unicorn.sample.view.SecondView;

import dagger.Subcomponent;

@PerView
@Subcomponent(modules = {UiModule.class})
public interface UiComponent {
  void inject(FirstView view);

  void inject(SecondView view);
}
