package com.learninge.app.ui.learn;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.learninge.app.core.Result;
import com.learninge.app.data.model.DeckSummary;
import com.learninge.app.data.model.WordItem;
import com.learninge.app.ui.common.BaseViewModel;

import java.util.List;

/**
 * Từ của một chủ đề (US-008).
 */
public class WordListViewModel extends BaseViewModel {

    record State(DeckSummary deck, List<WordItem> words) {
    }

    private final MutableLiveData<Result<State>> state = new MutableLiveData<>();

    public WordListViewModel(@NonNull Application application) {
        super(application);
    }

    LiveData<Result<State>> state() {
        return state;
    }

    void toggleFavorite(long deckId, long wordId) {
        services.executors().dbWrite().execute(() -> {
            services.deckService().toggleFavorite(wordId);
            load(deckId);
        });
    }

    void load(long deckId) {
        services.executors().dbRead().execute(() -> {
            Result<DeckSummary> deck = services.deckService().getDeck(deckId);
            Result<List<WordItem>> words = services.deckService().listWords(deckId);
            state.postValue(deck.isOk() && words.isOk() ? Result.ok(new State(deck.value, words.value))
                    : Result.errorOf(deck.isOk() ? words : deck));
        });
    }
}
