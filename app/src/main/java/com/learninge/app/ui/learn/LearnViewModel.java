package com.learninge.app.ui.learn;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.learninge.app.core.Result;
import com.learninge.app.data.model.DeckSummary;
import com.learninge.app.domain.DeckService;
import com.learninge.app.ui.common.BaseViewModel;

import java.util.List;

/**
 * Chủ đề theo cấp độ đã chọn (US-007).
 */
public class LearnViewModel extends BaseViewModel {

    /**
     * @param level null = tất cả
     */
    record State(String level, Result<List<DeckSummary>> topics) {
    }

    private final MutableLiveData<State> state = new MutableLiveData<>();
    private String level;
    private boolean isLevelChosen;

    public LearnViewModel(@NonNull Application application) {
        super(application);
    }

    LiveData<State> state() {
        return state;
    }

    void chooseLevel(String chosen) {
        level = chosen;
        isLevelChosen = true;
        load();
    }

    /**
     * Đọc lại mỗi khi màn hình hiện để số "đã thuộc" luôn mới. Lần đầu lọc theo cấp độ trong cài đặt.
     */
    void load() {
        services.executors().dbRead().execute(() -> {
            DeckService decks = services.deckService();
            if (!isLevelChosen) {
                level = decks.userLevel();
                isLevelChosen = true;
            }
            state.postValue(new State(level, decks.listTopics(level)));
        });
    }
}
