package com.xiao.idealistachallenge

import android.view.View
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeLeft
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.ext.junit.rules.ActivityScenarioRule
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matcher
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PropertyBrowsingJourneyTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun clearFavorites() = runBlocking {
        app().container.favoriteDatabase.clearAllTables()
    }

    @After
    fun removeFavorites() = runBlocking {
        app().container.favoriteDatabase.clearAllTables()
    }

    @Test
    fun listingPagerSwipeKeepsNavigationAndFavoriteIndependentThenMediaTapOpensDetail() {
        onView(withId(R.id.listingRecyclerView)).check(matches(isDisplayed()))

        onView(withId(R.id.listingRecyclerView)).perform(
            performChildActionAtPosition(0, R.id.listingImagePager, swipeLeft()),
        )
        onView(withId(R.id.listingRecyclerView)).perform(
            assertChildTextAtPosition(0, R.id.listingImagePosition, "2 / 3"),
            assertChildVisibilityAtPosition(0, R.id.favoriteDate, false),
        )

        onView(withId(R.id.listingRecyclerView)).perform(
            performChildActionAtPosition(0, R.id.favoriteButton, click()),
            assertChildVisibilityAtPosition(0, R.id.favoriteDate, true),
        )

        onView(withId(R.id.listingRecyclerView)).perform(
            performChildActionAtPosition(0, R.id.listingImagePager, click()),
        )
        onView(withId(R.id.detailFavoriteButton)).check(matches(isDisplayed()))
    }

    private fun app(): App = activityRule.scenario.let {
        androidx.test.platform.app.InstrumentationRegistry.getInstrumentation()
            .targetContext.applicationContext as App
    }
}

private fun performChildActionAtPosition(
    position: Int,
    @IdRes childViewId: Int,
    action: androidx.test.espresso.ViewAction,
): androidx.test.espresso.ViewAction = object : androidx.test.espresso.ViewAction {
    override fun getConstraints(): Matcher<View> = org.hamcrest.Matchers.instanceOf(RecyclerView::class.java)

    override fun getDescription(): String = "perform ${action.description} on child $childViewId at $position"

    override fun perform(uiController: androidx.test.espresso.UiController, view: View) {
        val holder = checkNotNull((view as RecyclerView).findViewHolderForAdapterPosition(position)) {
            "No ViewHolder for adapter position $position"
        }
        action.perform(uiController, checkNotNull(holder.itemView.findViewById(childViewId)))
    }
}

private fun assertChildTextAtPosition(
    position: Int,
    @IdRes childViewId: Int,
    expected: String,
): androidx.test.espresso.ViewAction = object : androidx.test.espresso.ViewAction {
    override fun getConstraints(): Matcher<View> = org.hamcrest.Matchers.instanceOf(RecyclerView::class.java)

    override fun getDescription(): String = "assert child $childViewId at $position has text $expected"

    override fun perform(uiController: androidx.test.espresso.UiController, view: View) {
        val text = childAtPosition(view as RecyclerView, position, childViewId) as TextView
        check(text.text.toString() == expected) { "Expected $expected but was ${text.text}" }
    }
}

private fun assertChildVisibilityAtPosition(
    position: Int,
    @IdRes childViewId: Int,
    expectedVisible: Boolean,
): androidx.test.espresso.ViewAction = object : androidx.test.espresso.ViewAction {
    override fun getConstraints(): Matcher<View> = org.hamcrest.Matchers.instanceOf(RecyclerView::class.java)

    override fun getDescription(): String = "assert child $childViewId at $position visibility is $expectedVisible"

    override fun perform(uiController: androidx.test.espresso.UiController, view: View) {
        check(childAtPosition(view as RecyclerView, position, childViewId).visibility ==
            if (expectedVisible) View.VISIBLE else View.GONE)
    }
}

private fun childAtPosition(recyclerView: RecyclerView, position: Int, @IdRes childViewId: Int): View {
    val holder = checkNotNull(recyclerView.findViewHolderForAdapterPosition(position)) {
        "No ViewHolder for adapter position $position"
    }
    return checkNotNull(holder.itemView.findViewById(childViewId)) {
        "No child $childViewId at $position"
    }
}
