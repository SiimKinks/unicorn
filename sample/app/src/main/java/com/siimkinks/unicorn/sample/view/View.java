package com.siimkinks.unicorn.sample.view;

import android.support.annotation.NonNull;

import com.siimkinks.unicorn.ContentViewContract;
import com.siimkinks.unicorn.sample.di.DIProvider;

interface View extends ContentViewContract<DIProvider> {
  void renderMessage(@NonNull String message);
}