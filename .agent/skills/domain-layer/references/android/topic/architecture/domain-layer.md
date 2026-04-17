# Domain Layer

Source: https://developer.android.com/topic/architecture/domain-layer

The domain layer is an optional layer that sits between the UI layer and the data layer. It
encapsulates complex business logic or simple business logic reused by multiple ViewModels. Not all
apps require a domain layer.

---

## Benefits

1. Avoids code duplication
2. Improves readability in classes that use domain layer classes
3. Improves testability of the app
4. Avoids large classes by allowing you to split responsibilities

---

## Naming Conventions

```
[verb in present tense] + [noun/what (optional)] + UseCase
```

Examples: `FormatDateUseCase`, `LogOutUserUseCase`, `GetLatestNewsWithAuthorsUseCase`,
`MakeLoginRequestUseCase`.

---

## Dependencies

Use cases typically depend on repository classes:

```kotlin
class GetLatestNewsWithAuthorsUseCase(
    private val newsRepository: NewsRepository,
    private val authorsRepository: AuthorsRepository
)
```

Use cases can depend on other use cases, creating multiple levels:

```kotlin
class GetLatestNewsWithAuthorsUseCase(
    private val newsRepository: NewsRepository,
    private val authorsRepository: AuthorsRepository,
    private val formatDateUseCase: FormatDateUseCase
)
```

---

## The Invoke Operator

Make use case instances callable as functions with `operator fun invoke()`:

```kotlin
class FormatDateUseCase(userRepository: UserRepository) {
    private val formatter = SimpleDateFormat(
        userRepository.getPreferredDateFormat(),
        userRepository.getPreferredLocale()
    )

    operator fun invoke(date: Date): String {
        return formatter.format(date)
    }
}
```

Usage:

```kotlin
class MyViewModel(formatDateUseCase: FormatDateUseCase) : ViewModel() {
    init {
        val todaysDate = formatDateUseCase(today)  // Called as a function
    }
}
```

The `invoke()` method is not restricted to any specific signature. It can take any number of
parameters, return any type, and be overloaded.

---

## Lifecycle

- Use cases **don't have their own lifecycle**
- They are **scoped to the class that uses them**
- Create a **new instance every time** you pass one as a dependency (since they shouldn't contain
  mutable data)
- Can be called from UI layer classes, Services, or the `Application` class

---

## Threading

Use cases must be **main-safe**. If performing long-running blocking operations, move logic to
appropriate thread:

```kotlin
class MyUseCase(
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    suspend operator fun invoke(...) = withContext(defaultDispatcher) {
        // Long-running blocking operations happen on a background thread.
    }
}
```

Check if blocking operations would be better placed in the data layer.

---

## Common Tasks

### Reusable Simple Business Logic

Encapsulate repeatable business logic in use cases for centralized changes, isolated testing, and
discoverability. Preferred over static `Util` classes.

### Combining Multiple Repositories

```kotlin
class GetLatestNewsWithAuthorsUseCase(
    private val newsRepository: NewsRepository,
    private val authorsRepository: AuthorsRepository,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    suspend operator fun invoke(): List<ArticleWithAuthor> =
        withContext(defaultDispatcher) {
            val news = newsRepository.fetchLatestNews()
            val result: MutableList<ArticleWithAuthor> = mutableListOf()
            for (article in news) {
                val author = authorsRepository.getAuthor(article.authorId)
                result.add(ArticleWithAuthor(article, author))
            }
            result
        }
}
```

Note: If the database is the source of truth, Room relationships may be a better solution. Consider
creating a `NewsWithAuthorsRepository`.

---

## Data Layer Access Restriction

**Should UI layer be forced through the domain layer?**

Advantages: prevents bypassing domain logic, ensures analytics logging.

Disadvantages: adds use cases even for simple calls, complexity for little benefit.

Recommended: add use cases only when required. If nearly all access already goes through use cases,
enforcing the rule may make sense.

---

## Other Consumers

The domain layer can be reused by Services, `Application` class, and other platforms (TV, Wear)
sharing the codebase.

---

## Testing

Use fake repositories. Test use cases in isolation for better coverage. General testing guidance
applies.

---

## Related Documentation

- [Architecture overview](https://developer.android.com/topic/architecture)
- [Architecture recommendations](https://developer.android.com/topic/architecture/recommendations)
- [UI layer](https://developer.android.com/topic/architecture/ui-layer)
- [Data layer](https://developer.android.com/topic/architecture/data-layer)
