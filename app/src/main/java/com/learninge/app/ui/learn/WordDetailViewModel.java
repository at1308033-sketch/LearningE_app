package com.learninge.app.ui.learn;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.learninge.app.core.Result;
import com.learninge.app.data.model.WordDetail;
import com.learninge.app.ui.common.BaseViewModel;

/**
 * Chi tiết một từ (US-008, US-011).
 */
public class WordDetailViewModel extends BaseViewModel {
    private final MutableLiveData<Result<WordDetail>> word = new MutableLiveData<>();

    public WordDetailViewModel(@NonNull Application application) {
        super(application);
    }

    LiveData<Result<WordDetail>> word() {
        return word;
    }

    void load(long wordId) {
        services.executors().dbRead().execute(() -> word.postValue(services.deckService().getWord(wordId)));
    }

    void toggleFavorite(long wordId) {
        services.executors().dbWrite().execute(() -> {
            services.deckService().toggleFavorite(wordId);
            load(wordId);
        });
    }
}
