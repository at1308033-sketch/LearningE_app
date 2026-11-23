package com.learninge.app.ui.deck;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.learninge.app.core.Result;
import com.learninge.app.data.model.DeckSummary;
import com.learninge.app.domain.ImportExportService.ImportResult;
import com.learninge.app.domain.rules.ImportPreview;
import com.learninge.app.ui.common.BaseViewModel;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Xem trước và nhập tệp CSV (SCR-28).
 */
public class ImportViewModel extends BaseViewModel {

    /**
     * @param targets các bộ của người dùng có thể nhập vào
     */
    record State(Result<ImportPreview> preview, List<DeckSummary> targets) {
    }

    private final MutableLiveData<State> state = new MutableLiveData<>();
    private final MutableLiveData<Result<ImportResult>> imported = new MutableLiveData<>();

    public ImportViewModel(@NonNull Application application) {
        super(application);
    }

    LiveData<State> state() {
        return state;
    }

    LiveData<Result<ImportResult>> imported() {
        return imported;
    }

    void load(Uri file) {
        if (state.getValue() != null) {
            return;
        }
        services.executors().io().execute(() -> {
            Result<ImportPreview> preview = services.importExportService().preview(file);
            Result<List<DeckSummary>> decks = services.deckService().listMyDecks();
            state.postValue(new State(preview, decks.isOk() ? decks.value.stream()
                    .filter(item -> !item.deck().isBuiltin()).collect(Collectors.toList()) : List.of()));
        });
    }

    void importInto(ImportPreview preview, long deckId) {
        services.executors().io().execute(() ->
                imported.postValue(services.importExportService().importInto(preview, deckId)));
    }
}
