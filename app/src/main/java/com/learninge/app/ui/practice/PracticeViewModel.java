package com.learninge.app.ui.practice;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.learninge.app.core.Result;
import com.learninge.app.data.model.DeckSummary;
import com.learninge.app.ui.common.BaseViewModel;

import java.util.List;

/**
 * SCR-12: danh sách chủ đề để chọn quiz.
 */
public class PracticeViewModel extends BaseViewModel {

    private final MutableLiveData<Result<List<DeckSummary>>> topics = new MutableLiveData<>();

    public PracticeViewModel(@NonNull Application application) {
        super(application);
    }

    LiveData<Result<List<DeckSummary>>> topics() {
        return topics;
    }

    void load() {
        services.executors().dbRead().execute(() ->
                topics.postValue(services.deckService().listTopics(null)));
    }
}
