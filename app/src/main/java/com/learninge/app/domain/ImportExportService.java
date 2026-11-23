package com.learninge.app.domain;

import android.database.SQLException;
import android.net.Uri;

import com.learninge.app.core.AppExecutors;
import com.learninge.app.core.ErrorCode;
import com.learninge.app.core.Result;
import com.learninge.app.core.SessionStore;
import com.learninge.app.data.dao.DeckDao;
import com.learninge.app.data.dao.WordDao;
import com.learninge.app.data.io.SafFiles;
import com.learninge.app.data.model.DeckSummary;
import com.learninge.app.data.model.Word;
import com.learninge.app.data.model.WordItem;
import com.learninge.app.data.repo.DeckRepository;
import com.learninge.app.domain.rules.CsvCodec;
import com.learninge.app.domain.rules.ImportPreview;
import com.learninge.app.domain.rules.TextNormalizer;
import com.learninge.app.domain.rules.WordInput;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Nhập/xuất CSV (API-SPECIFICATION §3.6). Gọi trên {@code io}; phần ghi CSDL tự chuyển sang {@code dbWrite}.
 */
public class ImportExportService {
    /**
     * Nhập vào bộ mới đặt theo tên tệp.
     */
    public static final long NEW_DECK = -1;

    private static final String DEFAULT_LANGUAGE = "en";
    private static final DateTimeFormatter FILE_DATE = DateTimeFormatter.BASIC_ISO_DATE;

    /**
     * @param skipped dòng trùng và dòng lỗi
     */
    public record ImportResult(long deckId, int imported, int skipped) {
    }

    private final SafFiles files;
    private final DeckDao decks;
    private final WordDao words;
    private final DeckRepository repository;
    private final DeckService service;
    private final SessionStore session;
    private final AppExecutors executors;
    private final Clock clock;

    public ImportExportService(SafFiles files, DeckDao decks, WordDao words, DeckRepository repository,
                               DeckService service, SessionStore session, AppExecutors executors, Clock clock) {
        this.files = files;
        this.decks = decks;
        this.words = words;
        this.repository = repository;
        this.service = service;
        this.session = session;
        this.executors = executors;
        this.clock = clock;
    }

    public Result<ImportPreview> preview(Uri file) {
        Result<String> text = files.readText(file, ImportPreview.MAX_BYTES);
        return text.isOk() ? preview(files.displayName(file), text.value) : Result.errorOf(text);
    }

    Result<ImportPreview> preview(String fileName, String csv) {
        try {
            List<CsvCodec.Row> rows = CsvCodec.parse(csv);
            return rows.size() > ImportPreview.MAX_ROWS ? Result.error(ErrorCode.FILE_TOO_LARGE)
                    : Result.ok(ImportPreview.of(fileName, rows));
        } catch (CsvCodec.FormatException wrong) {
            return Result.error(ErrorCode.FILE_FORMAT, wrong.getMessage());
        }
    }

    /**
     * Thêm mọi dòng hợp lệ trong một giao dịch; từ đã có trong bộ đích thì bỏ qua (BR-050).
     */
    public Result<ImportResult> importInto(ImportPreview preview, long targetDeckId) {
        if (preview.words().isEmpty()) {
            return Result.error(ErrorCode.NO_VALID_ROWS);
        }
        try {
            return executors.write(() -> repository.inTransaction(() -> insertAll(preview, targetDeckId)));
        } catch (SQLException failed) {
            return Result.error(ErrorCode.DB, failed.getMessage());
        }
    }

    /**
     * Trả số từ đã xuất; bộ có sẵn cũng xuất được (BR-051).
     */
    public Result<Integer> export(long deckId, Uri target) {
        Result<List<WordItem>> items = service.listWords(deckId);
        if (!items.isOk()) {
            return Result.errorOf(items);
        }
        Result<Void> written = files.writeText(target, CsvCodec.format(items.value.stream()
                .map(item -> input(item.word())).collect(Collectors.toList())));
        return written.isOk() ? Result.ok(items.value.size()) : Result.errorOf(written);
    }

    /**
     * Tên tệp gợi ý: {@code <tên bộ>_<yyyyMMdd>.csv}, bỏ ký tự hệ thống tệp không cho phép.
     */
    public String exportName(String deckName) {
        return deckName.replaceAll("[\\\\/:*?\"<>|]", "_") + "_" + FILE_DATE.format(LocalDate.now(clock)) + ".csv";
    }

    private Result<ImportResult> insertAll(ImportPreview preview, long targetDeckId) {
        long userId = session.userId();
        long now = clock.millis();
        long deckId = targetDeckId;
        String language = DEFAULT_LANGUAGE;
        if (targetDeckId == NEW_DECK) {
            String name = ImportPreview.freeName(preview.deckName(),
                    candidate -> decks.existsName(userId, TextNormalizer.normalize(candidate), DeckDao.NONE));
            deckId = decks.insert(userId, name, null, DEFAULT_LANGUAGE, now);
        } else {
            Result<DeckSummary> deck = service.getDeck(targetDeckId);
            if (!deck.isOk() || deck.value.deck().isBuiltin()) {
                return Result.error(deck.isOk() ? ErrorCode.READ_ONLY : deck.error);
            }
            language = deck.value.deck().languageCode();
        }
        int imported = 0;
        for (WordInput word : preview.words()) {
            if (words.insertIfAbsent(deckId, word, language, now)) {
                imported++;
            }
        }
        return Result.ok(new ImportResult(deckId, imported, preview.total() - imported));
    }

    private static WordInput input(Word word) {
        return new WordInput(word.term(), word.reading(), word.meaning(), word.example(), word.exampleTranslation());
    }
}
