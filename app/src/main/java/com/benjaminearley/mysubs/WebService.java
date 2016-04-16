package com.benjaminearley.mysubs;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WebService {

    private static final String REDDIT_BASE_URL = "https://www.reddit.com";
    private static WebService ourInstance = new WebService();
    private Retrofit retrofit;
    private RedditService redditService;

    private WebService() {
    }

    public static WebService getInstance() {
        return ourInstance;
    }

    private Retrofit getRetrofit() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(REDDIT_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public RedditService getRedditService() {
        if (redditService == null) {
            redditService = getRetrofit().create(RedditService.class);
        }
        return redditService;
    }
}
