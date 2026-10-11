/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.data

import androidx.room.immediateTransaction
import androidx.room.useWriterConnection

/** Serializes aggregate mutations and rolls back every DAO write when a command fails. */
interface RepositoryTransactions {
    /** Executes [block] on a single writer connection; nested commands join its transaction. */
    suspend fun <T> write(block: suspend () -> T): T
}

/** Room-backed transaction boundary shared by all repository aggregate commands. */
class RoomRepositoryTransactions(private val database: AppDatabase) : RepositoryTransactions {
    override suspend fun <T> write(block: suspend () -> T): T =
        database.useWriterConnection { connection ->
            connection.immediateTransaction { block() }
        }
}

/** Explicit seam for isolated DAO fakes, not a substitute for production database transactions. */
internal object DirectRepositoryTransactions : RepositoryTransactions {
    override suspend fun <T> write(block: suspend () -> T): T = block()
}
