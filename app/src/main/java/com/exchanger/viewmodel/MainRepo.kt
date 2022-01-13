package com.exchanger.viewmodel

import com.exchanger.helper.Resource
import com.exchanger.model.ApiResponse
import com.exchanger.network.ApiDataSource
import com.exchanger.network.BaseDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject


class MainRepo @Inject constructor(private val apiDataSource: ApiDataSource) : BaseDataSource() {

    suspend fun getConvertedData(
        access_key: String
    ): Flow<Resource<ApiResponse>> {
        return flow {
            emit(safeApiCall { apiDataSource.getConvertedRate(access_key/*, from, to, amount*/) })
        }.flowOn(Dispatchers.IO)
    }
}