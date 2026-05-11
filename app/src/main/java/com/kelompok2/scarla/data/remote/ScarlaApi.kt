package com.kelompok2.scarla.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("data") val data: T? = null,
    @SerializedName("message") val message: String? = null
)

data class QuizSummaryDto(
    @SerializedName("quizId") val quizId: String,
    @SerializedName("title") val title: String,
    @SerializedName("totalQuestions") val totalQuestions: Int? = null
)

data class MaterialSummaryDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("icon") val icon: String? = null,
    @SerializedName("totalVideos") val totalVideos: Int? = null,
    @SerializedName("quizzes") val quizzes: List<QuizSummaryDto> = emptyList(),
    @SerializedName("quizId") val quizId: String? = null
)

data class VideoDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("duration") val duration: String? = null,
    @SerializedName("videoRes") val videoRes: String? = null,
    @SerializedName("videoUrl") val videoUrl: String? = null,
    @SerializedName("url") val url: String? = null
)

data class MaterialDetailDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("icon") val icon: String? = null,
    @SerializedName("totalVideos") val totalVideos: Int? = null,
    @SerializedName("videos") val videos: List<VideoDto> = emptyList(),
    @SerializedName("quizzes") val quizzes: List<QuizSummaryDto> = emptyList(),
    @SerializedName("quizId") val quizId: String? = null
)

data class QuizQuestionDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("question") val question: String,
    @SerializedName("options") val options: List<String> = emptyList(),
    @SerializedName("correctAnswer") val correctAnswer: String? = null,
    @SerializedName("explanation") val explanation: String? = null,
    @SerializedName("answer") val answer: String? = null
)

data class QuizDetailDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("duration") val duration: Int? = null,
    @SerializedName("totalQuestions") val totalQuestions: Int? = null,
    @SerializedName("passingScore") val passingScore: Int? = null,
    @SerializedName("questions") val questions: List<QuizQuestionDto> = emptyList()
)

data class SubmitQuizRequest(
    @SerializedName("answers") val answers: List<String>,
    @SerializedName("userId") val userId: String? = null
)

data class SubmitQuizResponse(
    @SerializedName("quizId") val quizId: String? = null,
    @SerializedName("userId") val userId: String? = null,
    @SerializedName("score") val score: Int? = null,
    @SerializedName("correctAnswers") val correctAnswers: Int? = null,
    @SerializedName("totalQuestions") val totalQuestions: Int? = null,
    @SerializedName("isPassed") val isPassed: Boolean? = null,
    @SerializedName("passingScore") val passingScore: Int? = null,
    @SerializedName("results") val results: List<Any> = emptyList(),
    @SerializedName("submittedAt") val submittedAt: String? = null,
    @SerializedName("message") val message: String? = null
)

interface ScarlaApiService {
    @GET("api/materials")
    suspend fun getMaterials(): ApiResponse<List<MaterialSummaryDto>>

    @GET("api/materials/{materialId}")
    suspend fun getMaterial(@Path("materialId") materialId: String): ApiResponse<MaterialDetailDto>

    @GET("api/quizzes/{quizId}")
    suspend fun getQuiz(@Path("quizId") quizId: String): ApiResponse<QuizDetailDto>

    @POST("api/quizzes/{quizId}/submit")
    suspend fun submitQuiz(
        @Path("quizId") quizId: String,
        @Body request: SubmitQuizRequest
    ): ApiResponse<SubmitQuizResponse>
}

object ScarlaApi {
    // Deployed backend used by the app.
    private const val BASE_URL = "https://be-scarla.vercel.app/"

    val service: ScarlaApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ScarlaApiService::class.java)
    }
}
