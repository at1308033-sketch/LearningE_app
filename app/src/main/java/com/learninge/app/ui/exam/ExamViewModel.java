package com.learninge.app.ui.exam;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.learninge.app.core.ErrorCode;
import com.learninge.app.core.Result;
import com.learninge.app.data.model.ExamQuestion;
import com.learninge.app.data.model.ExamSession;
import com.learninge.app.ui.common.BaseViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Lượt thi đang làm; mỗi câu được lưu ngay khi chọn (US-036).
 */
public class ExamViewModel extends BaseViewModel {

    record State(ExamSession session, int index, ErrorCode error) {

        ExamQuestion question() {
            return session.questions().get(index);
        }
    }

    /**
     * @param isTimeUp true nếu hệ thống tự nộp vì hết giờ
     */
    record Finished(long attemptId, boolean isTimeUp) {
    }

    private final MutableLiveData<State> state = new MutableLiveData<>();
    private final MutableLiveData<Finished> finished = new MutableLiveData<>();
    private boolean isSubmitting;

    public ExamViewModel(@NonNull Application application) {
        super(application);
    }

    LiveData<State> state() {
        return state;
    }

    LiveData<Finished> finished() {
        return finished;
    }

    void start(String testCode) {
        if (state.getValue() != null) {
            return;
        }
        services.executors().dbWrite().execute(() -> {
            Result<ExamSession> session = services.examService().startOrResume(testCode);
            if (session.error == ErrorCode.TIME_UP) {
                onTimeUp(session);
            } else {
                state.postValue(new State(session.value, 0, session.error));
            }
        });
    }

    void goTo(int index) {
        State current = state.getValue();
        if (current != null && current.session() != null && index >= 0
                && index < current.session().questions().size()) {
            state.setValue(new State(current.session(), index, null));
        }
    }

    void select(int option) {
        State current = state.getValue();
        if (current != null && current.session() != null) {
            save(current, option, current.question().isFlagged());
        }
    }

    void toggleFlag() {
        State current = state.getValue();
        if (current != null && current.session() != null) {
            save(current, current.question().selectedIndex(), !current.question().isFlagged());
        }
    }

    long remainingMillis() {
        State current = state.getValue();
        return current == null || current.session() == null ? 0
                : services.examService().remainingMillis(current.session().attempt());
    }

    void submit(boolean isTimeUp) {
        State current = state.getValue();
        if (current == null || current.session() == null || isSubmitting) {
            return;
        }
        isSubmitting = true;
        long attemptId = current.session().attempt().id();
        services.executors().dbWrite().execute(() -> {
            Result<ExamSession> result = services.examService().submit(attemptId);
            if (result.isOk()) {
                services.syncScheduler().onActivityFinished();
                finished.postValue(new Finished(attemptId, isTimeUp));
            } else {
                isSubmitting = false;
                state.postValue(new State(current.session(), current.index(), result.error));
            }
        });
    }

    // Hiện lựa chọn ngay rồi mới ghi xuống CSDL.
    private void save(State current, Integer option, boolean isFlagged) {
        ExamQuestion old = current.question();
        ExamQuestion changed = new ExamQuestion(old.id(), old.number(), old.passage(), old.question(), old.options(),
                old.correctIndex(), old.explanation(), old.wordId(), option, isFlagged);
        List<ExamQuestion> questions = new ArrayList<>(current.session().questions());
        questions.set(current.index(), changed);
        state.setValue(new State(new ExamSession(current.session().attempt(), questions), current.index(), null));
        services.executors().dbWrite().execute(() -> {
            Result<Void> saved = services.examService().answer(current.session().attempt().id(), old.number(), option,
                    isFlagged);
            if (saved.error == ErrorCode.TIME_UP) {
                onTimeUp(saved);
            }
        });
    }

    private void onTimeUp(Result<?> result) {
        services.syncScheduler().onActivityFinished();
        finished.postValue(new Finished(Long.parseLong(result.detail), true));
    }
}
