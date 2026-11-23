package com.learninge.app.domain.rules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Kết quả kiểm tra tệp CSV trước khi nhập (BR-050). Java thuần.
 *
 * @param words      các dòng hợp lệ, không trùng nhau trong tệp
 * @param duplicates số dòng trùng từ với dòng trước trong tệp
 * @param errors     dòng lỗi kèm lý do
 */
public record ImportPreview(String deckName, int total, List<WordInput> words, int duplicates, List<RowError> errors) {
    public static final int MAX_BYTES = 1024 * 1024;
    public static final int MAX_ROWS = 2000;

    /**
     * @param reason trường sai đầu tiên, hoặc null nếu thiếu cột
     */
    public record RowError(int line, DeckRules.Field reason) {
    }

    /**
     * {@code fileName} "tu_vung.csv" thành tên bộ "tu_vung" (cắt còn 50 ký tự).
     */
    public static ImportPreview of(String fileName, List<CsvCodec.Row> rows) {
        List<WordInput> words = new ArrayList<>();
        List<RowError> errors = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        int duplicates = 0;
        for (CsvCodec.Row row : rows) {
            if (row.cells().size() != CsvCodec.HEADER.size()) {
                errors.add(new RowError(row.line(), null));
                continue;
            }
            WordInput word = new WordInput(row.cells().get(0), row.cells().get(1), row.cells().get(2),
                    row.cells().get(3), row.cells().get(4));
            Set<DeckRules.Field> wrong = DeckRules.checkWord(word);
            if (!wrong.isEmpty()) {
                errors.add(new RowError(row.line(), wrong.iterator().next()));
            } else if (!seen.add(TextNormalizer.normalize(word.term()))) {
                duplicates++;
            } else {
                words.add(word);
            }
        }
        return new ImportPreview(deckName(fileName), rows.size(), words, duplicates, errors);
    }

    private static String deckName(String fileName) {
        int dot = fileName.lastIndexOf('.');
        String name = (dot > 0 ? fileName.substring(0, dot) : fileName).strip();
        int[] points = name.codePoints().limit(DeckRules.MAX_NAME).toArray();
        String shortName = new String(points, 0, points.length).strip();
        return shortName.isEmpty() ? "Bộ nhập từ tệp" : shortName;
    }

    /**
     * Tên chưa dùng: thêm " (2)", " (3)"… và vẫn không quá 50 ký tự.
     */
    public static String freeName(String name, Predicate<String> isTaken) {
        String candidate = name;
        for (int n = 2; isTaken.test(candidate); n++) {
            String suffix = " (" + n + ")";
            int room = DeckRules.MAX_NAME - suffix.length();
            int[] points = name.codePoints().limit(room).toArray();
            candidate = new String(points, 0, points.length).strip() + suffix;
        }
        return candidate;
    }

    /**
     * Năm dòng đầu để xem trước.
     */
    public List<WordInput> sample() {
        return words.subList(0, Math.min(5, words.size()));
    }
}
