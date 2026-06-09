# Architectural Specification & Feature Implementation Report

This document specifies the design, architecture, localization, UI/UX guidelines, edge-to-edge inset handling, memory safety, and test suites for the features implemented in the Atomic Periodic Table Android application.

---

## 1. Feature Specifications & Technical Design

### 1.1 Element Notes
*   **Purpose**: Allows users to save personal notes, observations, or studies for each chemical element on the element details screen.
*   **Integration**: Embedded inside [ElementInfoAct](file:///Users/loitran/AndroidStudioProjects/@mckimquyen/@playstore/@prodution/@ad/260411_Atomic-Periodic-Table.Android/app/src/main/java/com/mckimquyen/atomicPeriodicTable/act/ElementInfoAct.kt).
*   **UI/UX Layout**:
    *   Flat filled CardView (`notesCard`) styled with `?attr/colorSurfaceVariant` and `app:cardElevation="0dp"`.
    *   Outlined Material TextInputLayout (`notesInputLayout`) following Material Design 3 guidelines.
    *   Multi-line TextInputEditText (`notesInput`) supporting copy-paste and text scaling.
    *   Material Button (`notesSaveBtn`) aligned to the bottom-right.
*   **Storage Architecture**:
    *   Managed by `NotesPref.kt` using Android's lightweight SharedPreferences.
    *   Stored asynchronously via `apply()` inside Lifecycle coroutine scopes to prevent Main thread blocks or memory leaks.
    *   Key format: `notes_<element_name_lowercase>`.

### 1.2 Chemical Equation Balancer
*   **Purpose**: Automatically balances chemical equations (e.g., `H2 + O2 = H2O` to `2 H2 + O2 = 2 H2O`).
*   **Parser & Algebraic Algorithm**:
    1.  **Tokenizer**: Splits the input by `=` or `->` into reactants and products.
    2.  **Formula Parser**: Recursively parses each chemical formula (supporting nested brackets) into element-count maps.
    3.  **Linear System construction**: Builds a matrix $A$ where rows represent element conservation, and columns represent compounds.
    4.  **Gaussian Elimination with Fraction Arithmetic**: Solves the homogeneous system $A x = 0$ using fraction math (`Fraction.kt`) to ensure exact integer arithmetic (preventing IEEE 754 float inaccuracies).
    5.  **LCM Normalization**: Finds the Least Common Multiple of all denominators to scale variables to the smallest integer coefficients.
*   **UI/UX Layout**:
    *   TextInputLayout with helper text.
    *   Flat Material Button (`balancerBtn`) for calculation.
    *   Faded-in slide-up flat CardView (`balancerResultCard`) with `app:cardElevation="0dp"` showing the balanced equation.

### 1.3 Periodic Table Quiz
*   **Purpose**: An interactive 10-question multiple-choice quiz testing players on element details.
*   **State Machine**:
    *   *Init*: Randomly selects 10 elements from the element model dataset.
    *   *Question Generator*: Rotates randomly between three question types:
        1.  Atomic Number: "What is the atomic number of [Element]?"
        2.  Symbol: "What is the symbol of [Element]?"
        3.  Category: "Which category does [Element] belong to?"
    *   *Option Generator*: Fetches the correct answer and generates three distinct distractors from the element list (guaranteeing no duplicate choices).
    *   *Interactive Transitions*:
        *   **Correct Selection**: Selected card lights up in Green (`#C8E6C9` light / `#1B5E20` dark) with high-contrast text.
        *   **Incorrect Selection**: Selected card turns Red (`#FFCDD2` light / `#B71C1C` dark); correct card turns Green.
        *   **Dimming & Shrink (Material M3 feedback)**: Incorrect, unselected choice cards shrink (`scale: 0.95f`) and fade (`alpha: 0.5f`) to guide the user's focus.
        *   *Auto-advance*: Wait 1.5 seconds, then load the next question with a smooth animation.

### 1.4 Advanced Molar Mass Calculator
*   **Purpose**: Computes the molar mass of complex formulas (including nested brackets like `Al2(SO4)3`) with mass breakdown composition percentages.
*   **Algorithm**:
    *   Stack-based parser tokenizes element symbols, digits, and group multipliers.
    *   Distributes multipliers recursively across parentheses `()`, brackets `[]`, and braces `{}`.
    *   Multiplies counts by weights retrieved from the corrected `ElementWeightCache`.
*   **UI/UX Layout**:
    *   Input CardView using `?attr/colorSurfaceVariant` and `app:cardElevation="0dp"`.
    *   Dynamic Breakdown List: Displays contribution breakdown (e.g. `2 atoms × 1.008 g/mol = 2.016 g/mol`) with horizontal `ProgressBar` elements representing weight percentages.

---

## 2. Localization Strategy (100% Native XML Translation)

All string keys are strictly localized inside the Android resource directories. Dynamic elements are translated at runtime:

| Key | English (`values`) | Vietnamese (`values-vi`) | Traditional Chinese (`values-zh-rTW`) |
| :--- | :--- | :--- | :--- |
| `notes_title` | Personal Notes | Ghi chú cá nhân | 個人筆記 |
| `notes_hint` | Add your notes or memories about this element… | Nhập ghi chú hoặc kiến thức bổ sung về nguyên tố này… | 新增您關於此元素的筆記或回憶… |
| `notes_save` | Save Note | Lưu ghi chú | 保存備註 |
| `balancer_title` | Chemical Equation Balancer | Cân bằng phương trình hóa học | 化學方程式配平器 |
| `quiz_score` | Score: %1$d/%2$d | Điểm: %1$d/%2$d | 得分：%1$d/%2$d |
| `molar_mass_composition` | Element Composition Breakdown: | Tỷ lệ phần trăm khối lượng: | 元素組成分解： |

Dynamic lookups (e.g. Category names, Element names) use native string resources with reflection-like ID lookup:
```kotlin
val resId = context.resources.getIdentifier("element_name_${symbol.lowercase()}", "string", context.packageName)
```

---

## 3. UI/UX Styles, Theme & Insets (Material You)

### 3.1 Material You Styling Guidelines
*   **Zero Shadows**: All new screen components set `app:cardElevation="0dp"` and `android:elevation="0dp"` to adhere to flat Material 3 / Material You aesthetics.
*   **Surface Contrast**: Contrast is established using background tints:
    *   Default screen background: `?attr/backgroundColor` (transparent status/nav bars).
    *   Normal container cards: `?attr/colorSurfaceVariant` (filled card look).
    *   Highlighted/Result cards: `?attr/colorPrimaryContainer` with `?attr/colorOnPrimaryContainer` for text.
*   **Edge-to-Edge Padding (System Insets)**:
    Each custom activity overrides `onApplySystemInsets` to avoid layouts clipping under the transparent Status Bar and Navigation Bar:
    ```kotlin
    override fun onApplySystemInsets(top: Int, bottom: Int, left: Int, right: Int) {
        // Extend top toolbar height to prevent overlap with the Status Bar
        val params = binding.commonTitleBack.layoutParams
        params.height = top + resources.getDimensionPixelSize(R.dimen.title_bar)
        binding.commonTitleBack.layoutParams = params

        // Pad the bottom of the ScrollView to prevent overlap with the Navigation Bar
        val topPadding = resources.getDimensionPixelSize(R.dimen.margin)
        val bottomPadding = bottom + resources.getDimensionPixelSize(R.dimen.margin)
        binding.scrollView.setPadding(0, topPadding, 0, bottomPadding)
    }
    ```

### 3.2 Premium Animations
*   **Decelerating Slide-Up Fade**: Result cards animate using `Utils.slideUpFadeIn(view, duration)`, translating upwards by `30dp` while fading in from `0.0f` to `1.0f`.
*   **Spring Option Bounce**: Correct cards bounce slightly using scale spring animations (`scaleX`/`scaleY` up to `1.05f` then back to `1.02f`).

---

## 4. Testing Matrix & Code Quality

Automated JUnit and Espresso test suites verify correctness:

1.  **Unit Tests (JUnit)**:
    *   [ChemicalParserAndBalancerTest.kt](file:///Users/loitran/AndroidStudioProjects/@mckimquyen/@playstore/@prodution/@ad/260411_Atomic-Periodic-Table.Android/app/src/test/java/com/mckimquyen/atomicPeriodicTable/util/ChemicalParserAndBalancerTest.kt): Verifies fraction calculations, LCM coefficients, and parsing edge cases.
2.  **UI/Widget Tests (Espresso & ActivityScenario)**:
    *   [CalculatorActTest.kt](file:///Users/loitran/AndroidStudioProjects/@mckimquyen/@playstore/@prodution/@ad/260411_Atomic-Periodic-Table.Android/app/src/androidTest/java/com/mckimquyen/atomicPeriodicTable/act/CalculatorActTest.kt)
    *   [EquationBalancerActTest.kt](file:///Users/loitran/AndroidStudioProjects/@mckimquyen/@playstore/@prodution/@ad/260411_Atomic-Periodic-Table.Android/app/src/androidTest/java/com/mckimquyen/atomicPeriodicTable/act/EquationBalancerActTest.kt)
    *   [QuizActTest.kt](file:///Users/loitran/AndroidStudioProjects/@mckimquyen/@playstore/@prodution/@ad/260411_Atomic-Periodic-Table.Android/app/src/androidTest/java/com/mckimquyen/atomicPeriodicTable/act/QuizActTest.kt)
3.  **Integration Tests (Espresso)**:
    *   [FeaturesIntegrationTest.kt](file:///Users/loitran/AndroidStudioProjects/@mckimquyen/@playstore/@prodution/@ad/260411_Atomic-Periodic-Table.Android/app/src/androidTest/java/com/mckimquyen/atomicPeriodicTable/FeaturesIntegrationTest.kt)
    *   [NavigationIntegrationTest.kt](file:///Users/loitran/AndroidStudioProjects/@mckimquyen/@playstore/@prodution/@ad/260411_Atomic-Periodic-Table.Android/app/src/androidTest/java/com/mckimquyen/atomicPeriodicTable/NavigationIntegrationTest.kt)
