package com.learninge.app.data.model;

/**
 * Một máy đang đăng nhập tài khoản (EP-17). Thời điểm là epoch millis.
 *
 * @param deviceName rỗng nếu máy chủ không có tên
 * @param lastUsedAt chính xác tới một ngày; null nếu chưa dùng lại sau khi đăng nhập
 * @param isCurrent  chính máy này
 */
public record DeviceSession(long id, String deviceName, long createdAt, Long lastUsedAt, boolean isCurrent) {
}
