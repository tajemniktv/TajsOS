import re

file_path = "shared/src/commonTest/kotlin/com/tajemniktv/tajsos/data/PreferencesRepositoryTest.kt"

with open(file_path, "r") as f:
    content = f.read()

# The error was TurbineTimeoutCancellationException in packManagement_updatesOwnedAndEnabledPacks.
# This means there was no emission where we expected one. Let's trace it.
# 1. setPackOwned(customPack, true) -> awaitItem()  (customPack is STUDENT, default is not owned, so it should emit)
# 2. setPackEnabled(customPack, true) -> awaitItem() (it was not enabled, so it should emit)
# 3. setPackEnabled(customPack, false) -> awaitItem() (it was enabled, so it should emit)
# 4. setPackEnabled(customPack, true) -> awaitItem() (it was disabled, so it should emit)
# 5. setPackOwned(customPack, false) -> awaitItem() (it was owned, so it should emit)
#
# Actually, the DataStore `edit` doesn't emit if the value didn't actually change the final serialized state.
# But here it does change. Let's see if our FakeDataStore is missing an emit when the reference might be the same?
# Wait! In PreferencesRepository, `setPackEnabled` and `setPackOwned` update `PreferencesKeys.ENABLED_PACKS` and `OWNED_PACKS`.
# If `enabledPacks` Flow (which we are testing) emits a `PackRegistry`, maybe `ownedPackKeys` and `enabledPackKeys` are being compared with `.equals()` in `FakeDataStore`'s `MutableStateFlow`?
# In `PreferencesRepository`, `val current = preferences[PreferencesKeys.ENABLED_PACKS].orEmpty().toMutableSet()`, we modify it and then `preferences[PreferencesKeys.ENABLED_PACKS] = current`. Since it's a new set, it should emit.
#
# Ah, I replaced:
# `repository.setPackOwned(freePack, true)`
# `// No emission`
# `repository.setPackOwned(freePack, false)`
# `// No emission`
# `cancelAndIgnoreRemainingEvents()`
# What if it DID emit when we set it to true? If `freePack` is `MAINTENANCE`, it IS in `defaultFreePackKeys`. If `OWNED_PACKS` is null, it falls back to `defaultFreePackKeys`. If we call `setPackOwned` with `true`, it writes it to `OWNED_PACKS` explicitly.
# Oh, in `setPackOwned`:
# val ownedSet = (preferences[PreferencesKeys.OWNED_PACKS] ?: AppPack.defaultFreePackKeys).toMutableSet()
# If `owned` is true, `ownedSet += pack.key` (does nothing because it's already there)
# Then `preferences[PreferencesKeys.OWNED_PACKS] = ownedSet`
# For `MutableStateFlow`, if `newData == oldData`, it does not emit.
# The previous state was `emptyPreferences()` which resulted in `PackRegistry(defaultFreePackKeys, defaultFreePackKeys)`.
#
# So `cancelAndIgnoreRemainingEvents()` might be right, but maybe the timeout is earlier?
# The error says "at PreferencesRepositoryTest.kt:296".
# Wait, 296 is `fun packManagement_updatesOwnedAndEnabledPacks() = runTest {`.
# The stack trace says:
# Caused by: app.cash.turbine.TurbineTimeoutCancellationException at channel.kt:123
# Caused by: app.cash.turbine.TurbineTimeoutCancellationException at PreferencesRepositoryTest.kt:296
# This points to the line where `test { ... }` block starts or where the `runTest` starts.

# To be safe, instead of `.test {}`, we can just collect the flow manually, or just use `first()` after each mutation, because Turbine with DataStore can be tricky if emissions don't happen due to distinctUntilChanged logic implicitly happening somewhere, or if we accidentally consume events late.
# Or we can just read the first value each time using `.first()`!
# Since `enabledPacks` is a Flow derived from `DataStore.data`, `first()` will just give us the current state.
