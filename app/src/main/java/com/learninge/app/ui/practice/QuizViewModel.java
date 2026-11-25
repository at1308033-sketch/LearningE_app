package com.learninge.app.ui.practice;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.learninge.app.core.Result;
import com.learninge.app.data.model.Word;
import com.learninge.app.data.model.WordItem;
import com.learninge.app.ui.common.BaseViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * SCR-13 Quiz chủ đề: 1 câu/lần, 4 đáp án, tự động next sau 1.5s.
 */
public class QuizViewModel extends BaseViewModel {

    static final int SESSION_SIZE = 10;
    private static final long AUTO_NEXT_MS = 1500;

    record Question(String term, String reading, List<String> options, int correctIndex) {}

    record State(Question question, int index, int total, Integer selectedIndex, Boolean isCorrect) {}

    record Finished(int correct, int total, String deckName) {}

    private final MutableLiveData<State> state = new MutableLiveData<>();
    private final MutableLiveData<Finished> finished = new MutableLiveData<>();
    private final Handler handler = new Handler(Looper.getMainLooper());

    private List<Question> questions;
    private int current;
    private int correctCount;
    private boolean answered;
    private String deckName;
    private long deckId;

    public QuizViewModel(@NonNull Application application) {
        super(application);
    }

    LiveData<State> state() { return state; }
    LiveData<Finished> finished() { return finished; }

    void start(long deckId, String deckName) {
        this.deckId = deckId;
        this.deckName = deckName;
        services.executors().dbRead().execute(() -> {
            Result<List<WordItem>> result = services.deckService().listWords(deckId);
            if (!result.isOk() || result.value.size() < 2) {
                // 미만 2개면 quiz 불가 — finish immediately with 0
                finished.postValue(new Finished(0, 0, deckName));
                return;
            }
            List<WordItem> words = new ArrayList<>(result.value);
            Collections.shuffle(words);
            int size = Math.min(SESSION_SIZE, words.size());
            List<Question> qs = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                qs.add(buildQuestion(words, i, words));
            }
            questions = qs;
            current = 0;
            correctCount = 0;
            answered = false;
            postCurrent();
        });
    }

    private Question buildQuestion(List<WordItem> sessionWords, int idx, List<WordItem> allWords) {
        Word correct = sessionWords.get(idx).word();
        // Gather 3 distractors from other words in deck (excluding current)
        List<String> distractors = new ArrayList<>();
        for (int i = 0; i < allWords.size() && distractors.size() < 3; i++) {
            if (i != idx) {
                String meaning = allWords.get(i).word().meaning();
                if (meaning != null && !meaning.isBlank()) {
                    distractors.add(meaning);
                }
            }
        }
        while (distractors.size() < 3) {
            distractors.add("—"); // fallback if deck has very few words
        }
        List<String> options = new ArrayList<>(distractors.subList(0, 3));
        // Insert correct answer at random position
        int correctPos = (int) (Math.random() * 4);
        options.add(correctPos, correct.meaning() != null ? correct.meaning() : "—");
        return new Question(correct.term(), correct.reading(), options, correctPos);
    }

    void select(int optionIndex) {
        if (answered || questions == null) return;
        answered = true;
        boolean isCorrect = optionIndex == questions.get(current).correctIndex();
        if (isCorrect) correctCount++;
        state.setValue(new State(questions.get(current), current, questions.size(), optionIndex, isCorrect));
        handler.postDelayed(this::next, AUTO_NEXT_MS);
    }

    private void next() {
        if (questions == null) return;
        current++;
        if (current >= questions.size()) {
            finished.setValue(new Finished(correctCount, questions.size(), deckName));
            return;
        }
        answered = false;
        postCurrent();
    }

    private void postCurrent() {
        state.postValue(new State(questions.get(current), current, questions.size(), null, null));
    }

    @Override
    protected void onCleared() {
        handler.removeCallbacksAndMessages(null);
        super.onCleared();
    }
}
