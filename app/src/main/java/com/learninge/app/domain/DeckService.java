package com.learninge.app.domain;

import android.database.SQLException;

import com.learninge.app.core.ErrorCode;
import com.learninge.app.core.Result;
import com.learninge.app.core.SessionStore;
import com.learninge.app.data.dao.DeckDao;
import com.learninge.app.data.dao.FavoriteDao;
import com.learninge.app.data.dao.SettingsDao;
import com.learninge.app.data.dao.WordDao;
import com.learninge.app.data.model.DeckSummary;
import com.learninge.app.data.model.Settings;
import com.learninge.app.data.model.WordDetail;
import com.learninge.app.data.model.WordItem;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Bộ từ và từ (API-SPECIFICATION §3.2). Phương thức đọc gọi trên {@code dbRead}, phương thức ghi trên {@code dbWrite}.
 */
public class DeckService {
    // Chủ đề là bộ có sẵn mã Txx; Xxx là bộ luyện thi, Jxx là bộ tiếng Nhật.
    private static final String TOPIC_PREFIX = "T";

    private final DeckDao decks;
    private final WordDao words;
    private final SettingsDao settings;
    private final FavoriteDao favorites;
    private final LanguageRegistry languages;
    private final SessionStore session;
    private final Clock clock;

    public DeckService(DeckDao decks, WordDao words, SettingsDao settings, FavoriteDao favorites,
                       LanguageRegistry languages, SessionStore session, Clock clock) {
        this.decks = decks;
        this.words = words;
        this.settings = settings;
        this.favorites = favorites;
        this.languages = languages;
        this.session = session;
        this.clock = clock;
    }

    /**
     * Mọi bộ có sẵn; tham số null = không lọc.
     */
    public Result<List<DeckSummary>> listBuiltinDecks(String level, String examTag) {
        try {
            return Result.ok(decks.listBuiltin(session.userId(), today(), level, examTag));
        } catch (SQLException failed) {
            return Result.error(ErrorCode.DB, failed.getMessage());
        }
    }

    /**
     * Chủ đề của SCR-06 (US-007).
     */
    public Result<List<DeckSummary>> listTopics(String level) {
        Result<List<DeckSummary>> all = listBuiltinDecks(level, null);
        return all.isOk() ? Result.ok(all.value.stream()
                .filter(item -> item.deck().code() != null && item.deck().code().startsWith(TOPIC_PREFIX))
                .collect(Collectors.toList())) : all;
    }

    /**
     * Cấp độ trong cài đặt, là bộ lọc mặc định của SCR-06 (AC-007.2).
     */
    public String userLevel() {
        Settings found = settings.find(session.userId());
        return found == null ? null : found.level();
    }

    public Result<DeckSummary> getDeck(long deckId) {
        try {
            DeckSummary found = decks.findSummary(session.userId(), today(), deckId);
            return found == null ? Result.error(ErrorCode.NOT_FOUND) : Result.ok(found);
        } catch (SQLException failed) {
            return Result.error(ErrorCode.DB, failed.getMessage());
        }
    }

    public Result<List<WordItem>> listWords(long deckId) {
        Result<DeckSummary> deck = getDeck(deckId);
        return deck.isOk() ? Result.ok(words.listByDeck(session.userId(), deckId)) : Result.errorOf(deck);
    }

    public Result<WordDetail> getWord(long wordId) {
        try {
            WordItem item = words.findById(session.userId(), wordId);
            if (item == null) {
                return Result.error(ErrorCode.NOT_FOUND);
            }
            Result<DeckSummary> deck = getDeck(item.word().deckId());
            return deck.isOk() ? Result.ok(new WordDetail(item, deck.value.deck(),
                    languages.get(deck.value.deck().languageCode()))) : Result.errorOf(deck);
        } catch (SQLException failed) {
            return Result.error(ErrorCode.DB, failed.getMessage());
        }
    }

    /**
     * Đổi trạng thái yêu thích, trả trạng thái mới (US-011). Một câu lệnh nên không cần giao dịch riêng.
     */
    public Result<Boolean> toggleFavorite(long wordId) {
        try {
            long userId = session.userId();
            if (words.findById(userId, wordId) == null) {
                return Result.error(ErrorCode.NOT_FOUND);
            }
            boolean isFavorite = !favorites.isFavorite(userId, wordId);
            favorites.set(userId, wordId, isFavorite, clock.millis());
            return Result.ok(isFavorite);
        } catch (SQLException failed) {
            return Result.error(ErrorCode.DB, failed.getMessage());
        }
    }

    private String today() {
        return LocalDate.now(clock).toString();
    }
}
