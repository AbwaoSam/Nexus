package com.example.nexus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val repository: NewsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewsUiState())
    val uiState: StateFlow<NewsUiState> = _uiState.asStateFlow()

    private var fetchJob: Job? = null
    private var searchJob: Job? = null

    val categories = listOf("general", "business", "entertainment", "health", "science", "sports", "technology")
    val continents = listOf("All", "Africa", "Asia", "Europe", "North America", "South America", "Oceania")

    init {
        loadNews()
    }

    fun onCategorySelected(category: String) {
        if (_uiState.value.selectedCategory == category) return
        _uiState.value = _uiState.value.copy(
            selectedCategory = category,
            selectedContinent = "All",
            searchQuery = "",
            isVideoOnly = false
        )
        loadNews(forceRefresh = true)
    }

    fun onContinentSelected(continent: String) {
        if (_uiState.value.selectedContinent == continent) return
        _uiState.value = _uiState.value.copy(
            selectedContinent = continent,
            searchQuery = "",
            isVideoOnly = false
        )
        loadNews(forceRefresh = true)
    }

    fun toggleVideoFilter() {
        val newState = !_uiState.value.isVideoOnly
        _uiState.value = _uiState.value.copy(isVideoOnly = newState)
        loadNews(forceRefresh = true)
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500) // Debounce search
            if (query.isNotEmpty()) {
                loadNews(query = query, forceRefresh = true)
            } else {
                loadNews(forceRefresh = true)
            }
        }
    }

    fun loadNews(query: String? = null, forceRefresh: Boolean = false) {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Determine what to fetch
            var effectiveQuery = query ?: if (_uiState.value.selectedContinent != "All") {
                _uiState.value.selectedContinent
            } else if (_uiState.value.searchQuery.isNotEmpty()) {
                _uiState.value.searchQuery
            } else {
                null
            }

            // If video filter is on, append "video" to the search query
            if (_uiState.value.isVideoOnly) {
                effectiveQuery = if (effectiveQuery == null) "video" else "$effectiveQuery video"
            }

            repository.getNewsArticles(
                category = if (effectiveQuery == null) _uiState.value.selectedCategory else null,
                query = effectiveQuery,
                forceRefresh = forceRefresh
            ).collect { articles ->
                _uiState.value = _uiState.value.copy(
                    articles = articles,
                    isLoading = false,
                    error = null
                )
            }
        }
    }

    fun refreshNews() {
        loadNews(forceRefresh = true)
    }

    fun toggleBookmark(article: Article) {
        viewModelScope.launch {
            repository.toggleBookmark(article.id, article.isBookmarked)
        }
    }

    data class NewsUiState(
        val articles: List<Article> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val selectedCategory: String = "general",
        val selectedContinent: String = "All",
        val searchQuery: String = "",
        val isVideoOnly: Boolean = false
    )
}
