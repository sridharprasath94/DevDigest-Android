package com.flash.devdigest.presentation.newsdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flash.devdigest.core.DataResult
import com.flash.devdigest.domain.model.News
import com.flash.devdigest.domain.usecase.GetNewsUseCase
import com.flash.devdigest.presentation.error.UIError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewsDetailsViewModel @Inject constructor(
    private val getNewsUseCase: GetNewsUseCase
) : ViewModel() {
    private val _state = MutableStateFlow<NewsDetailUiState>(NewsDetailUiState.Initial)
    val state = _state.asStateFlow()
    private val _events = MutableSharedFlow<UIError>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val events = _events.asSharedFlow()

    fun loadNews(id: Long) {
        viewModelScope.launch {
            _state.value = NewsDetailUiState.Initial
            when (val result = getNewsUseCase.invoke(id)) {
                is DataResult.Success<News> -> {
                    _state.value = NewsDetailUiState.Success(result.data)
                }

                is DataResult.Error -> _events.emit(UIError.from(result.error))
            }
        }
    }
}