package com.tiktok.app.data.model

data class ApiResponse<T>(
    val code: Int,
    val message: String,
    val data: T?
)

data class VideoListResponse(
    val videos: List<VideoItem>,
    val hasMore: Boolean,
    val nextPage: Int
)

data class CommentListResponse(
    val comments: List<Comment>,
    val hasMore: Boolean,
    val nextPage: Int
)
