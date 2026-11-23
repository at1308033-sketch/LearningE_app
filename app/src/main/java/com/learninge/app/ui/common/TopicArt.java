package com.learninge.app.ui.common;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;

import com.learninge.app.R;
import com.learninge.app.data.model.Deck;

import java.util.Map;

/**
 * Icon và màu ô của bộ từ. Dùng bảng thay vì tra theo tên để R8 không cắt mất tài nguyên.
 */
public final class TopicArt {

    private record Art(@DrawableRes int icon, @ColorRes int tile) {
    }

    private static final Map<String, Art> TOPICS = Map.ofEntries(
            Map.entry("t01-family", new Art(R.drawable.ic_topic_t01_family, R.color.topic_tile_t01)),
            Map.entry("t02-numbers-time", new Art(R.drawable.ic_topic_t02_numbers_time, R.color.topic_tile_t02)),
            Map.entry("t03-colors-shapes", new Art(R.drawable.ic_topic_t03_colors_shapes, R.color.topic_tile_t03)),
            Map.entry("t04-body", new Art(R.drawable.ic_topic_t04_body, R.color.topic_tile_t04)),
            Map.entry("t05-food-drinks", new Art(R.drawable.ic_topic_t05_food_drinks, R.color.topic_tile_t05)),
            Map.entry("t06-animals", new Art(R.drawable.ic_topic_t06_animals, R.color.topic_tile_t06)),
            Map.entry("t07-house-home", new Art(R.drawable.ic_topic_t07_house_home, R.color.topic_tile_t07)),
            Map.entry("t08-school", new Art(R.drawable.ic_topic_t08_school, R.color.topic_tile_t08)),
            Map.entry("t09-clothes-shopping", new Art(R.drawable.ic_topic_t09_clothes_shopping,
                    R.color.topic_tile_t09)),
            Map.entry("t10-weather-seasons", new Art(R.drawable.ic_topic_t10_weather_seasons, R.color.topic_tile_t10)),
            Map.entry("t11-city-transport", new Art(R.drawable.ic_topic_t11_city_transport, R.color.topic_tile_t11)),
            Map.entry("t12-hobbies-sports", new Art(R.drawable.ic_topic_t12_hobbies_sports, R.color.topic_tile_t12)),
            Map.entry("t13-work-jobs", new Art(R.drawable.ic_topic_t13_work_jobs, R.color.topic_tile_t13)),
            Map.entry("t14-travel", new Art(R.drawable.ic_topic_t14_travel, R.color.topic_tile_t14)),
            Map.entry("t15-health", new Art(R.drawable.ic_topic_t15_health, R.color.topic_tile_t15)));
    private static final Art DEFAULT = new Art(R.drawable.ic_book_open, R.color.feat_learn_container);

    private TopicArt() {
    }

    @DrawableRes
    public static int icon(Deck deck) {
        return of(deck).icon;
    }

    @ColorRes
    public static int tile(Deck deck) {
        return of(deck).tile;
    }

    private static Art of(Deck deck) {
        return deck.icon() == null ? DEFAULT : TOPICS.getOrDefault(deck.icon(), DEFAULT);
    }
}
