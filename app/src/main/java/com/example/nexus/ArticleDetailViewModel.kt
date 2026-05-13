package com.example.nexus

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

@HiltViewModel
class ArticleDetailViewModel @Inject constructor(
    private val repository: NewsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val encodedArticleId: String = savedStateHandle["articleId"] ?: ""
    private val articleId: String = try {
        URLDecoder.decode(encodedArticleId, StandardCharsets.UTF_8.toString())
    } catch (e: Exception) {
        encodedArticleId
    }

    private val _uiState = MutableStateFlow(ArticleDetailUiState())
    val uiState: StateFlow<ArticleDetailUiState> = _uiState.asStateFlow()

    init {
        loadArticle()
    }

    private fun loadArticle() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.getArticleById(articleId).collect { article ->
                _uiState.value = _uiState.value.copy(
                    article = article,
                    isLoading = false
                )
            }
        }
    }

    fun toggleBookmark() {
        viewModelScope.launch {
            _uiState.value.article?.let { article ->
                repository.toggleBookmark(article.id, article.isBookmarked)
            }
        }
    }

    data class ArticleDetailUiState(
        val article: Article? = null,
        val isLoading: Boolean = false
    )
}
