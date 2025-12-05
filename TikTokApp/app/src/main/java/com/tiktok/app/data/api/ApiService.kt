package com.tiktok.app.data.api

import com.tiktok.app.data.model.ApiResponse
import com.tiktok.app.data.model.Comment
import com.tiktok.app.data.model.CommentListResponse
import com.tiktok.app.data.model.VideoListResponse
import retrofit2.http.*

interface ApiService {
    
    /**
     * 获取视频列表
     * @param category 分类：recommend（推荐）、following（关注）、nearby（附近）
     * @param page 页码
     * @param size 每页数量
     */
    @GET("videos")
    suspend fun getVideos(
        @Query("category") category: String,
        @Query("page") page: Int,
        @Query("size") size: Int = 20
    ): ApiResponse<VideoListResponse>
    
    /**
     * 获取评论列表
     */
    @GET("comments/{videoId}")
    suspend fun getComments(
        @Path("videoId") videoId: String,
        @Query("page") page: Int,
        @Query("size") size: Int = 20
    ): ApiResponse<CommentListResponse>
    
    /**
     * 发布评论
     */
    @POST("comments")
    suspend fun postComment(@Body comment: Comment): ApiResponse<Comment>
    
    /**
     * 点赞视频
     */
    @POST("videos/{videoId}/like")
    suspend fun likeVideo(@Path("videoId") videoId: String): ApiResponse<Any>
    
    /**
     * 取消点赞
     */
    @DELETE("videos/{videoId}/like")
    suspend fun unlikeVideo(@Path("videoId") videoId: String): ApiResponse<Any>
}
