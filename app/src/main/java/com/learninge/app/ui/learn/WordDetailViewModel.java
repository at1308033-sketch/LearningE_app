package com.learninge.app.ui.learn;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.learninge.app.core.Result;
import com.learninge.app.data.model.DeckSummary;
import com.learninge.app.data.model.WordDetail;
import com.learninge.app.ui.common.BaseViewModel;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Chi tiết một từ (US-008, US-011).
 */
public class WordDetailViewModel extends BaseViewModel {
    private final MutableLiveData<Result<WordDetail>> word = new MutableLiveData<>();

    private final MutableLiveData<List<DeckSummary>> targets = new MutableLiveData<>();
    private final MutableLiveData<Result<Long>> copied = new MutableLiveData<>();
    private final MutableLiveData<Result<Void>> deleted = new MutableLiveData<>();

    public WordDetailViewModel(@NonNull Application application) {
        super(application);
    }

    LiveData<Result<WordDetail>> word() {
        return word;
    }

    void load(long wordId) {
        services.executors().dbRead().execute(() -> word.postValue(services.deckService().getWord(wordId)));
    }

    LiveData<List<DeckSummary>> targets() {
        return targets;
    }

    LiveData<Result<Long>> copied() {
        return copied;
    }

    LiveData<Result<Void>> deleted() {
        return deleted;
    }

    /**
     * Các bộ của người dùng, để chọn nơi lưu từ.
     */
    void loadTargets() {
        services.executors().dbRead().execute(() -> {
            Result<List<DeckSummary>> decks = services.deckService().listMyDecks();
            targets.postValue(decks.isOk() ? decks.value.stream().filter(item -> !item.deck().isBuiltin())
                    .collect(Collectors.toList()) : List.of());
        });
    }

    void onTargetsShown() {
        targets.setValue(null);
    }

    void copyTo(long wordId, long deckId) {
        services.executors().dbWrite().execute(() ->
                copied.postValue(services.deckEditor().copyToDeck(wordId, deckId)));
    }

    void delete(long wordId) {
        services.executors().dbWrite().execute(() -> deleted.postValue(services.deckEditor().deleteWord(wordId)));
    }

    void toggleFavorite(long wordId) {
        services.executors().dbWrite().execute(() -> {
            services.deckService().toggleFavorite(wordId);
            load(wordId);
        });
    }
}
