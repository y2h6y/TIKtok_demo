package com.tiktok.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "videos")
data class VideoItem(
    @PrimaryKey val id: String,
    val coverUrl: String,           // 封面图URL
    val videoUrl: String,           // 视频URL
    val title: String,              // 标题
    val description: String,        // 描述
    val authorName: String,         // 作者名
    val authorAvatar: String,       // 作者头像
    val likeCount: Int,             // 点赞数
    val commentCount: Int,          // 评论数
    val shareCount: Int,            // 分享数
    val width: Int,                 // 原始宽度
    val height: Int,                // 原始高度（用于瀑布流）
    val category: String,           // 分类（推荐/关注/附近）
    val timestamp: Long,            // 缓存时间
    var isLiked: Boolean = false    // 是否已点赞
) : Serializable
