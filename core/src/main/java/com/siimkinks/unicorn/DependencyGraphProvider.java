package com.siimkinks.unicorn;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;

/**
 * Application dependency graph provider
 */
public interface DependencyGraphProvider {
    @NonNull
    @CheckResult
    Navigator navigator();
}
