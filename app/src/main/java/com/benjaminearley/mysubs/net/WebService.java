package com.benjaminearley.mysubs.net;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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

            Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(REDDIT_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
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
