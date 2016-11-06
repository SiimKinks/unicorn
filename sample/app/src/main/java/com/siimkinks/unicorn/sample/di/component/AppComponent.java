package com.siimkinks.unicorn.sample.di.component;

import com.siimkinks.unicorn.sample.SampleApplication;
import com.siimkinks.unicorn.sample.di.module.ApplicationModule;
import com.siimkinks.unicorn.sample.di.module.UiModule;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {
        ApplicationModule.class,
})
public interface AppComponent {
    void inject(SampleApplication application);

    /* Sub-Components */
    UiComponent uiComponent(UiModule uiModule);
}
