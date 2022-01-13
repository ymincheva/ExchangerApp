package com.exchanger.helper

import com.exchanger.BuildConfig

class EndPoints {

    companion object {

        //Base URL
        const val BASE_URL = "http://api.exchangeratesapi.io/v1/"

        //API KEY
        const val API_KEY = BuildConfig.API_TOKEN

        //LATEST URL
        const val LATEST_URL = "latest"
    }
}