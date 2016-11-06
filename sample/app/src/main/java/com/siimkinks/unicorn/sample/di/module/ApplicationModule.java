package com.siimkinks.unicorn.sample.di.module;

import android.app.Application;

import com.siimkinks.unicorn.Navigator;
import com.siimkinks.unicorn.ViewManager;
import com.siimkinks.unicorn.sample.SampleApplication;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public final class ApplicationModule {
    private final SampleApplication application;

    public ApplicationModule(SampleApplication application) {
        this.application = application;
    }

    @Provides
    SampleApplication provideTypedApplication() {
        return this.application;
    }

    @Provides
    Application provideBaseApplication() {
        return this.application;
    }

    @SuppressWarnings("unchecked")
    @Provides
    @Singleton
    ViewManager provideViewManager(SampleApplication application) {
        return new ViewManager(application);
    }

    @Provides
    @Singleton
    Navigator provideNavigator(ViewManager viewManager) {
        return new Navigator(viewManager);
    }
}
