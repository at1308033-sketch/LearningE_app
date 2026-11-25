package com.learninge.app.data.remote;

import com.learninge.app.core.ErrorCode;
import com.learninge.app.core.Result;
import com.learninge.app.data.io.SeedContent.SeedDeck;
import com.learninge.app.data.io.SeedContent.SeedExam;
import com.learninge.app.data.io.SeedContent.SeedLanguage;
import com.learninge.app.data.io.SeedContent.SeedQuestion;
import com.learninge.app.data.io.SeedContent.SeedWord;
import com.learninge.proto.app.v1.ContentDeck;
import com.learninge.proto.app.v1.ContentExam;
import com.learninge.proto.app.v1.ContentServiceGrpc;
import com.learninge.proto.app.v1.ContentWord;
import com.learninge.proto.app.v1.ExamQuestion;
import com.learninge.proto.app.v1.GetDeckResponse;
import com.learninge.proto.app.v1.GetDecksRequest;
import com.learninge.proto.app.v1.GetDecksResponse;
import com.learninge.proto.app.v1.GetExamResponse;
import com.learninge.proto.app.v1.GetExamsRequest;
import com.learninge.proto.app.v1.GetExamsResponse;
import com.learninge.proto.app.v1.GetManifestRequest;
import com.learninge.proto.app.v1.GetManifestResponse;
import com.learninge.proto.app.v1.ManifestItem;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Gọi {@code learninge.app.v1.ContentService} (EP-14, EP-20, EP-21). Chạy trên luồng {@code io}.
 */
public class ContentRemote {
    /**
     * Số mã tối đa trong một lời gọi EP-20, EP-21.
     */
    public static final int MAX_CODES = 10;
    private static final int OPTION_COUNT = 4;

    /**
     * Một mục trong danh mục nội dung.
     */
    public record Item(String code, int version) {
    }

    /**
     * @param isNotModified bản trên máy đã mới nhất, các danh sách rỗng
     */
    public record Manifest(long version, boolean isNotModified, List<SeedLanguage> languages, List<Item> decks,
                           List<Item> exams) {
    }

    private final GrpcClient client;

    public ContentRemote(GrpcClient client) {
        this.client = client;
    }

    public Result<Manifest> manifest(long installedVersion) {
        GetManifestRequest request = GetManifestRequest.newBuilder().setIfVersion(installedVersion).build();
        Result<GetManifestResponse> response = client.call(Rpc.GET_MANIFEST, ContentServiceGrpc::newBlockingStub,
                stub -> stub.getManifest(request));
        if (!response.isOk()) {
            return Result.errorOf(response);
        }
        GetManifestResponse value = response.value;
        List<SeedLanguage> languages = value.getLanguagesList().stream().map(item -> new SeedLanguage(item.getCode(),
                        item.getNameVi(), item.getTtsLocale(), item.getReadingLabel(), item.getSortOrder()))
                .collect(Collectors.toList());
        return Result.ok(new Manifest(value.getContentVersion(), value.getNotModified(), languages,
                items(value.getDecksList()), items(value.getExamsList())));
    }

    /**
     * EP-20: tối đa {@link #MAX_CODES} bộ; thiếu bộ nào thì cả lời gọi là lỗi.
     */
    public Result<List<SeedDeck>> decks(List<String> codes) {
        GetDecksRequest request = GetDecksRequest.newBuilder().addAllCodes(codes).build();
        Result<GetDecksResponse> response = client.call(Rpc.GET_DECKS, ContentServiceGrpc::newBlockingStub,
                stub -> stub.getDecks(request));
        return all(response, codes, response.isOk() ? response.value.getDecksList() : null, ContentRemote::deck);
    }

    /**
     * EP-21: như {@link #decks}.
     */
    public Result<List<SeedExam>> exams(List<String> codes) {
        GetExamsRequest request = GetExamsRequest.newBuilder().addAllCodes(codes).build();
        Result<GetExamsResponse> response = client.call(Rpc.GET_EXAMS, ContentServiceGrpc::newBlockingStub,
                stub -> stub.getExams(request));
        return all(response, codes, response.isOk() ? response.value.getExamsList() : null, ContentRemote::exam);
    }

    private static <T, R> Result<List<R>> all(Result<?> response, List<String> codes, List<T> items,
                                              Function<T, Result<R>> convert) {
        if (!response.isOk()) {
            return Result.errorOf(response);
        }
        if (items.size() != codes.size()) {
            return Result.error(ErrorCode.NOT_FOUND);
        }
        List<R> converted = new ArrayList<>();
        for (T item : items) {
            Result<R> result = convert.apply(item);
            if (!result.isOk()) {
                return Result.errorOf(result);
            }
            converted.add(result.value);
        }
        return Result.ok(converted);
    }

    private static Result<SeedDeck> deck(GetDeckResponse response) {
        ContentDeck deck = response.getDeck();
        // Từ đã gỡ khỏi bộ không có trong danh sách nên ContentWriter tự ẩn.
        List<SeedWord> words = response.getWordsList().stream().filter(word -> !word.getDeleted())
                .map(ContentRemote::word).collect(Collectors.toList());
        if (deck.getCode().isEmpty() || words.isEmpty()) {
            return Result.error(ErrorCode.BAD_RESPONSE);
        }
        return Result.ok(new SeedDeck(deck.getCode(), deck.getVersion(), deck.getName(),
                deck.hasDescription() ? deck.getDescription() : null, deck.getLanguageCode(),
                deck.hasLevel() ? deck.getLevel() : null, deck.hasIcon() ? deck.getIcon() : null,
                deck.hasExamTag() ? deck.getExamTag() : null, deck.getSortOrder(), "server", words,
                deck.getRetired()));
    }

    private static Result<SeedExam> exam(GetExamResponse response) {
        ContentExam exam = response.getExam();
        List<SeedQuestion> questions = new ArrayList<>();
        for (ExamQuestion question : response.getQuestionsList()) {
            if (question.getOptionsCount() != OPTION_COUNT) {
                return Result.error(ErrorCode.BAD_RESPONSE);
            }
            questions.add(question(question));
        }
        if (exam.getCode().isEmpty() || questions.isEmpty()) {
            return Result.error(ErrorCode.BAD_RESPONSE);
        }
        return Result.ok(new SeedExam(exam.getCode(), exam.getVersion(), exam.getExamTag(), exam.getTitle(),
                exam.hasPartLabel() ? exam.getPartLabel() : null, exam.getDurationSec(),
                exam.hasDescription() ? exam.getDescription() : null, exam.getSortOrder(), questions,
                exam.getRetired()));
    }

    private static List<Item> items(List<ManifestItem> items) {
        return items.stream().map(item -> new Item(item.getCode(), item.getVersion())).collect(Collectors.toList());
    }

    private static SeedWord word(ContentWord word) {
        return new SeedWord(word.getUid(), word.getTerm(), word.hasReading() ? word.getReading() : null,
                word.hasPartOfSpeech() ? word.getPartOfSpeech() : null, word.getMeaningVi(),
                word.hasExample() ? word.getExample() : null,
                word.hasExampleTranslation() ? word.getExampleTranslation() : null);
    }

    private static SeedQuestion question(ExamQuestion question) {
        return new SeedQuestion(question.getNumber(), question.hasPassage() ? question.getPassage() : null,
                question.getQuestion(), new JSONArray(question.getOptionsList()).toString(),
                question.getCorrectIndex(), question.hasExplanation() ? question.getExplanation() : null, null, null,
                question.hasWordUid() ? question.getWordUid() : null);
    }
}
