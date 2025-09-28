package com.github.livingwithhippos.unchained.data.remote

import com.github.livingwithhippos.unchained.data.model.TmdbMovieDetails
import com.github.livingwithhippos.unchained.data.model.TmdbSearchResponse
import com.github.livingwithhippos.unchained.data.model.TmdbTvDetails
import retrofit2.Response

interface TmdbApiHelper {
    suspend fun searchMulti(
        apiKey: String,
        query: String,
        page: Int = 1
    ): Response<TmdbSearchResponse>
    
    suspend fun searchMovies(
        apiKey: String,
        query: String,
        year: Int? = null,
        page: Int = 1
    ): Response<TmdbSearchResponse>
    
    suspend fun searchTv(
        apiKey: String,
        query: String,
        year: Int? = null,
        page: Int = 1
    ): Response<TmdbSearchResponse>
    
    suspend fun getMovieDetails(
        movieId: Int,
        apiKey: String
    ): Response<TmdbMovieDetails>
    
    suspend fun getTvDetails(
        tvId: Int,
        apiKey: String
    ): Response<TmdbTvDetails>
}