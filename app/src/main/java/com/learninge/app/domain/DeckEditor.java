package com.learninge.app.domain;

import android.database.SQLException;

import com.learninge.app.core.ErrorCode;
import com.learninge.app.core.Result;
import com.learninge.app.core.SessionStore;
import com.learninge.app.data.dao.DeckDao;
import com.learninge.app.data.dao.WordDao;
import com.learninge.app.data.model.Deck;
import com.learninge.app.data.model.DeckSummary;
import com.learninge.app.data.model.WordDetail;
import com.learninge.app.data.repo.DeckRepository;
import com.learninge.app.domain.rules.DeckRules;
import com.learninge.app.domain.rules.TextNormalizer;
import com.learninge.app.domain.rules.WordInput;

import java.time.Clock;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Phần ghi của {@code DeckService} (API-SPECIFICATION §3.2): tạo/sửa/xóa bộ và từ của người dùng. Gọi trên
 * {@code dbWrite}. Lỗi VALIDATION có {@code detail} là các trường sai, vd "NAME".
 */
public class DeckEditor {
    private final DeckDao decks;
    private final WordDao words;
    private final DeckRepository repository;
    private final DeckService service;
    private final SessionStore session;
    private final Clock clock;

    public DeckEditor(DeckDao decks, WordDao words, DeckRepository repository, DeckService service,
                      SessionStore session, Clock clock) {
        this.decks = decks;
        this.words = words;
        this.repository = repository;
        this.service = service;
        this.session = session;
        this.clock = clock;
    }

    /**
     * Trả khóa bộ mới (BR-044).
     */
    public Result<Long> createDeck(String name, String description, String languageCode) {
        return checked(DeckRules.checkDeck(name, description), () -> {
            String clean = DeckRules.clean(name);
            if (decks.existsName(session.userId(), TextNormalizer.normalize(clean), DeckDao.NONE)) {
                return Result.error(ErrorCode.DUPLICATE);
            }
            return Result.ok(decks.insert(session.userId(), clean, DeckRules.clean(description), languageCode,
                    clock.millis()));
        });
    }

    /**
     * Ngôn ngữ chỉ đổi được khi bộ chưa có từ (BR-052).
     */
    public Result<Void> updateDeck(long deckId, String name, String description, String languageCode) {
        return checked(DeckRules.checkDeck(name, description), () -> {
            Result<DeckSummary> deck = editable(deckId);
            if (!deck.isOk()) {
                return Result.errorOf(deck);
            }
            boolean isLanguageChanged = !deck.value.deck().languageCode().equals(languageCode);
            if (isLanguageChanged && deck.value.total() > 0) {
                return Result.error(ErrorCode.READ_ONLY);
            }
            String clean = DeckRules.clean(name);
            if (decks.existsName(session.userId(), TextNormalizer.normalize(clean), deckId)) {
                return Result.error(ErrorCode.DUPLICATE);
            }
            decks.update(deckId, clean, DeckRules.clean(description), languageCode, clock.millis());
            return Result.ok(null);
        });
    }

    /**
     * Bộ mặc định "Sổ tay của tôi" không xóa được (BR-045).
     */
    public Result<Void> deleteDeck(long deckId) {
        return guarded(() -> {
            Result<DeckSummary> deck = editable(deckId);
            if (!deck.isOk()) {
                return Result.errorOf(deck);
            }
            if (deck.value.deck().isDefault()) {
                return Result.error(ErrorCode.READ_ONLY);
            }
            repository.delete(deckId, clock.millis());
            return Result.ok(null);
        });
    }

    /**
     * Trả khóa từ mới (BR-047).
     */
    public Result<Long> addWord(long deckId, WordInput input) {
        return checked(DeckRules.checkWord(input), () -> {
            Result<DeckSummary> deck = editable(deckId);
            return deck.isOk() ? insert(deck.value.deck(), input) : Result.errorOf(deck);
        });
    }

    public Result<Void> updateWord(long wordId, WordInput input) {
        return checked(DeckRules.checkWord(input), () -> {
            Result<WordDetail> word = editableWord(wordId);
            if (!word.isOk()) {
                return Result.errorOf(word);
            }
            Deck deck = word.value.deck();
            if (words.existsTerm(deck.id(), searchTerm(deck, input), wordId)) {
                return Result.error(ErrorCode.DUPLICATE);
            }
            words.update(wordId, input, deck.languageCode(), clock.millis());
            return Result.ok(null);
        });
    }

    public Result<Void> deleteWord(long wordId) {
        return guarded(() -> {
            Result<WordDetail> word = editableWord(wordId);
            if (!word.isOk()) {
                return Result.errorOf(word);
            }
            words.markDeleted(wordId, clock.millis());
            return Result.ok(null);
        });
    }

    /**
     * Chép một từ bất kỳ sang bộ của mình (AC-030.4).
     */
    public Result<Long> copyToDeck(long wordId, long targetDeckId) {
        return guarded(() -> {
            Result<WordDetail> source = service.getWord(wordId);
            if (!source.isOk()) {
                return Result.errorOf(source);
            }
            Result<DeckSummary> target = editable(targetDeckId);
            if (!target.isOk()) {
                return Result.errorOf(target);
            }
            var word = source.value.item().word();
            return insert(target.value.deck(), new WordInput(word.term(), word.reading(), word.meaning(),
                    word.example(), word.exampleTranslation()));
        });
    }

    private Result<Long> insert(Deck deck, WordInput input) {
        if (words.existsTerm(deck.id(), searchTerm(deck, input), WordDao.NONE)) {
            return Result.error(ErrorCode.DUPLICATE);
        }
        return Result.ok(words.insert(deck.id(), input, deck.languageCode(), clock.millis()));
    }

    private Result<DeckSummary> editable(long deckId) {
        Result<DeckSummary> deck = service.getDeck(deckId);
        return deck.isOk() && deck.value.deck().isBuiltin() ? Result.error(ErrorCode.READ_ONLY) : deck;
    }

    private Result<WordDetail> editableWord(long wordId) {
        Result<WordDetail> word = service.getWord(wordId);
        return word.isOk() && word.value.deck().isBuiltin() ? Result.error(ErrorCode.READ_ONLY) : word;
    }

    private static String searchTerm(Deck deck, WordInput input) {
        return TextNormalizer.searchKey(DeckRules.clean(input.term()), deck.languageCode());
    }

    private <T> Result<T> checked(Set<DeckRules.Field> errors, Supplier<Result<T>> body) {
        return errors.isEmpty() ? guarded(body) : Result.error(ErrorCode.VALIDATION,
                errors.stream().map(Enum::name).collect(Collectors.joining(",")));
    }

    private <T> Result<T> guarded(Supplier<Result<T>> body) {
        try {
            return repository.inTransaction(body);
        } catch (SQLException failed) {
            return Result.error(ErrorCode.DB, failed.getMessage());
        }
    }
}
