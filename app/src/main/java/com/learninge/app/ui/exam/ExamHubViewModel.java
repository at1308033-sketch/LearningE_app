package com.learninge.app.ui.exam;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.learninge.app.core.Result;
import com.learninge.app.data.model.DeckSummary;
import com.learninge.app.data.model.ExamTest;
import com.learninge.app.ui.common.BaseViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Bộ từ và đề thi gom theo kỳ thi (US-035).
 */
public class ExamHubViewModel extends BaseViewModel {
    private static final List<String> EXAM_TAGS = List.of("TOEIC", "IELTS", "APTIS");

    record Group(String tag, List<DeckSummary> decks, List<ExamTest> tests) {
    }

    private final MutableLiveData<Result<List<Group>>> groups = new MutableLiveData<>();

    public ExamHubViewModel(@NonNull Application application) {
        super(application);
    }

    LiveData<Result<List<Group>>> groups() {
        return groups;
    }

    void load() {
        services.executors().dbRead().execute(() -> {
            Result<List<DeckSummary>> decks = services.deckService().listBuiltinDecks(null, null);
            Result<List<ExamTest>> tests = services.examService().listTests();
            if (!decks.isOk() || !tests.isOk()) {
                groups.postValue(Result.errorOf(decks.isOk() ? tests : decks));
                return;
            }
            List<Group> result = new ArrayList<>();
            for (String tag : EXAM_TAGS) {
                result.add(new Group(tag, decks.value.stream().filter(item -> tag.equals(item.deck().examTag()))
                        .collect(Collectors.toList()), tests.value.stream()
                        .filter(test -> tag.equals(test.examTag())).collect(Collectors.toList())));
            }
            groups.postValue(Result.ok(result));
        });
    }
}
