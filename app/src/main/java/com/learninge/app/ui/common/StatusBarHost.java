package com.learninge.app.ui.common;

/**
 * Activity quyết định màu icon thanh trạng thái, vì thanh "Đang ngoại tuyến" có thể che phần đầu màn hình.
 */
public interface StatusBarHost {

    void setDarkHeader(boolean isDark);
}
