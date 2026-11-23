package com.learninge.app.domain;

import android.database.SQLException;

import com.learninge.app.core.AppExecutors;
import com.learninge.app.core.ErrorCode;
import com.learninge.app.core.Result;
import com.learninge.app.data.io.SeedContent.SeedDeck;
import com.learninge.app.data.io.SeedContent.SeedExam;
import com.learninge.app.data.remote.ContentRemote;
import com.learninge.app.data.remote.ContentRemote.Item;
import com.learninge.app.data.remote.ContentRemote.Manifest;
import com.learninge.app.data.repo.ContentRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Tải bộ từ, đề thi mới từ server (US-044). Lỗi thì giữ seed hoặc bản đã tải, lần mở app sau thử lại.
 */
public class ContentService {
    private final Supplier<ContentRemote> remote;
    private final ContentRepository content;
    private final AppExecutors executors;

    public ContentService(Supplier<ContentRemote> remote, ContentRepository content, AppExecutors executors) {
        this.remote = remote;
        this.content = content;
        this.executors = executors;
    }

    /**
     * Gọi trên {@code io}. Trả số mục (bộ, đề) đã cập nhật.
     */
    public Result<Integer> update() {
        Result<Manifest> manifest = remote.get().manifest(content.installedVersion());
        if (!manifest.isOk()) {
            return Result.errorOf(manifest);
        }
        if (manifest.value.isNotModified()) {
            return Result.ok(0);
        }
        // Tải đủ mọi mục trước rồi mới ghi, để không bao giờ có nội dung dở dang.
        List<SeedDeck> decks = new ArrayList<>();
        for (Item item : manifest.value.decks()) {
            if (content.isNewer("deck:" + item.code(), item.version())) {
                Result<SeedDeck> deck = remote.get().deck(item.code());
                if (!deck.isOk()) {
                    return Result.errorOf(deck);
                }
                decks.add(deck.value);
            }
        }
        List<SeedExam> exams = new ArrayList<>();
        for (Item item : manifest.value.exams()) {
            if (content.isNewer("exam:" + item.code(), item.version())) {
                Result<SeedExam> exam = remote.get().exam(item.code());
                if (!exam.isOk()) {
                    return Result.errorOf(exam);
                }
                exams.add(exam.value);
            }
        }
        try {
            executors.write(() -> {
                content.install(manifest.value.version(), manifest.value.languages(), decks, exams);
                return null;
            });
            return Result.ok(decks.size() + exams.size());
        } catch (SQLException failed) {
            return Result.error(ErrorCode.DB, failed.getMessage());
        }
    }
}
