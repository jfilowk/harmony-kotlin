package com.harmony.kotlin.data.datasource.network

import io.ktor.client.HttpClient
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.client.utils.EmptyContent
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.Parameters
import io.ktor.http.ParametersBuilder
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.contentType

/**
 * Executes a Ktor request using the information contained in the query.
 *
 * **IMPORTANT:** This method is intended to be used only be used inside a Network DataSource
 */
suspend fun NetworkQuery.executeKtorRequest(httpClient: HttpClient, baseUrl: String, globalHeaders: List<Pair<String, String>>): String {
  val query = this
  return httpClient.request {
    method = query.method.mapToKtorMethod()

    url(query.generateKtorUrl(baseUrl))

    // headers
    if (query is GenericOAuthQuery) {
      oauthPasswordHeader(getPasswordTokenInteractor = query.getPasswordTokenInteractor)
    }
    headers(globalHeaders)
    headers(query.mergeHeaders())

    // content-type & body
    query.method.contentType?.let { contentType ->
      when (contentType) {
        is NetworkQuery.ContentType.FormUrlEncoded -> {
          contentType(ContentType.Application.FormUrlEncoded)
          setBody(
            FormDataContent(
              Parameters.build {
                contentType.params.forEach {
                  append(it.first, it.second)
                }
              }
            )
          )
        }
        is NetworkQuery.ContentType.Json<*> -> {
          contentType(ContentType.Application.Json)
          setBody(contentType.entity ?: EmptyContent)
        }
      }
    }
  }.bodyAsText()
}

/**
 * Creates a url using Ktor URLBuilder with baseUrl + path + url params
 *
 * **IMPORTANT:** This method is intended to be used only be used inside a Network DataSource
 */
fun NetworkQuery.generateKtorUrl(baseUrl: String): Url {

  val sanitizedBaseUrl = baseUrl.removeSuffix("/")
  val sanitizedPaths = this.path.split("/").filter { it.isNotEmpty() }.joinToString(separator = "/", prefix = "/")

  val urlBuilder = URLBuilder(sanitizedBaseUrl + sanitizedPaths)

  if (this.urlParams.isNotEmpty()) {
    urlBuilder.parameters.also {
      it.appendAll(generateKtorUrlParams(this.urlParams))
    }
  }

  return urlBuilder.build()
}

private fun generateKtorUrlParams(params: List<Pair<String, String>>): Parameters {
  val parametersBuilder = ParametersBuilder(params.size)
  params.forEach {
    parametersBuilder.append(it.first, it.second)
  }
  return parametersBuilder.build()
}

/**
 * Transforms query method to Ktor method
 *
 * **IMPORTANT:** This method is intended to be used only be used inside a Network DataSource
 */
fun NetworkQuery.Method.mapToKtorMethod(): HttpMethod {
  return when (this) {
    is NetworkQuery.Method.Get -> HttpMethod.Get
    is NetworkQuery.Method.Post -> HttpMethod.Post
    is NetworkQuery.Method.Put -> HttpMethod.Put
    is NetworkQuery.Method.Delete -> HttpMethod.Delete
  }
}
