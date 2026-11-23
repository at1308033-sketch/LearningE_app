package com.learninge.app.core;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/**
 * Báo cho giao diện biết máy chủ đã kết thúc phiên (BR-059). Giá trị null = không có gì chờ xử lý.
 */
public final class SessionEvents {
    private final MutableLiveData<ErrorCode> ended = new MutableLiveData<>();

    public LiveData<ErrorCode> ended() {
        return ended;
    }

    public void publish(ErrorCode reason) {
        ended.postValue(reason);
    }

    /**
     * Gọi trên luồng giao diện sau khi đã chuyển màn hình, để không báo lại.
     */
    public void consume() {
        ended.setValue(null);
    }
}
