package com.siimkinks.unicorn.sample.di.component;

import com.siimkinks.unicorn.sample.di.PerView;
import com.siimkinks.unicorn.sample.di.module.UiModule;
import com.siimkinks.unicorn.sample.view.FirstViewImpl;
import com.siimkinks.unicorn.sample.view.SecondViewImpl;

import dagger.Subcomponent;

@PerView
@Subcomponent(modules = {UiModule.class})
public interface UiComponent {
    void inject(FirstViewImpl view);

    void inject(SecondViewImpl view);
}
