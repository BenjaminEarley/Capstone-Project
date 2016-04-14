package com.benjaminearley.mysubs;

import com.benjaminearley.mysubs.model.Listing;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WebServices {

    private static final String REDDIT_BASE_URL = "https://www.reddit.com";
    private static WebServices ourInstance = new WebServices();
    private Retrofit retrofit;
    private RedditService redditService;

    private WebServices() {
    }

    public static WebServices getInstance() {
        return ourInstance;
    }

    public static Call<Listing> getSubredditHotListing(String subreddit) {
        return WebServices.getInstance().getRedditService().getSubredditHotListing(subreddit);
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

    private RedditService getRedditService() {
        if (redditService == null) {
            redditService = getRetrofit().create(RedditService.class);
        }
        return redditService;
    }
}
