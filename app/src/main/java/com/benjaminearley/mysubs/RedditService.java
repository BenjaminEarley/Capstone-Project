package com.benjaminearley.mysubs;

import com.benjaminearley.mysubs.model.Listing;
import com.benjaminearley.mysubs.model.Subreddit;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface RedditService {

    @GET("{subreddit}hot/.json")
    Call<Listing> getSubredditHotListing(@Path("subreddit") String subreddit);

    @GET("r/{subreddit}/about/.json")
    Call<Subreddit> getSubredditInfo(@Path("subreddit") String subreddit);

}
