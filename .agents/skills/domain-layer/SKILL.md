---
name: domain-layer
description: >-
  Google's recommended Domain Layer architecture for Android. Use this skill when
  deciding whether to add a domain layer, designing use cases, reviewing use case
  implementations, or checking that business logic is correctly placed between
  the UI and data layers. Based on developer.android.com/topic/architecture/domain-layer
  and the official Android architecture recommendations page.
license: Complete terms in LICENSE.txt
metadata:
  author: Google LLC
  keywords:
    - android
    - architecture
    - domain-layer
    - use-case
    - interactor
    - business-logic
    - operator-invoke
    - coroutines
    - testability
---

# Android Architecture -- Domain Layer

**Source of truth:
** [developer.android.com/topic/architecture/domain-layer](https://developer.android.com/topic/architecture/domain-layer)

## TajsOS Scope Note

This is Android domain-layer guidance. Apply it as reference, while keeping TajsOS KMP module
boundaries and `commonMain` ownership rules as the default baseline.

## References

- *[Domain layer](references/android/topic/architecture/domain-layer.md)* -- full domain layer
  documentation with detailed code examples

---

## The Domain Layer's Job

The domain layer is an **optional** middle layer between UI and data:

```
UI Layer
   |  (calls use cases)
   v
Domain Layer   <- use cases / interactors
   |  (calls repositories)
   v
Data Layer
```

It owns:

- Complex business logic that would otherwise bloat a ViewModel
- Business logic reused across multiple ViewModels

It must **not** own:

- UI logic (screen layout, show/hide decisions)
- Android framework dependencies
- Mutable state

Benefits of a domain layer:

- Avoids code duplication
- Improves readability in classes that consume use cases
- Improves testability (test logic independently)
- Avoids large classes by splitting responsibilities

---

## When to Add It -- **Recommended in Large Apps**

Add a domain layer (use cases) when:

1. **Reuse** -- The same business logic is needed by 2+ ViewModels
2. **Complexity** -- A ViewModel contains complex multi-step business operations that are hard to
   test inline
3. **Testability** -- You want to test the business logic completely independently from the
   ViewModel and UI

**Do NOT add a use case** that merely proxies a single repository call with no
additional logic. This adds indirection without benefit:

```kotlin
// BAD -- Useless use case -- just delegates, no logic added
class GetUserUseCase(private val repo: UserRepository) {
    operator fun invoke(id: String) = repo.getUser(id)
}

// GOOD -- In this case, ViewModel calls repository directly
class ProfileViewModel(private val userRepository: UserRepository) : ViewModel()
```

### Alternative: Room relationships or a combined repository

If the business logic is simply combining data from a single database, Room
relationships may be a better solution. Consider creating a combined repository
(e.g., `NewsWithAuthorsRepository`) instead of a use case.

### Restricting UI -> Data layer access

Some teams force all data access through the domain layer. This ensures use case
logic (e.g., analytics logging) is never bypassed. However, it forces you to add
use cases even for trivial calls. The decision depends on your codebase -- add use
cases only when required, unless your team explicitly adopts the stricter rule.

---

## Use Case Design

### One class, one action

Each use case does **exactly one thing**. Name it:
`[verb in present tense] + [noun/what (optional)] + UseCase`.

```kotlin
GetLatestNewsUseCase
LogOutUserUseCase
FormatDateUseCase
GetUserNewsResourcesUseCase
PlaceOrderUseCase
MakeLoginRequestUseCase
```

### `operator fun invoke()`

Implement `invoke()` with the `operator` modifier so the use case is called as a function:

```kotlin
class GetLatestNewsWithAuthorsUseCase @Inject constructor(
    private val newsRepository: NewsRepository,
    private val authorsRepository: AuthorsRepository,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    // Operator invoke = callable as a function: getLatestNewsWithAuthorsUseCase()
    suspend operator fun invoke(): List<ArticleWithAuthor> =
        withContext(defaultDispatcher) {
            val news = newsRepository.fetchLatestNews()
            news.map { article ->
                val author = authorsRepository.getAuthor(article.authorId)
                ArticleWithAuthor(article, author)
            }
        }
}
```

Call site in ViewModel is clean -- no `.execute()`, no `.run()`:

```kotlin
viewModelScope.launch {
    val articles = getLatestNewsWithAuthorsUseCase()  // <- reads like a function
    _uiState.update { it.copy(articles = articles.toUiModel()) }
}
```

### Use cases calling other use cases

Valid when it genuinely simplifies reuse -- reusable logic can itself be encapsulated
in a use case:

```kotlin
class GetUserNewsResourcesUseCase @Inject constructor(
    private val newsRepository: NewsRepository,
    private val bookmarksRepository: BookmarksRepository,
    private val formatDateUseCase: FormatDateUseCase,   // <- another use case
) {
    operator fun invoke(userId: String): Flow<List<UserNewsResource>> =
        newsRepository.getNewsResourcesStream()
            .combine(bookmarksRepository.getBookmarkedIds(userId)) { news, bookmarks ->
                news.map { article ->
                    UserNewsResource(
                        article = article,
                        isBookmarked = article.id in bookmarks,
                        formattedDate = formatDateUseCase(article.publishedAt),
                    )
                }
            }
}
```

---

## Rules for Use Cases

| Rule         | Requirement                                                                                                                            |
|--------------|----------------------------------------------------------------------------------------------------------------------------------------|
| Naming       | `VerbNoun(Optional)UseCase` -- verb in present tense                                                                                   |
| Entry point  | `operator fun invoke()`                                                                                                                |
| State        | No mutable state -- use cases are stateless                                                                                            |
| Android deps | None -- pure Kotlin only                                                                                                               |
| Main-safety  | Must be safe to call from main thread; dispatch long work via `withContext`                                                            |
| Dependencies | Repositories and other use cases only -- never ViewModels or UI classes                                                                |
| Lifecycle    | Use cases have no lifecycle of their own. They are scoped to the class that uses them.                                                 |
| Scoping      | Do NOT scope use cases (`@Singleton`, `@ViewModelScoped`). Create a new instance each time via DI, since they contain no mutable data. |
| Testing      | Test independently with fake repositories -- no ViewModel needed                                                                       |

---

## Lifecycle

Use cases **do not have their own lifecycle**. They are scoped to the class that
uses them. Because use cases should not contain mutable data, you should create a
**new instance every time** you pass one as a dependency.

Use cases can be called from:

- ViewModels (most common)
- Services
- The `Application` class
- Other platforms sharing the codebase (TV, Wear OS)

This broad reusability is one of the main reasons to extract business logic into
use cases.

---

## Threading in Use Cases

Use cases must be main-safe. Dispatch heavy computation to `Dispatchers.Default`:

```kotlin
class SortArticlesUseCase @Inject constructor(
    private val defaultDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(articles: List<Article>): List<Article> =
        withContext(defaultDispatcher) {
            articles.sortedByDescending { it.publishedAt }
        }
}
```

Inject the dispatcher -- never hardcode. This makes use cases unit-testable with
`UnconfinedTestDispatcher`.

> **Note:** Before dispatching inside a use case, check whether the heavy work
> belongs in the data layer instead. Resource-intensive operations (network, disk)
> are typically the data layer's responsibility.

---

## Domain Models

Use cases may define domain models that are independent of:

- Network DTOs (data layer)
- Room entities (data layer)
- `UiState` classes (UI layer)

```kotlin
// Domain model -- lives in domain layer or core:model
data class ArticleWithAuthor(
    val article: Article,
    val author: Author,
)

// ViewModel maps to UI model -- domain model doesn't know about UI
fun ArticleWithAuthor.toUiModel(): ArticleWithAuthorUi = ...
```

---

## PR Review Checklist

### Is a use case needed?

- [ ] Use case is justified: logic is either reused by 2+ ViewModels, or complex enough to warrant
  isolation
- [ ] Not a one-line repository proxy -- if so, ViewModel should call the repository directly
- [ ] Considered alternatives: Room relationships or a combined repository where appropriate

### Use case implementation

- [ ] Named `VerbNounUseCase` -- verb first, then noun
- [ ] Implements `operator fun invoke()` -- not a custom method name like `execute()` or `run()`
- [ ] Contains no mutable state -- fields are all `val` or absent
- [ ] Contains zero Android imports (`android.*`, `androidx.*`) -- pure Kotlin
- [ ] Long work dispatched with `withContext(dispatcher)` -- use case is main-safe
- [ ] `CoroutineDispatcher` is injected -- not hardcoded as `Dispatchers.Default`
- [ ] Only depends on repositories and other use cases -- not on ViewModels, Context, or UI types

### Scoping

- [ ] Use case is **not** scoped (`@Singleton`, `@ViewModelScoped`) -- a new instance is created
  each time via DI
- [ ] Use case is `internal` if only consumed within the same module

### Testing

- [ ] Use case is tested directly (not via ViewModel) with fake repositories
- [ ] All code paths (success, error, empty) have a corresponding test

---

## Anti-Patterns

| Anti-pattern                                             | Why it's wrong                                             | Fix                                                                    |
|----------------------------------------------------------|------------------------------------------------------------|------------------------------------------------------------------------|
| `GetUserUseCase` that just calls `repo.getUser()`        | No logic added -- useless indirection                      | Call repository from ViewModel directly                                |
| Use case with `var` fields or `@Singleton` mutable state | Use cases must be stateless; docs say create new instances | Remove state; move to repository if needed; remove scoping annotations |
| Use case depending on a ViewModel                        | Inverts the dependency direction                           | Use case -> repository only                                            |
| `execute()` or `run()` instead of `invoke()`             | Non-standard; noisier call sites                           | Implement `operator fun invoke()`                                      |
| Android imports in use case                              | Domain layer must be Android-free                          | Move to data layer or inject as interface                              |
| Use case created for every single repository call        | Unnecessary ceremony                                       | Use cases only when logic is complex or reused                         |
| Hardcoded `Dispatchers.Default`                          | Untestable                                                 | Inject `CoroutineDispatcher`                                           |
| `@Singleton` use case                                    | Docs say use cases have no lifecycle; create new instances | Remove scope annotation; let DI create fresh instances                 |
