package com.jfcardenas.musicwall.auth

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface UserSessionEntryPoint {
    fun userSessionRepository(): UserSessionRepository
}
