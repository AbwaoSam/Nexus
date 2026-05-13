package com.example.nexus

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "articles")
data class Article(
    @PrimaryKey
    val id: String = "",
    val title: String,
    val description: String,
    val content: String,
    val url: String,
    val imageUrl: String?,
    val publishedAt: String,
    val sourceName: String,
    val author: String?,
    val isBookmarked: Boolean = false
)
