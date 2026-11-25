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
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
        Result<List<SeedDeck>> decks = download("deck:", manifest.value.decks(), remote.get()::decks);
        if (!decks.isOk()) {
            return Result.errorOf(decks);
        }
        Result<List<SeedExam>> exams = download("exam:", manifest.value.exams(), remote.get()::exams);
        if (!exams.isOk()) {
            return Result.errorOf(exams);
        }
        try {
            executors.write(() -> {
                content.install(manifest.value.version(), manifest.value.languages(), decks.value, exams.value);
                return null;
            });
            return Result.ok(decks.value.size() + exams.value.size());
        } catch (SQLException failed) {
            return Result.error(ErrorCode.DB, failed.getMessage());
        }
    }

    /**
     * Tải các mục mới hơn bản trên máy, mỗi lời gọi tối đa {@link ContentRemote#MAX_CODES} mục.
     */
    private <T> Result<List<T>> download(String keyPrefix, List<Item> items,
                                         Function<List<String>, Result<List<T>>> fetch) {
        List<String> codes = items.stream().filter(item -> content.isNewer(keyPrefix + item.code(), item.version()))
                .map(Item::code).collect(Collectors.toList());
        List<T> downloaded = new ArrayList<>();
        for (int from = 0; from < codes.size(); from += ContentRemote.MAX_CODES) {
            int to = Math.min(codes.size(), from + ContentRemote.MAX_CODES);
            Result<List<T>> part = fetch.apply(codes.subList(from, to));
            if (!part.isOk()) {
                return part;
            }
            downloaded.addAll(part.value);
        }
        return Result.ok(downloaded);
    }
}
