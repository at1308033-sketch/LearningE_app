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
import com.learninge.proto.app.v1.GetDeckRequest;
import com.learninge.proto.app.v1.GetDeckResponse;
import com.learninge.proto.app.v1.GetExamRequest;
import com.learninge.proto.app.v1.GetExamResponse;
import com.learninge.proto.app.v1.GetManifestRequest;
import com.learninge.proto.app.v1.GetManifestResponse;
import com.learninge.proto.app.v1.ManifestItem;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Gọi {@code learninge.app.v1.ContentService} (EP-14..EP-16). Chạy trên luồng {@code io}.
 */
public class ContentRemote {
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

    public Result<SeedDeck> deck(String code) {
        GetDeckRequest request = GetDeckRequest.newBuilder().setCode(code).build();
        Result<GetDeckResponse> response = client.call(Rpc.GET_DECK, ContentServiceGrpc::newBlockingStub,
                stub -> stub.getDeck(request));
        if (!response.isOk()) {
            return Result.errorOf(response);
        }
        ContentDeck deck = response.value.getDeck();
        // Từ đã gỡ khỏi bộ không có trong danh sách nên ContentWriter tự ẩn.
        List<SeedWord> words = response.value.getWordsList().stream().filter(word -> !word.getDeleted())
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

    public Result<SeedExam> exam(String code) {
        GetExamRequest request = GetExamRequest.newBuilder().setCode(code).build();
        Result<GetExamResponse> response = client.call(Rpc.GET_EXAM, ContentServiceGrpc::newBlockingStub,
                stub -> stub.getExam(request));
        if (!response.isOk()) {
            return Result.errorOf(response);
        }
        ContentExam exam = response.value.getExam();
        List<SeedQuestion> questions = new ArrayList<>();
        for (ExamQuestion question : response.value.getQuestionsList()) {
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
