package com.tiktok.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "comments")
data class Comment(
    @PrimaryKey val id: String,
    val videoId: String,        // 关联的视频ID
    val userId: String,         // 用户ID
    val userName: String,       // 用户名
    val avatarUrl: String,      // 头像URL
    val content: String,        // 评论内容
    val timestamp: Long,        // 评论时间
    val likeCount: Int = 0      // 评论点赞数
)
