package com.tiktok.app.data.db

import androidx.room.*
import com.tiktok.app.data.model.Comment

@Dao
interface CommentDao {
    
    @Query("SELECT * FROM comments WHERE videoId = :videoId ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    suspend fun getCommentsByVideoId(videoId: String, limit: Int, offset: Int): List<Comment>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComments(comments: List<Comment>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: Comment)
    
    @Query("DELETE FROM comments WHERE videoId = :videoId")
    suspend fun deleteCommentsByVideoId(videoId: String)
}
