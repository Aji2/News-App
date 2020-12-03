package com.example.newsapp.repository.localStorageNewsDeprecated;

import android.provider.BaseColumns;

public class NewsReaderContract {

    private NewsReaderContract() {
    }

    public static class NewsEntry implements BaseColumns {
        public static final String TABLE_NAME = "saved_news";
        public static final String COLUMN_NAME_ARTICLE_JSON = "article_json";
        public static final String COLUMN_NAME_ARTICLE_TITLE = "article_title";
        public static final String COLUMN_NAME_ARTICLE_URL = "article_url";
    }
}