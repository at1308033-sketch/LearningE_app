package com.learninge.app.ui.me;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.learninge.app.core.ErrorCode;
import com.learninge.app.core.Result;
import com.learninge.app.data.model.DeviceSession;
import com.learninge.app.domain.AuthService;
import com.learninge.app.ui.common.BaseViewModel;

import java.util.List;
import java.util.function.Function;

/**
 * Các máy đang đăng nhập tài khoản.
 */
public class SessionsViewModel extends BaseViewModel {

    /**
     * @param sessions null nếu chưa tải được lần nào
     * @param error    lỗi của thao tác gần nhất, null nếu thành công
     */
    record State(boolean isBusy, List<DeviceSession> sessions, ErrorCode error) {
    }

    private final MutableLiveData<State> state = new MutableLiveData<>(new State(false, null, null));

    public SessionsViewModel(@NonNull Application application) {
        super(application);
    }

    LiveData<State> state() {
        return state;
    }

    void load() {
        run(auth -> Result.ok(null));
    }

    void revoke(long id) {
        run(auth -> auth.revokeSession(id));
    }

    void revokeOthers() {
        run(AuthService::revokeOtherSessions);
    }

    // Làm xong luôn tải lại danh sách, kể cả khi lỗi: máy khác có thể vừa đăng nhập hoặc đăng xuất.
    private void run(Function<AuthService, Result<Void>> action) {
        List<DeviceSession> shown = state.getValue().sessions();
        state.setValue(new State(true, shown, null));
        services.executors().io().execute(() -> {
            AuthService auth = services.authService();
            Result<Void> done = action.apply(auth);
            Result<List<DeviceSession>> fresh = auth.sessions();
            state.postValue(new State(false, fresh.isOk() ? fresh.value : shown,
                    done.isOk() ? fresh.error : done.error));
        });
    }
}
