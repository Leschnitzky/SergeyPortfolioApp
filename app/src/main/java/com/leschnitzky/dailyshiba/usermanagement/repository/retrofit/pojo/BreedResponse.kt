package com.leschnitzky.dailyshiba.usermanagement.repository.retrofit.pojo

import com.squareup.moshi.Json

data class BreedResponse(

	@Json(name="response")
	val response: Response,

	@Json(name="status")
	val status: String
)

data class Response(

	@Json(name="url")
	val url: String
)
