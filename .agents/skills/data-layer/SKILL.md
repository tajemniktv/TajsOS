---
name: data-layer
description: >-
  Google's recommended Data Layer architecture for Android. Use this skill when
  designing, implementing, or reviewing repositories, data sources, data models,
  model-per-layer separation, naming conventions, threading, error propagation,
  lifecycle/scoping, or testing in the data layer. Based on
  developer.android.com/topic/architecture/data-layer and the official Android
  architecture recommendations page.
license: Complete terms in LICENSE.txt
metadata:
  author: Google LLC
  keywords:
    - android
    - architecture
    - data-layer
    - repository
    - data-source
    - single-source-of-truth
    - room
    - datastore
    - coroutines
    - threading
    - main-safety
    - error-handling
---

# Android Architecture -- Data Layer

**Source of truth:
** [developer.android.com/topic/architecture/data-layer](https://developer.android.com/topic/architecture/data-layer)

## TajsOS Scope Note

This is Android data-layer guidance. For TajsOS shared KMP code, prioritize `commonMain` boundaries
and repo-specific architecture docs over Android-only assumptions.

## References

- *[Data layer](references/android/topic/architecture/data-layer.md)* -- full data layer
  documentation with detailed code examples

---

## The Data Layer's Job

The data layer:

- Exposes application data to the rest of the app
- Contains **the vast majority of your app's business logic**
- Is the **Single Source of Truth** for every data type it owns
- Abstracts data sources from the UI and domain layers

---

## Structure

```
Data Layer
+-- Repository          <- single entry point per data type
|   +-- RemoteDataSource  <- network (Retrofit, Ktor, Firebase)
|   +-- LocalDataSource   <- disk (Room, DataStore, file, SharedPreferences)
+-- Models              <- network DTOs, DB entities, domain/app models
```

The UI and domain layers must **never** access a data source directly.
Repositories are the only entry point.

---

## Repository -- **Strongly Recommended**

Create a repository even if it contains only a single data source.

> **Note:** For trivial cases where a repository has a single data source and no
> other dependencies, the official docs allow merging repository and data source
> responsibilities into one class. Split them when the repository grows.

A repository:

1. Exposes data to upper layers as immutable types
2. Centralises all mutations for the data type it owns
3. Resolves conflicts between data sources (e.g. network vs cache)
4. Applies the Single Source of Truth strategy (usually the local DB)
5. Contains business rules for data creation, storage, and change

```kotlin
class NewsRepository @Inject constructor(
    private val newsRemoteDataSource: NewsRemoteDataSource,
    private val newsLocalDataSource: NewsLocalDataSource,
) {
    // Observable stream -- local DB is the SSOT
    fun getLatestNewsStream(): Flow<List<Article>> =
        newsLocalDataSource.getNewsStream()

    // One-shot refresh -- network -> DB -> DB emits via Flow above
    suspend fun refreshLatestNews() {
        val fresh = newsRemoteDataSource.fetchLatestNews()
        newsLocalDataSource.saveNews(fresh.toEntities())
    }

    // One-shot read
    suspend fun getArticle(id: String): Article =
        newsLocalDataSource.getArticle(id).toArticle()
}
```

---

## Data Sources

- One data source per **physical source** (network, database, file, DataStore, SharedPreferences)
- Data sources only interact with a single source -- never two
- Data sources are **never** accessed directly from the UI or domain layers

```kotlin
// Named after data type + source type
class NewsRemoteDataSource @Inject constructor(private val api: NewsApi)
class NewsLocalDataSource @Inject constructor(private val dao: NewsDao)

// Don't name after implementation detail -- breaks when you swap the impl
// BAD:  class NewsSharedPreferencesDataSource(...)
// GOOD: class NewsLocalDataSource(...)  // swap SP -> DataStore without renaming
```

---

## Exposing Data -- API Contract

```kotlin
// One-shot operation: suspend fun
suspend fun getUser(id: String): User

// Observable stream: Flow<T> -- use get{Model}Stream() naming
fun getUserStream(id: String): Flow<User>

// Mutation: suspend fun, return Result or throw typed exception
suspend fun updateUser(user: User)
suspend fun saveBookmark(articleId: String): Result<Unit>
```

**All exposed data must be immutable.** Use `data class` with `val`-only properties.
Callers must never be able to mutate data they receive from a repository.

---

## Single Source of Truth (SSOT)

Each repository must define a single source of truth. Different repositories may
choose different sources:

| Repository           | Typical SSOT          | Reason                |
|----------------------|-----------------------|-----------------------|
| `NewsRepository`     | Local database (Room) | Offline-first support |
| `LoginRepository`    | In-memory cache       | Fast, session-scoped  |
| `PaymentsRepository` | Network data source   | Must be authoritative |

The local database is the **recommended** SSOT for persistent, offline-first data:

```
Network response
      |
      v (save)
Local DB  ---- (emit via Flow) -->  Repository -->  ViewModel -->  UI
```

Never expose a raw network response to the UI. Persist first, then observe from local.

---

## Model Per Layer -- **Recommended**

In complex apps, create a distinct model class at each layer boundary:

| Layer                   | Model type       | Naming example                           |
|-------------------------|------------------|------------------------------------------|
| Network                 | API response DTO | `ArticleApiModel`, `ArticleNetworkModel` |
| Local DB                | Room entity      | `ArticleEntity`                          |
| Repository (domain/app) | App model        | `Article`                                |
| UI                      | Display model    | `ArticleUi`, `ArticleFeedItem`           |

Map between models at each boundary. This decouples layers so a backend schema
change doesn't cascade to the UI, and a UI redesign doesn't force DB migrations.

```kotlin
// In repository -- map from entity to app model
fun ArticleEntity.toArticle(): Article = Article(
    id = this.id,
    title = this.title,
    content = this.content,
)

// In ViewModel -- map from app model to UI model
fun Article.toUiModel(): ArticleUi = ArticleUi(
    id = this.id,
    displayTitle = this.title.uppercase(),
    snippet = this.content.take(150),
)
```

At minimum, create new model classes when a data source receives data that doesn't
match what the rest of the app expects. Properly document and test transformations.

---

## Naming Conventions -- Optional (but consistent)

| Class                     | Pattern                             | Example                                               |
|---------------------------|-------------------------------------|-------------------------------------------------------|
| Repository                | `<DataType>Repository`              | `NewsRepository`, `UserRepository`                    |
| Remote data source        | `<DataType>RemoteDataSource`        | `NewsRemoteDataSource`                                |
| Local data source         | `<DataType>LocalDataSource`         | `UserLocalDataSource`                                 |
| Tasks data source         | `<DataType>TasksDataSource`         | `NewsTasksDataSource` (WorkManager scheduling)        |
| Repository interface impl | Meaningful name or `Default` prefix | `OfflineFirstNewsRepository`, `DefaultUserRepository` |
| Fake impl (tests)         | `Fake` prefix                       | `FakeNewsRepository`, `FakeUserLocalDataSource`       |
| Stream-returning function | `get{Model}Stream()`                | `getAuthorStream(): Flow<Author>`                     |
| List stream               | plural model name                   | `getAuthorsStream(): Flow<List<Author>>`              |
| Regular method            | verb phrase                         | `refreshNews()`, `saveBookmark()`                     |
| Property                  | noun phrase                         | `inProgressTopicSelection`                            |

> **Note:** Some developers call repositories that depend on other repositories
> "Managers" (e.g., `UserManager`). Either convention is acceptable.

---

## Threading & Main-Safety -- **Strongly Recommended**

Every repository and data source function must be safe to call from the main thread.
**The class doing the work is responsible for dispatching -- not the caller.**

```kotlin
class NewsRemoteDataSource(
    private val newsApi: NewsApi,
    private val ioDispatcher: CoroutineDispatcher,  // injected -- testable
) {
    suspend fun fetchLatestNews(): List<ArticleHeadline> =
        withContext(ioDispatcher) {     // dispatches here, not in the caller
            newsApi.fetchLatestNews()
        }
}
```

Because the repository is already main-safe, **the ViewModel does not need to
specify a dispatcher** -- `viewModelScope.launch` runs on the main thread and is correct:

```kotlin
// No Dispatchers.IO here -- the repository handles threading
viewModelScope.launch {
    val result = newsRepository.fetchLatestNews()   // safe from main thread
    _uiState.update { it.copy(newsItems = result.toUiModel()) }
}

// BAD -- Don't do this -- leaks threading concern into the ViewModel
viewModelScope.launch(Dispatchers.IO) {
    newsRepository.fetchLatestNews()
}
```

Inject dispatchers. Never hardcode `Dispatchers.IO` inline -- inject a
`CoroutineDispatcher` so tests can substitute `UnconfinedTestDispatcher`.

### In-memory caching with Mutex

```kotlin
class NewsRepository(private val remoteDataSource: NewsRemoteDataSource) {

    private val latestNewsMutex = Mutex()
    private var latestNews: List<Article> = emptyList()

    suspend fun getLatestNews(refresh: Boolean = false): List<Article> {
        if (refresh || latestNews.isEmpty()) {
            val networkResult = remoteDataSource.fetchLatestNews()
            latestNewsMutex.withLock { latestNews = networkResult }
        }
        return latestNewsMutex.withLock { latestNews }
    }
}
```

Use `Mutex` to protect reads and writes to any in-memory cache from multiple threads.

### Operation types

| Type              | Lifetime                          | Implementation                               |
|-------------------|-----------------------------------|----------------------------------------------|
| UI-oriented       | Cancelled when user leaves screen | `viewModelScope.launch`                      |
| App-oriented      | Lives as long as the app process  | Injected application-scoped `CoroutineScope` |
| Business-critical | Survives process death            | `WorkManager`                                |

For **app-oriented operations**, use an external `CoroutineScope` so the operation
continues even if the user leaves the screen:

```kotlin
class NewsRepository(
    private val newsRemoteDataSource: NewsRemoteDataSource,
    private val externalScope: CoroutineScope,  // Application lifecycle
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

For **business-critical operations**, use a `TasksDataSource` wrapping WorkManager:

```kotlin
class NewsTasksDataSource(private val workManager: WorkManager) {
    fun fetchNewsPeriodically() {
        val request = PeriodicWorkRequestBuilder<RefreshLatestNewsWorker>(
            4, TimeUnit.HOURS
        ).setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.TEMPORARILY_UNMETERED)
                .setRequiresCharging(true)
                .build()
        ).build()

        workManager.enqueueUniquePeriodicWork(
            "FetchLatestNews",
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }
}
```

---

## Lifecycle & Scoping

Data layer instances remain in memory while reachable from garbage collection roots.

| Scope             | When to use                          | How                                                   |
|-------------------|--------------------------------------|-------------------------------------------------------|
| Application-level | App-wide data, in-memory caches      | `@Singleton` via Hilt or application-scoped container |
| Flow-specific     | Data relevant only to a feature flow | Scoped to the flow's DI container or Activity         |

Use dependency injection to manage scoping. Don't create repositories manually in
Activities -- let the DI framework control instance lifetime.

---

## Error Handling

Repositories must propagate errors -- never swallow them silently.

- **For `suspend fun`**: use `try/catch`
- **For `Flow`**: use the `catch` operator -- `try/catch` does not catch exceptions in flow
  collectors

```kotlin
// suspend fun -- try/catch
suspend fun fetchLatestNews(): List<Article> {
    return try {
        remoteDataSource.fetchLatestNews().map { it.toArticle() }
    } catch (e: IOException) {
        throw NewsNetworkException("Network unavailable", cause = e)
    }
}

// Flow -- catch operator
fun getLatestNewsStream(): Flow<List<Article>> =
    localDataSource.getNewsStream()
        .catch { e -> throw NewsStorageException("DB read failed", cause = e) }
        .map { entities -> entities.map { it.toArticle() } }
```

The UI layer handles these in `viewModelScope.launch`:

```kotlin
viewModelScope.launch {
    try {
        val articles = newsRepository.fetchLatestNews()
        _uiState.update { it.copy(newsItems = articles.toUiModel()) }
    } catch (e: NewsNetworkException) {
        _uiState.update { it.copy(userMessages = listOf(Message(e))) }
    }
}
```

### Result wrapper pattern (alternative)

For APIs where you prefer explicit success/failure types over exceptions:

```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
    data object Loading : Result<Nothing>()
}

// Repository returns Result<T>
suspend fun getLatestNews(): Result<List<Article>> = try {
    Result.Success(remoteDataSource.fetchLatestNews().map { it.toArticle() })
} catch (e: Exception) {
    Result.Error(e)
}
```

The data layer can define custom exception types (e.g. `UserNotAuthenticatedException`)
so upper layers can handle them specifically.

---

## Multiple Repositories

A repository can depend on other repositories when aggregating data:

```kotlin
class UserRepository @Inject constructor(
    private val loginRepository: LoginRepository,
    private val preferencesRepository: UserPreferencesRepository,
)
```

---

## Testing

### Unit tests with fakes

```kotlin
class NewsRepositoryTest {
    private val fakeRemoteDataSource = FakeNewsRemoteDataSource()
    private val fakeLocalDataSource = FakeNewsLocalDataSource()
    private val repository = NewsRepository(fakeRemoteDataSource, fakeLocalDataSource)

    @Test
    fun getLatestNews_returnsCachedData() = runTest {
        fakeRemoteDataSource.setArticles(listOf(article1, article2))
        val result = repository.getLatestNews()
        assertEquals(listOf(article1, article2), result)
    }
}
```

### Integration tests

- **Room**: Use `Room.inMemoryDatabaseBuilder()` with `allowMainThreadQueries()` for tests
- **Network**: Use `MockWebServer` to test API parsing without hitting real servers
- **Prefer fakes over mocks** -- fakes are simpler, more readable, and expose interface gaps

---

## PR Review Checklist

### Repository

- [ ] Repository exists even if only one data source is used -- not skipped for "simplicity"
- [ ] UI and domain layers access data only through repositories -- never data sources directly
- [ ] Repository is the SSOT: local DB used as the observable source, not network directly
- [ ] Repository does not depend on ViewModels, UI state types, or Compose types
- [ ] Data exposed from repository uses `val`-only `data class` -- not mutable

### Data Sources

- [ ] Each data source interacts with exactly one physical source
- [ ] Data source is not accessed from outside the data layer
- [ ] Data source named after data type + source type (not implementation)

### Models

- [ ] Network DTOs are not used as UI state -- mapped at repository boundary
- [ ] Room entities are not passed to the UI layer
- [ ] App/domain model defined in repository layer or `core:model`

### Error Handling

- [ ] `suspend fun` errors wrapped in `try/catch` -- not silently swallowed
- [ ] `Flow` errors handled with the `catch` operator -- not `try/catch` on the collector
- [ ] Custom exception types used where the UI needs to handle errors specifically
- [ ] Network failures don't leave local state in an inconsistent/stale state

### Threading

- [ ] Long-blocking work dispatched via `withContext(ioDispatcher)` inside the data class -- not in
  the ViewModel
- [ ] `CoroutineDispatcher` is injected -- not hardcoded as `Dispatchers.IO`
- [ ] Repository and data sources are main-safe (safe to call from main thread)
- [ ] In-memory caches protected with `Mutex` when accessed from multiple coroutines
- [ ] ViewModel does NOT specify `Dispatchers.IO` in `launch` when calling a main-safe repository

### Lifecycle & Scoping

- [ ] App-oriented operations use an injected application-scoped `CoroutineScope` -- not
  `viewModelScope`
- [ ] Business-critical operations use `WorkManager` -- not `viewModelScope` or app scope
- [ ] Repository scoping matches its data lifetime (app-wide vs feature-flow)

### Naming

- [ ] Repository: `<DataType>Repository`
- [ ] Remote source: `<DataType>RemoteDataSource`
- [ ] Local source: `<DataType>LocalDataSource`
- [ ] Stream methods: `get{Model}Stream(): Flow<Model>`

---

## Anti-Patterns

| Anti-pattern                                                           | Why it's wrong                             | Fix                                                                                 |
|------------------------------------------------------------------------|--------------------------------------------|-------------------------------------------------------------------------------------|
| `Activity` / `ViewModel` calling a `DataSource` directly               | Bypasses SSOT and caching                  | Always call through `Repository`                                                    |
| Raw `ArticleApiModel` or `ArticleEntity` in `UiState`                  | Tight coupling, breaks on schema change    | Map to `ArticleUi` at ViewModel boundary                                            |
| `var` fields in repository-exposed models                              | Breaks immutability, unsafe across threads | Use `data class` with `val`                                                         |
| `Dispatchers.IO` hardcoded in data class                               | Untestable                                 | Inject `CoroutineDispatcher`                                                        |
| `try/catch` around a `Flow` collector                                  | Doesn't catch upstream exceptions in flows | Use the `catch` operator on the Flow instead                                        |
| Exception caught and discarded (`catch { }`)                           | UI can't recover from unknown failure      | Propagate as typed exception or `Result`                                            |
| Repository that holds no data, just proxies one source                 | Still create it -- future-proofing SSOT    | Repository with single data source is correct pattern (may merge for trivial cases) |
| `WorkManager` used for UI-oriented short tasks                         | Overkill, survives when it shouldn't       | Use `viewModelScope` for UI-tied work                                               |
| Data source named after implementation (`SharedPreferencesDataSource`) | Breaks when you swap the impl              | Name after data type + source type (`NewsLocalDataSource`)                          |
