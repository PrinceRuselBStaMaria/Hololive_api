package com.example.final_api.api;

import com.example.final_api.model.LiveStream;
import com.example.final_api.model.VTuber;
import com.example.final_api.model.Video;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface HolodexApi {
    // Original search endpoints
    @GET("channels")
    Call<List<VTuber>> searchVTubers(
        @Query("org") String org,
        @Query("type") String type,
        @Query("limit") int limit,
        @Query("offset") int offset,
        @Query("name") String query
    );
    
    @GET("channels")
    Call<List<VTuber>> getAllHololiveMembers(
        @Query("org") String org,
        @Query("type") String type,
        @Query("limit") int limit
    );
    
    // New endpoints for enhanced detail view
    
    // Get detailed channel info
    @GET("channels/{channelId}")
    Call<VTuber> getChannelDetails(@Path("channelId") String channelId);
    
    // Get recent videos
    @GET("videos")
    Call<List<Video>> getRecentVideos(
        @Query("channel_id") String channelId,
        @Query("type") String type,
        @Query("limit") int limit
    );
    
    // Get upcoming streams
    @GET("videos")
    Call<List<Video>> getUpcomingStreams(
        @Query("channel_id") String channelId,
        @Query("status") String status,
        @Query("limit") int limit
    );
    
    // Get live status
    @GET("live")
    Call<List<Video>> getLiveStreamsByChannel(
        @Query("channel_id") String channelId
    );
    
    // Get all live streams for an organization
    @GET("live")
    Call<List<LiveStream>> getLiveStreamsByOrg(
    );
    
    // Get clips featuring the VTuber
    @GET("clips")
    Call<List<Video>> getClips(
        @Query("channel_id") String channelId,
        @Query("limit") int limit
    );
    
    // Get videos with collaborations
    @GET("videos")
    Call<List<Video>> getCollaborations(
        @Query("channel_id") String channelId,
        @Query("include") String include,
        @Query("limit") int limit
    );
    
    // Get all live and upcoming streams for Hololive
    @GET("videos")
    Call<List<LiveStream>> getLiveAndUpcomingStreams(
        @Query("org") String org,
        @Query("status") String status,
        @Query("type") String type,
        @Query("sort") String sort,
        @Query("order") String order,
        @Query("max_upcoming_hours") int maxUpcomingHours,
        @Query("limit") int limit,
        @Query("include") String include
    );
    
    // Get all currently live streams (all orgs, including Hololive)
    @GET("live")
    Call<List<LiveStream>> getAllLiveStreams();
}
