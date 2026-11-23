package com.learninge.app.ui.deck;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.learninge.app.core.Result;
import com.learninge.app.data.model.DeckSummary;
import com.learninge.app.data.model.Language;
import com.learninge.app.data.model.Word;
import com.learninge.app.data.model.WordDetail;
import com.learninge.app.domain.rules.WordInput;
import com.learninge.app.ui.common.BaseViewModel;

/**
 * Thêm/sửa từ (SCR-20).
 */
public class WordEditViewModel extends BaseViewModel {
    static final long NEW_WORD = -1;

    /**
     * @param word null khi thêm mới
     */
    record Form(DeckSummary deck, Language language, Word word) {
    }

    private final MutableLiveData<Form> form = new MutableLiveData<>();
    private final MutableLiveData<Result<Void>> saved = new MutableLiveData<>();

    public WordEditViewModel(@NonNull Application application) {
        super(application);
    }

    LiveData<Form> form() {
        return form;
    }

    LiveData<Result<Void>> saved() {
        return saved;
    }

    void load(long deckId, long wordId) {
        services.executors().dbRead().execute(() -> {
            Result<DeckSummary> deck = services.deckService().getDeck(deckId);
            if (!deck.isOk()) {
                saved.postValue(Result.errorOf(deck));
                return;
            }
            Result<WordDetail> word = wordId == NEW_WORD ? Result.ok(null) : services.deckService().getWord(wordId);
            form.postValue(new Form(deck.value, services.languages().get(deck.value.deck().languageCode()),
                    word.value == null ? null : word.value.item().word()));
        });
    }

    void save(long deckId, long wordId, WordInput input) {
        services.executors().dbWrite().execute(() -> {
            if (wordId != NEW_WORD) {
                saved.postValue(services.deckEditor().updateWord(wordId, input));
                return;
            }
            Result<Long> created = services.deckEditor().addWord(deckId, input);
            saved.postValue(created.isOk() ? Result.ok(null) : Result.errorOf(created));
        });
    }
}
