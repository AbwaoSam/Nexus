package com.example.nexus


import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsDataApi {
    // Fetching Headlines
    @GET("latest")
    suspend fun getTopHeadlines(
        @Query("country") country: String = "ke",
        @Query("category") category: String? = null,
        @Query("apikey") apiKey: String = BuildConfig.NEWS_API_KEY
    ): NewsResponse

    @GET("news")
    suspend fun searchNews(
        @Query("q") query: String,
        @Query("apikey") apiKey: String = BuildConfig.NEWS_API_KEY
    ): NewsResponse
}

data class NewsResponse(
    val status: String,
    val totalResults: Int,
    val results: List<ArticleDto>,
    val nextPage: String?          // Pagination token
)

data class ArticleDto(
    @SerializedName("article_id") val articleId: String,
    val title: String,
    val link: String,
    val description: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("pubDate") val pubDate: String,
    val content: String?,
    val creator: List<String>?,
    @SerializedName("source_id") val sourceId: String?
) {
    fun toArticle(): Article {
        return Article(
            id = articleId,
            title = title,
            description = description ?: "",
            content = content ?: "",
            url = link,
            imageUrl = imageUrl,
            publishedAt = pubDate,
            sourceName = sourceId?.replaceFirstChar { it.uppercase() } ?: "General",
            author = creator?.joinToString(", ") ?: "Unknown",
            isBookmarked = false
        )
    }
}
