package dev.sathish.learningdashboard.data.remote

import dev.sathish.learningdashboard.data.remote.dto.CourseDetailDto
import dev.sathish.learningdashboard.data.remote.dto.CourseDto
import dev.sathish.learningdashboard.data.remote.dto.LoginRequest
import dev.sathish.learningdashboard.data.remote.dto.LoginResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse
}

interface CourseApi {
    @GET("courses")
    suspend fun getCourses(): List<CourseDto>

    @GET("courses/{id}")
    suspend fun getCourseDetail(@Path("id") courseId: Long): CourseDetailDto
}
