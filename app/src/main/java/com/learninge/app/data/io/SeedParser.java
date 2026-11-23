package com.learninge.app.data.io;

import com.learninge.app.data.io.SeedContent.SeedDeck;
import com.learninge.app.data.io.SeedContent.SeedExam;
import com.learninge.app.data.io.SeedContent.SeedLanguage;
import com.learninge.app.data.io.SeedContent.SeedQuestion;
import com.learninge.app.data.io.SeedContent.SeedWord;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Đọc file seed JSON (DATABASE-DESIGN §Định dạng dữ liệu nạp sẵn). Thiếu trường bắt buộc thì báo lỗi, không đoán.
 */
public final class SeedParser {
    private static final int OPTION_COUNT = 4;

    private SeedParser() {
    }

    public static List<SeedLanguage> languages(String json) throws JSONException {
        JSONArray array = new JSONArray(json);
        List<SeedLanguage> result = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject item = array.getJSONObject(i);
            result.add(new SeedLanguage(item.getString("code"), item.getString("name_vi"),
                    item.getString("tts_locale"), item.getString("reading_label"), item.getInt("sort_order")));
        }
        return result;
    }

    public static SeedDeck deck(String json) throws JSONException {
        JSONObject root = new JSONObject(json);
        JSONObject deck = root.getJSONObject("deck");
        JSONArray words = root.getJSONArray("words");
        List<SeedWord> list = new ArrayList<>();
        for (int i = 0; i < words.length(); i++) {
            JSONObject word = words.getJSONObject(i);
            list.add(new SeedWord(word.getString("uid"), word.getString("term"), optional(word, "reading"),
                    optional(word, "pos"), word.getString("meaning"), optional(word, "example"),
                    optional(word, "example_translation")));
        }
        return new SeedDeck(deck.getString("code"), deck.getInt("version"), deck.getString("name"),
                optional(deck, "description"), deck.getString("language"), optional(deck, "level"),
                optional(deck, "icon"), optional(deck, "exam_tag"), deck.getInt("sort_order"),
                root.getString("source"), list);
    }

    public static List<SeedExam> exams(String json) throws JSONException {
        JSONArray tests = new JSONObject(json).getJSONArray("tests");
        List<SeedExam> result = new ArrayList<>();
        for (int i = 0; i < tests.length(); i++) {
            result.add(exam(tests.getJSONObject(i)));
        }
        return result;
    }

    private static SeedExam exam(JSONObject test) throws JSONException {
        JSONArray questions = test.getJSONArray("questions");
        List<SeedQuestion> list = new ArrayList<>();
        for (int i = 0; i < questions.length(); i++) {
            JSONObject question = questions.getJSONObject(i);
            JSONArray options = question.getJSONArray("options");
            int correct = question.getInt("correct_index");
            if (options.length() != OPTION_COUNT || correct < 0 || correct >= OPTION_COUNT) {
                throw new JSONException("question " + question.getInt("number") + ": needs 4 options");
            }
            list.add(new SeedQuestion(question.getInt("number"), optional(question, "passage"),
                    question.getString("question"), options.toString(), correct, optional(question, "explanation"),
                    optional(question, "deck_code"), optional(question, "term"), null));
        }
        return new SeedExam(test.getString("code"), test.optInt("version", 1), test.getString("exam_tag"),
                test.getString("title"), optional(test, "part_label"), test.getInt("duration_sec"),
                optional(test, "description"), test.optInt("sort_order", 0), list);
    }

    private static String optional(JSONObject object, String key) throws JSONException {
        return object.isNull(key) ? null : object.getString(key);
    }
}
