package com.learninge.app.domain;

import com.learninge.app.data.dao.LanguageDao;
import com.learninge.app.data.model.Language;

import java.util.List;

/**
 * Danh mục ngôn ngữ nội dung (BR-052), đọc một lần vì chỉ đổi khi nạp seed. Gọi trên {@code dbRead}.
 */
public class LanguageRegistry {
    private final LanguageDao languages;
    private List<Language> cached = List.of();

    public LanguageRegistry(LanguageDao languages) {
        this.languages = languages;
    }

    public synchronized List<Language> all() {
        if (cached.isEmpty()) {
            cached = List.copyOf(languages.listAll());
        }
        return cached;
    }

    /**
     * null nếu mã lạ.
     */
    public Language get(String code) {
        return all().stream().filter(language -> language.code().equals(code)).findFirst().orElse(null);
    }
}
