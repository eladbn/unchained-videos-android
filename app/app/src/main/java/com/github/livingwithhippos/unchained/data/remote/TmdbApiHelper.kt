package com.github.livingwithhippos.unchained.data.remote

import com.github.livingwithhippos.unchained.data.model.NetworkResponse
import com.github.livingwithhippos.unchained.data.model.TmdbMovieDetails
import com.github.livingwithhippos.unchained.data.model.TmdbSearchResponse
import com.github.livingwithhippos.unchained.data.model.TmdbTvDetails

interface TmdbApiHelper {
    suspend fun searchMulti(
        apiKey: String,
        query: String,
        page: Int = 1
    ): NetworkResponse<TmdbSearchResponse>
    
    suspend fun searchMovies(
        apiKey: String,
        query: String,
        year: Int? = null,
        page: Int = 1
    ): NetworkResponse<TmdbSearchResponse>
    
    suspend fun searchTv(
        apiKey: String,
        query: String,
        year: Int? = null,
        page: Int = 1
    ): NetworkResponse<TmdbSearchResponse>
    
    suspend fun getMovieDetails(
        movieId: Int,
        apiKey: String
    ): NetworkResponse<TmdbMovieDetails>
    
    suspend fun getTvDetails(
        tvId: Int,
        apiKey: String
    ): NetworkResponse<TmdbTvDetails>
}