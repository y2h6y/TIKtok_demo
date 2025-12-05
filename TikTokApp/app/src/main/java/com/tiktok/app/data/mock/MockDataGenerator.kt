package com.tiktok.app.data.mock

import com.tiktok.app.data.model.Comment
import com.tiktok.app.data.model.VideoItem
import java.util.UUID

/**
 * Mock数据生成器
 * 用于生成测试数据，模拟从dmock获取的数据
 */
object MockDataGenerator {
    
    // 使用Unsplash和Pexels的免费图片/视频
    private val coverUrls = listOf(
        "https://picsum.photos/400/600?random=1",
        "https://picsum.photos/400/700?random=2",
        "https://picsum.photos/400/550?random=3",
        "https://picsum.photos/400/650?random=4",
        "https://picsum.photos/400/600?random=5",
        "https://picsum.photos/400/720?random=6",
        "https://picsum.photos/400/580?random=7",
        "https://picsum.photos/400/640?random=8",
        "https://picsum.photos/400/620?random=9",
        "https://picsum.photos/400/680?random=10"
    )
    
    // 测试视频URL列表 - 使用模拟器能解码的简单视频
    private val videoUrls = listOf(
        // 使用之前测试成功的视频（模拟器兼容性最好）
        "https://www.w3schools.com/html/mov_bbb.mp4",
        "https://www.w3schools.com/html/movie.mp4",
        "https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4",
        "https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.webm",
        "https://www.w3schools.com/html/mov_bbb.mp4",  // 重复几次以便测试滑动
        "https://www.w3schools.com/html/movie.mp4",
        "https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4",
        "https://www.w3schools.com/html/mov_bbb.mp4",
        "https://www.w3schools.com/html/movie.mp4",
        "https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4"
    )
    
    private val titles = listOf(
        "太美了！这个地方一定要去",
        "今天的vlog分享给大家",
        "教你做最简单的美食",
        "这个技巧太实用了！",
        "我的日常生活记录",
        "分享一个超酷的发现",
        "旅行中的美好瞬间",
        "学会这个你就是大神",
        "超级治愈的一天",
        "必看！这个太有趣了"
    )
    
    private val descriptions = listOf(
        "真的太好看了，大家一定要试试看！#推荐 #分享",
        "喜欢的话记得点赞关注哦❤️",
        "这是我最近发现的宝藏！",
        "简单易学，新手也能轻松掌握",
        "每天都要开心鸭🦆",
        "这个真的绝了！必须分享给你们",
        "生活需要仪式感✨",
        "治愈系的一天，心情美美哒",
        "快来一起体验吧！",
        "这个太酷了，必须安利！"
    )
    
    private val authorNames = listOf(
        "旅行达人小张", "美食博主李四", "摄影师王五", "生活家赵六",
        "Vlogger陈七", "探店小分队", "创意工作室", "日常记录者",
        "技能分享官", "快乐制造机"
    )
    
    /**
     * 生成视频列表
     */
    fun generateVideos(category: String, count: Int = 20, startIndex: Int = 0): List<VideoItem> {
        return (startIndex until startIndex + count).map { index ->
            val coverIndex = index % coverUrls.size
            val videoIndex = index % videoUrls.size
            val titleIndex = index % titles.size
            val descIndex = index % descriptions.size
            val authorIndex = index % authorNames.size
            
            // 随机高度实现瀑布流效果
            val heights = listOf(600, 700, 550, 650, 720, 580, 640, 620, 680)
            val height = heights[index % heights.size]
            
            VideoItem(
                id = UUID.randomUUID().toString(),
                coverUrl = coverUrls[coverIndex],
                videoUrl = videoUrls[videoIndex],
                title = titles[titleIndex],
                description = descriptions[descIndex],
                authorName = authorNames[authorIndex],
                authorAvatar = "https://picsum.photos/100?random=${authorIndex + 1}",
                likeCount = (1000..50000).random(),
                commentCount = (100..5000).random(),
                shareCount = (50..1000).random(),
                width = 400,
                height = height,
                category = category,
                timestamp = System.currentTimeMillis(),
                isLiked = false
            )
        }
    }
    
    /**
     * 生成评论列表
     */
    fun generateComments(videoId: String, count: Int = 20): List<Comment> {
        val commentContents = listOf(
            "太好看了！", "赞赞赞👍", "爱了爱了❤️", "这个绝了",
            "哈哈哈笑死我了😂", "学到了！", "太厉害了吧", "我也要试试",
            "收藏了！", "必须关注", "第一次见到", "太治愈了",
            "超级喜欢", "感谢分享", "这也太酷了", "我的天哪",
            "真的假的？", "在哪里可以买", "求教程", "已经转发了"
        )
        
        return (0 until count).map { index ->
            Comment(
                id = UUID.randomUUID().toString(),
                videoId = videoId,
                userId = "user_${index + 1}",
                userName = "用户${(1000..9999).random()}",
                avatarUrl = "https://picsum.photos/100?random=${index + 100}",
                content = commentContents[index % commentContents.size],
                timestamp = System.currentTimeMillis() - (index * 60000L), // 每条评论相差1分钟
                likeCount = (0..999).random()
            )
        }
    }
}
