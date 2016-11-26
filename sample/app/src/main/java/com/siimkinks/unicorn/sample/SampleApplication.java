package com.siimkinks.unicorn.sample;

import android.app.Application;
import android.support.annotation.NonNull;

import com.siimkinks.unicorn.Navigator;
import com.siimkinks.unicorn.sample.di.DIProvider;
import com.siimkinks.unicorn.sample.di.component.AppComponent;
import com.siimkinks.unicorn.sample.di.component.DaggerAppComponent;
import com.siimkinks.unicorn.sample.di.component.UiComponent;
import com.siimkinks.unicorn.sample.di.module.ApplicationModule;
import com.siimkinks.unicorn.sample.di.module.UiModule;

import javax.inject.Inject;

import static com.siimkinks.unicorn.Contracts.notNull;

public final class SampleApplication extends Application implements DIProvider {
    @Inject
    Navigator navigator;

    private AppComponent appComponent;
    private UiModule uiModule;

    @Override
    public void onCreate() {
        super.onCreate();
        final AppComponent appComponent = DaggerAppComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();
        appComponent.inject(this);
        this.appComponent = appComponent;
    }

    @NonNull
    @Override
    public Navigator navigator() {
        return navigator;
    }

    public void registerForegroundActivity(@NonNull RootActivity rootActivity) {
        uiModule = new UiModule(rootActivity);
    }

    public void unregisterForegroundActivity() {
        uiModule = null;
    }

    @NonNull
    @Override
    public AppComponent appComponent() {
        return appComponent;
    }

    @NonNull
    @Override
    public UiComponent uiComponent() {
        notNull(uiModule, "Cannot build new UI component if there is no UI module");
        return appComponent.uiComponent(uiModule);
    }
}
