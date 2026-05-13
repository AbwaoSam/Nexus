package com.example.nexus

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {
    @Query("SELECT * FROM articles ORDER BY publishedAt DESC")
    fun getAllArticles(): Flow<List<Article>>

    @Query("SELECT * FROM articles WHERE id = :id")
    fun getArticleById(id: String): Flow<Article?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(articles: List<Article>)

    @Query("SELECT id FROM articles WHERE isBookmarked = 1")
    suspend fun getBookmarkedIds(): List<String>

    @Query("DELETE FROM articles WHERE isBookmarked = 0")
    suspend fun deleteNonBookmarked()

    @Transaction
    suspend fun clearAndInsert(articles: List<Article>, isSearch: Boolean) {
        if (!isSearch) {
            deleteNonBookmarked()
        }
        val bookmarkedIds = getBookmarkedIds().toSet()
        val processedArticles = articles.map { 
            if (bookmarkedIds.contains(it.id)) it.copy(isBookmarked = true) else it
        }
        insertAll(processedArticles)
    }

    @Query("UPDATE articles SET isBookmarked = :isBookmarked WHERE id = :id")
    suspend fun updateBookmarkStatus(id: String, isBookmarked: Boolean)

    @Query("SELECT * FROM articles WHERE isBookmarked = 1 ORDER BY publishedAt DESC")
    fun getBookmarkedArticles(): Flow<List<Article>>
}
