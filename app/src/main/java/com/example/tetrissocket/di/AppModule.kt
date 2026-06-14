package com.example.tetrissocket.di

import com.example.tetrissocket.data.repository.GameRepository
import com.example.tetrissocket.data.repository.GameRepositoryImpl
import com.example.tetrissocket.data.repository.SocketRepository
import com.example.tetrissocket.data.socket.SocketRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideSocketRepository(impl: SocketRepositoryImpl): SocketRepository = impl

    @Provides
    @Singleton
    fun provideGameRepository(impl: GameRepositoryImpl): GameRepository = impl
}
