package com.learninge.app.ui.common;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.learninge.app.LearningEApp;
import com.learninge.app.core.ServiceLocator;

/**
 * ViewModel có sẵn {@link ServiceLocator}. Chỉ giữ Application, không giữ Context của Activity.
 */
public abstract class BaseViewModel extends AndroidViewModel {
    protected final ServiceLocator services;

    protected BaseViewModel(@NonNull Application application) {
        super(application);
        services = LearningEApp.services(application);
    }
}
