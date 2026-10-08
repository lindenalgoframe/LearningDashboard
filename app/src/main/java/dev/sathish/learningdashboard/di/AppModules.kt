package dev.sathish.learningdashboard.di

import android.content.Context
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.sathish.learningdashboard.BuildConfig
import dev.sathish.learningdashboard.data.local.CourseDao
import dev.sathish.learningdashboard.data.local.LearningDatabase
import dev.sathish.learningdashboard.data.remote.AuthApi
import dev.sathish.learningdashboard.data.remote.ConnectivityNetworkMonitor
import dev.sathish.learningdashboard.data.remote.CourseApi
import dev.sathish.learningdashboard.data.remote.MockApiInterceptor
import dev.sathish.learningdashboard.data.remote.NetworkMonitor
import dev.sathish.learningdashboard.data.repository.AuthRepositoryImpl
import dev.sathish.learningdashboard.data.repository.CourseRepositoryImpl
import dev.sathish.learningdashboard.domain.repository.AuthRepository
import dev.sathish.learningdashboard.domain.repository.CourseRepository
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        @ApplicationContext context: Context,
        networkMonitor: NetworkMonitor,
        json: Json,
    ): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            }
        }
        .addInterceptor(MockApiInterceptor(context.assets, networkMonitor, json))
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, json: Json): Retrofit = Retrofit.Builder()
        .baseUrl(MockApiInterceptor.BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideCourseApi(retrofit: Retrofit): CourseApi = retrofit.create(CourseApi::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    // No fallbackToDestructiveMigration(): wiping user progress on upgrade is not acceptable;
    // schema changes ship with explicit Migrations (schemas are exported to app/schemas).
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): LearningDatabase =
        Room.databaseBuilder(context, LearningDatabase::class.java, "learning.db").build()

    @Provides
    fun provideCourseDao(database: LearningDatabase): CourseDao = database.courseDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class BindingsModule {

    @Binds
    @Singleton
    abstract fun bindNetworkMonitor(impl: ConnectivityNetworkMonitor): NetworkMonitor

    @Binds
    @Singleton
    abstract fun bindCourseRepository(impl: CourseRepositoryImpl): CourseRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
}
