package com.learninge.app.data.remote;

import java.util.List;

/**
 * Trả lời cho một lô đã đẩy (EP-11).
 *
 * @param results   cùng thứ tự với bản ghi đã gửi
 * @param dataEpoch kỷ nguyên dữ liệu của máy chủ, 0 nếu máy chủ bản cũ không gửi
 */
public record PushReply(List<PushResult> results, long dataEpoch) {
}
