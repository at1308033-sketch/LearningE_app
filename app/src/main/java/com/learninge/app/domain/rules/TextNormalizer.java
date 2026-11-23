package com.learninge.app.domain.rules;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Chuẩn hóa chữ để so sánh và tìm kiếm (BR-007, BR-040, BR-047, BR-053). Java thuần.
 */
public final class TextNormalizer {
    private static final Pattern MARKS = Pattern.compile("\\p{M}+");
    private static final Pattern SPACES = Pattern.compile("\\s+");

    private TextNormalizer() {
    }

    /**
     * Bỏ khoảng trắng thừa và chữ hoa, giữ dấu: dùng cho luật trùng từ/trùng tên bộ ("Apple" ≡ " apple").
     */
    public static String normalize(String text) {
        return SPACES.matcher(text.strip()).replaceAll(" ").toLowerCase(Locale.ROOT);
    }

    /**
     * Như {@link #normalize} và bỏ dấu: "Hà Nội" → "ha noi". Dùng cho tra từ và câu trả lời bảo mật.
     */
    public static String stripMarks(String text) {
        String decomposed = Normalizer.normalize(normalize(text), Normalizer.Form.NFD);
        // "đ" là chữ cái riêng, không phải d + dấu, nên NFD không tách được.
        return MARKS.matcher(decomposed).replaceAll("").replace('đ', 'd');
    }

    /**
     * Cột tìm kiếm của từ theo ngôn ngữ: tiếng Nhật/Trung/Hàn giữ nguyên dấu vì dấu là một phần của chữ (BR-053).
     */
    public static String searchKey(String text, String languageCode) {
        switch (languageCode) {
            case "ja":
            case "zh":
            case "ko":
                return Normalizer.normalize(normalize(text), Normalizer.Form.NFKC);
            default:
                return stripMarks(text);
        }
    }
}
