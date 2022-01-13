package com.exchanger.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exchanger.helper.Resource
import com.exchanger.helper.SingleLiveEvent
import com.exchanger.model.ApiResponse
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainViewModel @ViewModelInject constructor(private val mainRepo: MainRepo) : ViewModel()  {
    //cached
    private val _data = SingleLiveEvent<Resource<ApiResponse>>()

    val data  =  _data
    val convertedRate = MutableLiveData<Double>()

    fun getConvertedData(access_key: String) {
        viewModelScope.launch {
           mainRepo.getConvertedData(access_key).collect {
               data.value = it
            }
        }
    }
}