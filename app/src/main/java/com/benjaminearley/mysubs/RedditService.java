package com.benjaminearley.mysubs;

import com.benjaminearley.mysubs.model.Listing;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface RedditService {

    @GET("r/{subreddit}/hot/.json")
    Call<Listing> getSubredditHotListing(@Path("subreddit") String subreddit);

}
