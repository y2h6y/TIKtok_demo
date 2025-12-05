package com.tiktok.app.data.repository

import com.tiktok.app.data.api.ApiService
import com.tiktok.app.data.db.CommentDao
import com.tiktok.app.data.model.Comment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class CommentRepository(
    private val apiService: ApiService,
    private val commentDao: CommentDao
) {
    
    /**
     * 获取评论列表
     */
    suspend fun getComments(
        videoId: String,
        page: Int,
        size: Int = 20
    ): Result<List<Comment>> = withContext(Dispatchers.IO) {
        try {
            // 先尝试从本地获取
            if (page == 0) {
                val cachedComments = commentDao.getCommentsByVideoId(videoId, size, 0)
                if (cachedComments.isNotEmpty()) {
                    return@withContext Result.success(cachedComments)
                }
            }
            
            // 从网络获取
            val response = apiService.getComments(videoId, page, size)
            if (response.code == 200 && response.data != null) {
                val comments = response.data.comments
                // 缓存到本地
                if (page == 0) {
                    commentDao.deleteCommentsByVideoId(videoId)
                }
                commentDao.insertComments(comments)
                Result.success(comments)
            } else {
                // 网络失败，从本地获取
                val cachedComments = commentDao.getCommentsByVideoId(videoId, size, page * size)
                if (cachedComments.isNotEmpty()) {
                    Result.success(cachedComments)
                } else {
                    Result.failure(Exception(response.message))
                }
            }
        } catch (e: Exception) {
            // 异常处理，尝试本地
            val cachedComments = commentDao.getCommentsByVideoId(videoId, size, page * size)
            if (cachedComments.isNotEmpty()) {
                Result.success(cachedComments)
            } else {
                Result.failure(e)
            }
        }
    }
    
    /**
     * 发布评论
     */
    suspend fun postComment(
        videoId: String,
        content: String,
        userId: String = "user_${System.currentTimeMillis()}",
        userName: String = "用户${(1000..9999).random()}",
        avatarUrl: String = "https://picsum.photos/100"
    ): Result<Comment> = withContext(Dispatchers.IO) {
        try {
            val comment = Comment(
                id = UUID.randomUUID().toString(),
                videoId = videoId,
                userId = userId,
                userName = userName,
                avatarUrl = avatarUrl,
                content = content,
                timestamp = System.currentTimeMillis()
            )
            
            // 先保存到本地
            commentDao.insertComment(comment)
            
            // 尝试上传到服务器
            try {
                val response = apiService.postComment(comment)
                if (response.code == 200 && response.data != null) {
                    Result.success(response.data)
                } else {
                    Result.success(comment) // 即使上传失败，本地也保存了
                }
            } catch (e: Exception) {
                Result.success(comment) // 网络失败不影响本地显示
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
