package com.example.nexus


import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ArticleDatabase {
        return ArticleDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideArticleDao(database: ArticleDatabase): ArticleDao {
        return database.articleDao()
    }
}
