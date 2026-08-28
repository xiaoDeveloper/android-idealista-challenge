# Tasks: Detail Consistency

- [X] T001 [US1] Add failing selected-listing and matching-enrichment regression coverage in `app/src/test/java/com/xiao/idealistachallenge/data/repository/AdRepositoryTest.kt`.
- [X] T002 [US1] Preserve list operation/address data and implement cached-or-refreshed selected-listing resolution with identity-gated enrichment in `app/src/main/java/com/xiao/idealistachallenge/data/remote/IdealistaDtos.kt`, `app/src/main/java/com/xiao/idealistachallenge/model/PropertyAd.kt`, `app/src/main/java/com/xiao/idealistachallenge/model/PropertyDetails.kt`, and `app/src/main/java/com/xiao/idealistachallenge/data/repository/AdRepository.kt`.
- [X] T003 [US1] Add truthful address presentation while retaining coordinate omission in `app/src/main/java/com/xiao/idealistachallenge/ui/detail/DetailPresentation.kt`, `app/src/main/java/com/xiao/idealistachallenge/ui/detail/DetailFragment.kt`, and `app/src/main/res/layout/fragment_detail.xml`.
- [X] T004 [US1] Add focused presentation/layout/model/ViewModel regression coverage in `app/src/test/java/com/xiao/idealistachallenge/ui/detail/` and `app/src/test/java/com/xiao/idealistachallenge/model/PropertyModelsTest.kt`.
- [X] T005 Run focused tests, `./gradlew.bat testDebugUnitTest`, `./gradlew.bat assembleDebug`, `./gradlew.bat lintDebug`, and `git diff --check`; reconcile this file only from fresh evidence.
