# Data Layer

Source: https://developer.android.com/topic/architecture/data-layer

The data layer contains application data and business logic. It enables reuse across multiple
screens, information sharing between app parts, and business logic unit testing outside the UI.

---

## Architecture

The data layer consists of repositories containing zero to many data sources. Create a repository
class for each different data type (e.g., `MoviesRepository`, `PaymentsRepository`).

### Repository Responsibilities

1. Exposing data to the rest of the app
2. Centralizing changes to the data
3. Resolving conflicts between multiple data sources
4. Abstracting data sources from the rest of the app
5. Containing business logic

### Data Source Responsibilities

Each data source class works with only one source of data (file, network source, or local database).
Data sources act as a bridge between the application and the system for data operations. Other
layers should never access data sources directly.

---

## Data Immutability

Data exposed by the data layer must be immutable. This prevents tampering, avoids inconsistent
states, and is safe for multiple threads.

---

## Expose APIs

**One-shot operations:** Kotlin `suspend` functions.

**Streaming data changes:** Kotlin `Flow` types.

```kotlin
class ExampleRepository(
    private val exampleRemoteDataSource: ExampleRemoteDataSource,
    private val exampleLocalDataSource: ExampleLocalDataSource
) {
    val data: Flow<Example> = ...
    suspend fun modifyData(example: Example) { ... }
}
```

---

## Naming Conventions

**Repository classes:** `{TypeOfData}Repository` (e.g., `NewsRepository`)

**Data source classes:** `{TypeOfData}{TypeOfSource}DataSource`

Type of source options: `Remote`, `Local` (generic, implementation-agnostic), or `Network`, `Disk`,
`Api`, `Database` (specific). Don't name based on implementation details (avoid
`UserSharedPreferencesDataSource`).

---

## Multiple Levels of Repositories

Repositories can depend on other repositories:

```kotlin
class UserRepository(
    private val loginRepository: LoginRepository,
    private val registrationRepository: RegistrationRepository
)
```

Some developers call these "Managers" (e.g., `UserManager`). Either convention is acceptable.

---

## Source of Truth

Each repository must define a single source of truth containing data that is consistent, correct,
and up-to-date. Different repositories can have different sources of truth:

- `LoginRepository` -> cache
- `PaymentsRepository` -> network
- `NewsRepository` -> database

Best practice: use local data source (database) as source of truth for offline-first support.

---

## Threading

Data sources and repositories must be main-safe. The class doing the work dispatches to the
appropriate thread:

```kotlin
class NewsRemoteDataSource(
    private val newsApi: NewsApi,
    private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun fetchLatestNews(): List<ArticleHeadline> =
        withContext(ioDispatcher) {
            newsApi.fetchLatestNews()
        }
}
```

Most modern libraries (Room, Retrofit, Ktor) already provide main-safe APIs.

---

## Lifecycle

Data layer instances remain in memory while reachable from garbage collection roots. Use dependency
injection to manage scoping:

- **Application-level scope** for app-wide data
- **Flow-specific scope** for feature flows

---

## Represent Business Models

Data sources often return more data than needed. Create repository-level models with only necessary
data:

```kotlin
// API returns too much data
data class ArticleApiModel(
    val id: Long,
    val title: String,
    val content: String,
    val publicationDate: Date,
    val modifications: Array<ArticleApiModel>,  // Not needed
    val comments: Array<CommentApiModel>,        // Not needed
    ...
)

// Repository exposes only necessary data
data class Article(
    val id: Long,
    val title: String,
    val content: String,
    val publicationDate: Date,
    val authorName: String,
    val readTimeMin: Int
)
```

---

## Types of Data Operations

### UI-Oriented

Only relevant when user is on specific screen. Cancelled when user leaves. Follow ViewModel
lifecycle.

### App-Oriented

Relevant as long as app is open. Use application-scoped `CoroutineScope`:

```kotlin
class NewsRepository(
    private val newsRemoteDataSource: NewsRemoteDataSource,
    private val externalScope: CoroutineScope
) {
    suspend fun getLatestNews(refresh: Boolean = false): List<ArticleHeadline> {
        return if (refresh) {
            externalScope.async {
                newsRemoteDataSource.fetchLatestNews().also { result ->
                    latestNewsMutex.withLock { latestNews = result }
                }
            }.await()
        } else {
            latestNewsMutex.withLock { latestNews }
        }
    }
}
```

### Business-Oriented

Cannot be canceled. Must survive process death. Use WorkManager:

```kotlin
class RefreshLatestNewsWorker(
    private val newsRepository: NewsRepository,
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = try {
        newsRepository.refreshLatestNews()
        Result.success()
    } catch (error: Throwable) {
        Result.failure()
    }
}
```

---

## Error Handling

### Coroutines and Flows

For `suspend` functions: `try/catch`. For `Flow`: `catch` operator.

### Custom Exceptions

```kotlin
class UserNotAuthenticatedException : Exception()
```

### Result Wrapper Pattern

```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
    object Loading : Result<Nothing>()
}
```

---

## Testing

### Unit Tests

Use fake implementations (not mocks):

```kotlin
class FakeNewsRemoteDataSource : NewsRemoteDataSource {
    private var articles = emptyList<Article>()
    fun setArticles(newArticles: List<Article>) { articles = newArticles }
    override suspend fun fetchLatestNews() = articles
}
```

### Integration Tests

- **Room**: `Room.inMemoryDatabaseBuilder()` with `allowMainThreadQueries()`
- **Network**: `MockWebServer` for API parsing tests

---

## Related Documentation

- [Architecture overview](https://developer.android.com/topic/architecture)
- [Architecture recommendations](https://developer.android.com/topic/architecture/recommendations)
- [Offline first](https://developer.android.com/topic/architecture/data-layer/offline-first)
- [Room](https://developer.android.com/training/data-storage/room)
- [DataStore](https://developer.android.com/topic/libraries/architecture/datastore)
- [WorkManager](https://developer.android.com/develop/background-work/background-tasks/persistent)
