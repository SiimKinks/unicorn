package com.siimkinks.unicorn.sample.di;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;

import com.siimkinks.unicorn.DependencyGraphProvider;
import com.siimkinks.unicorn.sample.di.component.AppComponent;
import com.siimkinks.unicorn.sample.di.component.UiComponent;

public interface DIProvider extends DependencyGraphProvider {
    @NonNull
    @CheckResult
    AppComponent appComponent();

    @NonNull
    @CheckResult
    UiComponent uiComponent();
}
