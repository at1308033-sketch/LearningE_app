package com.learninge.app.data.io;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Bốn lựa chọn của câu thi, lưu dạng mảng JSON trong {@code exam_questions.options_json}.
 */
public final class OptionsJson {

    private OptionsJson() {
    }

    public static List<String> parse(String json) {
        try {
            JSONArray array = new JSONArray(json);
            List<String> options = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                options.add(array.getString(i));
            }
            return options;
        } catch (JSONException broken) {
            // Seed và nội dung tải về đều đã kiểm tra trước khi ghi.
            throw new IllegalStateException(broken);
        }
    }
}
