package com.example.nexus

import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewsRepository @Inject constructor(
    private val articleDao: ArticleDao,
    private val newsDataApi: NewsDataApi
) {
    fun getNewsArticles(
        category: String? = null,
        query: String? = null,
        forceRefresh: Boolean = false
    ): Flow<List<Article>> {
        var refreshAttempted = false
        return articleDao.getAllArticles().transform { cachedArticles ->
            if (!refreshAttempted && (forceRefresh || cachedArticles.isEmpty() || query != null)) {
                refreshAttempted = true
                fetchAndCacheArticles(category, query)
            }
            emit(cachedArticles)
        }
    }

    private suspend fun fetchAndCacheArticles(category: String? = null, query: String? = null) {
        try {
            val response = if (query != null) {
                newsDataApi.searchNews(query = query)
            } else {
                newsDataApi.getTopHeadlines(category = category)
            }

            if (response.status == "success") {
                val articles: List<Article> = response.results.map { it.toArticle() }
                articleDao.clearAndInsert(articles, isSearch = query != null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getArticleById(id: String): Flow<Article?> {
        return articleDao.getArticleById(id)
    }

    suspend fun toggleBookmark(articleId: String, currentStatus: Boolean) {
        articleDao.updateBookmarkStatus(articleId, !currentStatus)
    }

    fun getBookmarkedArticles(): Flow<List<Article>> {
        return articleDao.getBookmarkedArticles()
    }
}
