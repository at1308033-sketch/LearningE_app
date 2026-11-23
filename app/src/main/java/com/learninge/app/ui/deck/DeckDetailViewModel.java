package com.learninge.app.ui.deck;

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
 * Một bộ và các từ của nó (SCR-19).
 */
public class DeckDetailViewModel extends BaseViewModel {

    record State(DeckSummary deck, List<WordItem> words) {
    }

    private final MutableLiveData<Result<State>> state = new MutableLiveData<>();
    private final MutableLiveData<Result<Void>> deleted = new MutableLiveData<>();

    public DeckDetailViewModel(@NonNull Application application) {
        super(application);
    }

    LiveData<Result<State>> state() {
        return state;
    }

    LiveData<Result<Void>> deleted() {
        return deleted;
    }

    void load(long deckId) {
        services.executors().dbRead().execute(() -> {
            Result<DeckSummary> deck = services.deckService().getDeck(deckId);
            Result<List<WordItem>> words = services.deckService().listWords(deckId);
            state.postValue(deck.isOk() && words.isOk() ? Result.ok(new State(deck.value, words.value))
                    : Result.errorOf(deck.isOk() ? words : deck));
        });
    }

    void toggleFavorite(long deckId, long wordId) {
        services.executors().dbWrite().execute(() -> {
            services.deckService().toggleFavorite(wordId);
            load(deckId);
        });
    }

    void delete(long deckId) {
        services.executors().dbWrite().execute(() -> deleted.postValue(services.deckEditor().deleteDeck(deckId)));
    }
}
