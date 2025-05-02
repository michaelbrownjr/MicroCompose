package com.example.microcompose.di

import android.content.Context
import com.example.microcompose.network.MicroBlogApi
import com.example.microcompose.repository.MicroBlogRepository
import com.example.microcompose.repository.MicroBlogRepositoryImpl
import com.example.microcompose.ui.data.UserPreferences
import dagger.Binds // Import Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.firstOrNull
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

// Use an abstract class or interface for modules containing @Binds
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule { // Renamed for clarity, you can keep AppModule if you prefer

    // Use @Binds to tell Hilt which implementation to use for the interface
    @Binds
    @Singleton
    abstract fun bindMicroBlogRepository(
        microBlogRepositoryImpl: MicroBlogRepositoryImpl
    ): MicroBlogRepository // Return the interface
}

// You can keep provides functions in a separate object module if preferred
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideMicroBlogApi(userPreferences: UserPreferences): MicroBlogApi {
        MicroBlogApi.initialize { userPreferences.tokenFlow.firstOrNull() ?: "" }
        return MicroBlogApi
    }

    @Provides
    @Singleton
    fun provideUserPreferences(@ApplicationContext context: Context): UserPreferences {
        return UserPreferences(context)
    }
}