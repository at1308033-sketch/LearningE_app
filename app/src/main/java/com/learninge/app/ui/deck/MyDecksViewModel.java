package com.learninge.app.ui.deck;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.learninge.app.core.Result;
import com.learninge.app.data.model.DeckSummary;
import com.learninge.app.ui.common.BaseViewModel;

import java.util.List;

/**
 * Danh sách bộ ở SCR-26.
 */
public class MyDecksViewModel extends BaseViewModel {
    private final MutableLiveData<Result<List<DeckSummary>>> decks = new MutableLiveData<>();

    public MyDecksViewModel(@NonNull Application application) {
        super(application);
    }

    LiveData<Result<List<DeckSummary>>> decks() {
        return decks;
    }

    void load() {
        services.executors().dbRead().execute(() -> decks.postValue(services.deckService().listMyDecks()));
    }
}
