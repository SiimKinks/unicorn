package com.siimkinks.unicorn;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Dual<T1, T2> {
    Dual() {
    }

    public abstract T1 first();

    public abstract T2 second();

    @NonNull
    @CheckResult
    public static <T1, T2> Dual<T1, T2> create(@NonNull T1 first, @NonNull T2 second) {
        return new AutoValue_Dual<T1, T2>(first, second);
    }
}