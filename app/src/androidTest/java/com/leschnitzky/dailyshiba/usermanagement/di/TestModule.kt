package com.leschnitzky.dailyshiba.usermanagement.di

import com.leschnitzky.dailyshiba.usermanagement.repository.Repository
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [UserManagementModule::class]
)
internal object TestModule {
    @Provides
    fun provideRepo(): Repository{
        return FakeRepositoryImpl()
    }
}