package com.learninge.app.ui.study;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.learninge.app.core.ErrorCode;
import com.learninge.app.core.Result;
import com.learninge.app.data.model.ActivityType;
import com.learninge.app.data.model.SessionSummary;
import com.learninge.app.data.model.WordDetail;
import com.learninge.app.domain.StudyService;
import com.learninge.app.ui.common.BaseViewModel;

import java.util.List;

/**
 * Một phiên thẻ; giữ trạng thái qua xoay màn hình.
 */
public class FlashcardViewModel extends BaseViewModel {

    /**
     * @param error null khi đang học bình thường
     */
    record State(List<WordDetail> cards, int index, boolean isFlipped, ErrorCode error) {

        WordDetail card() {
            return cards.get(index);
        }
    }

    private final MutableLiveData<State> state = new MutableLiveData<>();
    private final MutableLiveData<SessionSummary> finished = new MutableLiveData<>();
    private StudySource source;
    private long deckId;
    private int remembered;
    private int forgotten;
    private int streakBefore;
    private List<Integer> boxesBefore;
    private boolean isSaving;

    public FlashcardViewModel(@NonNull Application application) {
        super(application);
    }

    LiveData<State> state() {
        return state;
    }

    LiveData<SessionSummary> finished() {
        return finished;
    }

    void start(StudySource chosenSource, long chosenDeckId) {
        if (source != null) {
            return;
        }
        source = chosenSource;
        deckId = chosenSource == StudySource.NEW ? chosenDeckId : StudyService.NO_DECK;
        services.executors().dbRead().execute(() -> {
            StudyService study = services.studyService();
            Result<List<WordDetail>> cards = source == StudySource.NEW ? study.newSession(deckId)
                    : study.reviewSession();
            boxesBefore = study.boxCounts();
            streakBefore = study.currentStreak();
            state.postValue(new State(cards.isOk() ? cards.value : List.of(), 0, false, cards.error));
        });
    }

    void flip() {
        State current = state.getValue();
        if (current != null && current.error() == null) {
            state.setValue(new State(current.cards(), current.index(), !current.isFlipped(), null));
        }
    }

    void rate(boolean isRemembered) {
        State current = state.getValue();
        if (current == null || !current.isFlipped() || isSaving) {
            return;
        }
        isSaving = true;
        services.executors().dbWrite().execute(() -> {
            Result<Integer> saved = services.studyService().rate(current.card().item().word().id(), isRemembered);
            services.executors().main().execute(() -> onRated(current, isRemembered, saved));
        });
    }

    private void onRated(State current, boolean isRemembered, Result<Integer> saved) {
        isSaving = false;
        if (!saved.isOk()) {
            state.setValue(new State(current.cards(), current.index(), true, saved.error));
            state.setValue(current);
            return;
        }
        if (isRemembered) {
            remembered++;
        } else {
            forgotten++;
        }
        if (current.index() + 1 < current.cards().size()) {
            state.setValue(new State(current.cards(), current.index() + 1, false, null));
        } else {
            finish();
        }
    }

    private void finish() {
        isSaving = true;
        ActivityType kind = source == StudySource.NEW ? ActivityType.LEARN : ActivityType.REVIEW;
        services.executors().dbWrite().execute(() -> {
            Result<SessionSummary> summary = services.studyService().finishSession(kind, deckId, remembered,
                    forgotten, streakBefore, boxesBefore);
            if (summary.isOk()) {
                finished.postValue(summary.value);
            } else {
                state.postValue(new State(List.of(), 0, false, summary.error));
            }
        });
    }
}
