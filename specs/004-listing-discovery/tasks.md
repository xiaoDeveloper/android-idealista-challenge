# Tasks: Listing Discovery Controls

- [X] T001 Enable the minimal Compose compiler, BOM, runtime, Material 3, lifecycle, and test configuration in `gradle/libs.versions.toml`, `build.gradle.kts`, and `app/build.gradle.kts`.
- [X] T002 [P] [US1] Add focused failing category, stable-order, sorting, reset/preservation, favorite-update, zero-result, and single-request tests in `app/src/test/java/com/xiao/idealistachallenge/ui/listing/ListingViewModelTest.kt`.
- [X] T003 [P] [US1] Add failing Compose semantics/callback coverage and the XML host contract in `app/src/androidTest/java/com/xiao/idealistachallenge/ui/listing/ListingDiscoveryControlsTest.kt` and `app/src/test/java/com/xiao/idealistachallenge/ui/listing/ListingLayoutContractTest.kt`.
- [X] T004 [US1] Implement discovery selection and locally derived rows in `app/src/main/java/com/xiao/idealistachallenge/ui/listing/ListingViewModel.kt`.
- [X] T005 [US1] Add the stateless themed controls and Spanish labels in `app/src/main/java/com/xiao/idealistachallenge/ui/listing/ListingDiscoveryControls.kt` and `app/src/main/res/values/strings.xml`.
- [X] T006 [US1] Add and lifecycle-wire the single Compose host while preserving the XML RecyclerView in `app/src/main/res/layout/fragment_listing.xml` and `app/src/main/java/com/xiao/idealistachallenge/ui/listing/ListingFragment.kt`.
- [X] T007 Run focused listing tests, the focused Compose instrumentation test, `./gradlew.bat testDebugUnitTest`, `./gradlew.bat assembleDebug`, `./gradlew.bat lintDebug`, and `git diff --check`; update this file only from fresh evidence.

