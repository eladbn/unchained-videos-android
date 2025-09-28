package com.github.livingwithhippos.unchained.data.remote

import com.github.livingwithhippos.unchained.data.model.TmdbMovieDetails
import com.github.livingwithhippos.unchained.data.model.TmdbSearchResponse
import com.github.livingwithhippos.unchained.data.model.TmdbTvDetails
import javax.inject.Inject
import retrofit2.Response

class TmdbApiHelperImpl @Inject constructor(private val tmdbApi: TmdbApi) : TmdbApiHelper {
    
    override suspend fun searchMulti(
        apiKey: String,
        query: String,
        page: Int
    ): Response<TmdbSearchResponse> {
        return tmdbApi.searchMulti(apiKey, query, page)
    }
    
    override suspend fun searchMovies(
        apiKey: String,
        query: String,
        year: Int?,
        page: Int
    ): Response<TmdbSearchResponse> {
        return tmdbApi.searchMovies(apiKey, query, year, page)
    }
    
    override suspend fun searchTv(
        apiKey: String,
        query: String,
        year: Int?,
        page: Int
    ): Response<TmdbSearchResponse> {
        return tmdbApi.searchTv(apiKey, query, year, page)
    }
    
    override suspend fun getMovieDetails(
        movieId: Int,
        apiKey: String
    ): Response<TmdbMovieDetails> {
        return tmdbApi.getMovieDetails(movieId, apiKey)
    }
    
    override suspend fun getTvDetails(
        tvId: Int,
        apiKey: String
    ): Response<TmdbTvDetails> {
        return tmdbApi.getTvDetails(tvId, apiKey)
    }
}