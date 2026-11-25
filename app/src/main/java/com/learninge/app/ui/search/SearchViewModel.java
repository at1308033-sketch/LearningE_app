package com.learninge.app.ui.search;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.learninge.app.data.model.WordItem;
import com.learninge.app.ui.common.BaseViewModel;

import java.util.Collections;
import java.util.List;

/**
 * SCR-18 Tra từ.
 */
class SearchViewModel extends BaseViewModel {

    private final MutableLiveData<List<WordItem>> results = new MutableLiveData<>(Collections.emptyList());

    SearchViewModel(@NonNull Application application) {
        super(application);
    }

    LiveData<List<WordItem>> results() {
        return results;
    }

    void search(String query) {
        if (query == null || query.trim().isEmpty()) {
            results.postValue(Collections.emptyList());
            return;
        }
        services.executors().dbRead().execute(() ->
                results.postValue(services.deckService().searchWords(query)));
    }
}
