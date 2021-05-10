package com.septmb.septmb.septmb.provider;

/**
 * Created by sonback123456 on 9/18/2017.
 */

import android.content.SearchRecentSuggestionsProvider;

/**
 * Created by sodha on 4/3/16.
 */
public class SuggestionProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "com.septmb.septmb.septmb.provider.SuggestionProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public SuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}
