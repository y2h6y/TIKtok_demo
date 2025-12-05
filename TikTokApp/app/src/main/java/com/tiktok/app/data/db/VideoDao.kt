package com.tiktok.app.data.db

import androidx.room.*
import com.tiktok.app.data.model.VideoItem

@Dao
interface VideoDao {
    
    @Query("SELECT * FROM videos WHERE category = :category ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    suspend fun getVideosByCategory(category: String, limit: Int, offset: Int): List<VideoItem>
    
    @Query("SELECT * FROM videos WHERE id = :videoId")
    suspend fun getVideoById(videoId: String): VideoItem?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideos(videos: List<VideoItem>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: VideoItem)
    
    @Update
    suspend fun updateVideo(video: VideoItem)
    
    @Query("DELETE FROM videos WHERE category = :category")
    suspend fun deleteVideosByCategory(category: String)
    
    @Query("DELETE FROM videos WHERE timestamp < :timestamp")
    suspend fun deleteOldVideos(timestamp: Long)
}
