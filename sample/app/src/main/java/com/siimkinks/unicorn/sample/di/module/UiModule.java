package com.siimkinks.unicorn.sample.di.module;

import android.app.Activity;
import android.support.annotation.NonNull;

import com.siimkinks.unicorn.sample.RootActivity;
import com.siimkinks.unicorn.sample.di.PerView;

import dagger.Module;
import dagger.Provides;

@Module
public final class UiModule {
    private final Activity foregroundActivity;

    public UiModule(@NonNull RootActivity foregroundActivity) {
        this.foregroundActivity = foregroundActivity;
    }

    @PerView
    @Provides
    public Activity provideForegroundActivity() {
        return foregroundActivity;
    }
}
