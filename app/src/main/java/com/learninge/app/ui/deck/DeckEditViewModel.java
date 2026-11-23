package com.learninge.app.ui.deck;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.learninge.app.core.Result;
import com.learninge.app.data.model.DeckSummary;
import com.learninge.app.data.model.Language;
import com.learninge.app.domain.StudyService;
import com.learninge.app.ui.common.BaseViewModel;

import java.util.List;

/**
 * Tạo/sửa bộ (SCR-27).
 */
public class DeckEditViewModel extends BaseViewModel {

    /**
     * @param deck null khi tạo mới
     */
    record Form(List<Language> languages, DeckSummary deck) {
    }

    private final MutableLiveData<Form> form = new MutableLiveData<>();
    private final MutableLiveData<Result<Void>> saved = new MutableLiveData<>();

    public DeckEditViewModel(@NonNull Application application) {
        super(application);
    }

    LiveData<Form> form() {
        return form;
    }

    LiveData<Result<Void>> saved() {
        return saved;
    }

    void load(long deckId) {
        services.executors().dbRead().execute(() -> {
            Result<DeckSummary> deck = deckId == StudyService.NO_DECK ? Result.ok(null)
                    : services.deckService().getDeck(deckId);
            form.postValue(new Form(services.languages().all(), deck.value));
        });
    }

    void save(long deckId, String name, String description, String languageCode) {
        services.executors().dbWrite().execute(() -> {
            if (deckId != StudyService.NO_DECK) {
                saved.postValue(services.deckEditor().updateDeck(deckId, name, description, languageCode));
                return;
            }
            Result<Long> created = services.deckEditor().createDeck(name, description, languageCode);
            saved.postValue(created.isOk() ? Result.ok(null) : Result.errorOf(created));
        });
    }
}
