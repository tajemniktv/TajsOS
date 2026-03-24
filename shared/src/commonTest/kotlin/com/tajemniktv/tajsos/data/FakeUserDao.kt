package com.tajemniktv.tajsos.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeUserDao : UserDao {
    override fun getUser(): Flow<UserEntity?> = flowOf(null)
    override suspend fun insertUser(user: UserEntity) = Unit
    override suspend fun updateUser(user: UserEntity) = Unit
}
