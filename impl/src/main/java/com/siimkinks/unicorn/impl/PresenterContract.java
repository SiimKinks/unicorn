package com.siimkinks.unicorn.impl;

import android.support.annotation.NonNull;

import com.siimkinks.unicorn.ContentViewContract;

public interface PresenterContract<ViewType extends ContentViewContract> {
    void hookInto(@NonNull ViewType view);

    void destroy();
}
