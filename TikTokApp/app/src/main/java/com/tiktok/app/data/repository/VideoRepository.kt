package com.tiktok.app.data.repository

import com.tiktok.app.data.api.ApiService
import com.tiktok.app.data.db.VideoDao
import com.tiktok.app.data.model.VideoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VideoRepository(
    private val apiService: ApiService,
    private val videoDao: VideoDao
) {
    
    /**
     * 获取视频列表（网络优先，失败时使用缓存）
     */
    suspend fun getVideos(
        category: String,
        page: Int,
        size: Int = 20,
        forceRefresh: Boolean = false
    ): Result<List<VideoItem>> = withContext(Dispatchers.IO) {
        try {
            // 如果不是强制刷新且page=0，先尝试从本地获取
            if (!forceRefresh && page == 0) {
                val cachedVideos = videoDao.getVideosByCategory(category, size, 0)
                if (cachedVideos.isNotEmpty()) {
                    return@withContext Result.success(cachedVideos)
                }
            }
            
            // 从网络获取
            val response = apiService.getVideos(category, page, size)
            if (response.code == 200 && response.data != null) {
                val videos = response.data.videos
                // 如果是第一页，清空旧数据
                if (page == 0) {
                    videoDao.deleteVideosByCategory(category)
                }
                // 缓存到本地
                videoDao.insertVideos(videos)
                Result.success(videos)
            } else {
                // 网络失败，尝试从本地获取
                val cachedVideos = videoDao.getVideosByCategory(category, size, page * size)
                if (cachedVideos.isNotEmpty()) {
                    Result.success(cachedVideos)
                } else {
                    Result.failure(Exception(response.message))
                }
            }
        } catch (e: Exception) {
            // 网络异常，尝试从本地获取
            val cachedVideos = videoDao.getVideosByCategory(category, size, page * size)
            if (cachedVideos.isNotEmpty()) {
                Result.success(cachedVideos)
            } else {
                Result.failure(e)
            }
        }
    }
    
    /**
     * 获取单个视频详情
     */
    suspend fun getVideoById(videoId: String): VideoItem? = withContext(Dispatchers.IO) {
        videoDao.getVideoById(videoId)
    }
    
    /**
     * 更新视频（点赞等）
     */
    suspend fun updateVideo(video: VideoItem) = withContext(Dispatchers.IO) {
        videoDao.updateVideo(video)
    }
    
    /**
     * 点赞视频
     */
    suspend fun likeVideo(videoId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.likeVideo(videoId)
            if (response.code == 200) {
                // 更新本地数据库
                val video = videoDao.getVideoById(videoId)
                video?.let {
                    val updatedVideo = it.copy(
                        isLiked = true,
                        likeCount = it.likeCount + 1
                    )
                    videoDao.updateVideo(updatedVideo)
                }
                Result.success(true)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 取消点赞
     */
    suspend fun unlikeVideo(videoId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.unlikeVideo(videoId)
            if (response.code == 200) {
                val video = videoDao.getVideoById(videoId)
                video?.let {
                    val updatedVideo = it.copy(
                        isLiked = false,
                        likeCount = maxOf(0, it.likeCount - 1)
                    )
                    videoDao.updateVideo(updatedVideo)
                }
                Result.success(true)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
