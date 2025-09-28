package com.github.livingwithhippos.unchained.search.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.livingwithhippos.unchained.data.model.TmdbInfo
import com.github.livingwithhippos.unchained.data.repository.TmdbRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SearchItemViewModel @Inject constructor(
    private val tmdbRepository: TmdbRepository
) : ViewModel() {

    private val _tmdbInfo = MutableLiveData<TmdbInfo?>()
    val tmdbInfo: LiveData<TmdbInfo?> = _tmdbInfo

    private val _tmdbLoading = MutableLiveData<Boolean>()
    val tmdbLoading: LiveData<Boolean> = _tmdbLoading

    private val _tmdbError = MutableLiveData<String?>()
    val tmdbError: LiveData<String?> = _tmdbError

    fun loadTmdbInfo(torrentName: String) {
        viewModelScope.launch {
            try {
                _tmdbLoading.value = true
                _tmdbError.value = null
                
                val info = tmdbRepository.searchForTorrentInfo(torrentName)
                _tmdbInfo.value = info
                
                if (info == null) {
                    _tmdbError.value = "No TMDB information found"
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading TMDB info")
                _tmdbError.value = "Error loading TMDB information"
                _tmdbInfo.value = null
            } finally {
                _tmdbLoading.value = false
            }
        }
    }

    fun clearTmdbInfo() {
        _tmdbInfo.value = null
        _tmdbError.value = null
        _tmdbLoading.value = false
    }
}