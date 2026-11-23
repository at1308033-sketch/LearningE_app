package com.learninge.app.domain.rules;

import java.util.ArrayList;
import java.util.List;

/**
 * Đọc/ghi CSV từ vựng theo RFC 4180 (BR-048, DEC-008). Java thuần.
 */
public final class CsvCodec {
    public static final List<String> HEADER = List.of("term", "reading", "meaning", "example", "example_translation");

    private static final char BOM = '\uFEFF';
    private static final char QUOTE = '"';

    /**
     * Một bản ghi và số dòng bắt đầu của nó trong tệp (dòng đầu tiên là 1).
     */
    public record Row(int line, List<String> cells) {
    }

    /**
     * Tệp không đúng định dạng; thông điệp hiện được cho người dùng.
     */
    public static final class FormatException extends Exception {
        public FormatException(String message) {
            super(message);
        }
    }

    private CsvCodec() {
    }

    /**
     * Các dòng dữ liệu, không gồm dòng tiêu đề và dòng trống.
     */
    public static List<Row> parse(String text) throws FormatException {
        String content = !text.isEmpty() && text.charAt(0) == BOM ? text.substring(1) : text;
        List<Row> rows = split(content);
        rows.removeIf(row -> row.cells().size() == 1 && row.cells().get(0).isBlank());
        if (rows.isEmpty() || !HEADER.equals(trimmed(rows.get(0).cells()))) {
            throw new FormatException("dòng tiêu đề phải là " + String.join(",", HEADER));
        }
        return new ArrayList<>(rows.subList(1, rows.size()));
    }

    /**
     * Nội dung tệp xuất, có BOM để Excel mở đúng tiếng Việt.
     */
    public static String format(List<WordInput> words) {
        StringBuilder out = new StringBuilder().append(BOM);
        appendRow(out, HEADER);
        for (WordInput word : words) {
            appendRow(out, List.of(text(word.term()), text(word.reading()), text(word.meaning()),
                    text(word.example()), text(word.exampleTranslation())));
        }
        return out.toString();
    }

    private static List<Row> split(String content) throws FormatException {
        List<Row> rows = new ArrayList<>();
        List<String> cells = new ArrayList<>();
        StringBuilder cell = new StringBuilder();
        boolean isQuoted = false;
        int line = 1;
        int rowLine = 1;
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '\n') {
                line++;
            }
            if (isQuoted) {
                boolean isEscaped = c == QUOTE && i + 1 < content.length() && content.charAt(i + 1) == QUOTE;
                if (isEscaped) {
                    cell.append(QUOTE);
                    i++;
                } else if (c == QUOTE) {
                    isQuoted = false;
                } else {
                    cell.append(c);
                }
            } else if (c == QUOTE && cell.length() == 0) {
                isQuoted = true;
            } else if (c == ',') {
                cells.add(take(cell));
            } else if (c == '\n') {
                cells.add(take(cell));
                rows.add(new Row(rowLine, cells));
                cells = new ArrayList<>();
                rowLine = line;
            } else if (c != '\r') {
                cell.append(c);
            }
        }
        if (isQuoted) {
            throw new FormatException("dòng " + rowLine + " thiếu dấu nháy đóng");
        }
        if (cell.length() > 0 || !cells.isEmpty()) {
            cells.add(take(cell));
            rows.add(new Row(rowLine, cells));
        }
        return rows;
    }

    private static String take(StringBuilder cell) {
        String value = cell.toString();
        cell.setLength(0);
        return value;
    }

    private static List<String> trimmed(List<String> cells) {
        List<String> result = new ArrayList<>();
        cells.forEach(cell -> result.add(cell.strip()));
        return result;
    }

    private static void appendRow(StringBuilder out, List<String> cells) {
        for (int i = 0; i < cells.size(); i++) {
            if (i > 0) {
                out.append(',');
            }
            out.append(escape(cells.get(i)));
        }
        out.append("\r\n");
    }

    private static String escape(String cell) {
        boolean needsQuotes = cell.indexOf(',') >= 0 || cell.indexOf(QUOTE) >= 0 || cell.indexOf('\n') >= 0
                || cell.indexOf('\r') >= 0 || !cell.equals(cell.strip());
        return needsQuotes ? QUOTE + cell.replace("\"", "\"\"") + QUOTE : cell;
    }

    private static String text(String value) {
        return value == null ? "" : value;
    }
}
