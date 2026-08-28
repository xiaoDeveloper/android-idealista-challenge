package com.xiao.idealistachallenge

import android.app.Activity
import android.view.View
import android.view.ViewTreeObserver
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.IdlingPolicies
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matcher
import org.hamcrest.Matchers.startsWith
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class FavoriteJourneyTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun clearFavorites() = runBlocking {
        IdlingPolicies.setIdlingResourceTimeout(30, TimeUnit.SECONDS)
        app().container.favoriteDatabase.clearAllTables()
    }

    @After
    fun removeFavorites() = runBlocking {
        app().container.favoriteDatabase.clearAllTables()
    }

    @Test
    fun favoriteJourneySynchronizesDateThroughBackStackAndRecreation() {
        awaitView(R.id.listingRecyclerView) { view ->
            view is RecyclerView && view.isShown && view.childCount > 0
        }

        onView(withId(R.id.listingRecyclerView)).perform(clickChildAtPosition(0, R.id.favoriteButton))
        awaitView(R.id.favoriteDate) { it.isShown && (it as TextView).text.isNotBlank() }
        val favoriteDate = textOf(R.id.favoriteDate)

        onView(withId(R.id.listingRecyclerView)).perform(clickChildAtPosition(0, R.id.listingCard))
        awaitView(R.id.detailSavedState) { it.isShown && (it as TextView).text.isNotBlank() }
        onView(withId(R.id.detailSavedState)).check(ViewAssertion { view, noViewFoundException ->
            checkNotNull(view) { noViewFoundException?.message ?: "Favorite date view was not found." }
            check((view as TextView).text.toString() == favoriteDate)
        })

        onView(withContentDescription(startsWith("Quitar vivienda de guardados"))).perform(click())
        awaitView(R.id.detailSavedState) { !it.isShown }
        pressBack()
        awaitView(R.id.favoriteDate) { !it.isShown }

        onView(withId(R.id.listingRecyclerView)).perform(clickChildAtPosition(0, R.id.favoriteButton))
        awaitView(R.id.favoriteDate) { it.isShown && (it as TextView).text.isNotBlank() }
        val recreatedFavoriteDate = textOf(R.id.favoriteDate)

        activityRule.scenario.recreate()
        awaitView(R.id.listingRecyclerView) { view ->
            view is RecyclerView && view.isShown && view.childCount > 0
        }
        awaitView(R.id.favoriteDate) { it.isShown && (it as TextView).text.isNotBlank() }
        onView(withId(R.id.favoriteDate)).check(ViewAssertion { view, noViewFoundException ->
            checkNotNull(view) { noViewFoundException?.message ?: "Favorite date view was not found." }
            check((view as TextView).text.toString() == recreatedFavoriteDate)
        })
    }

    private fun awaitView(@IdRes viewId: Int, predicate: (View) -> Boolean) {
        lateinit var activity: Activity
        activityRule.scenario.onActivity { activity = it }
        val resource = ViewPredicateIdlingResource(activity, viewId, predicate)
        IdlingRegistry.getInstance().register(resource)
        try {
            onView(withId(viewId)).check(ViewAssertion { view, noViewFoundException ->
                checkNotNull(view) { noViewFoundException?.message ?: "Expected view was not found." }
                check(predicate(view)) { "View $viewId did not reach its expected state." }
            })
        } finally {
            IdlingRegistry.getInstance().unregister(resource)
            resource.dispose()
        }
    }

    private fun textOf(@IdRes viewId: Int): String {
        var text = ""
        onView(withId(viewId)).check(ViewAssertion { view, noViewFoundException ->
            checkNotNull(view) { noViewFoundException?.message ?: "Expected text view was not found." }
            text = (view as TextView).text.toString()
        })
        return text
    }

    private fun app(): App = InstrumentationRegistry.getInstrumentation()
        .targetContext.applicationContext as App
}

private fun clickChildAtPosition(position: Int, @IdRes childViewId: Int): ViewAction =
    object : ViewAction {
        override fun getConstraints(): Matcher<View> = org.hamcrest.Matchers.instanceOf(RecyclerView::class.java)

        override fun getDescription(): String = "click child $childViewId at adapter position $position"

        override fun perform(uiController: androidx.test.espresso.UiController, view: View) {
            val recyclerView = view as RecyclerView
            val holder = checkNotNull(recyclerView.findViewHolderForAdapterPosition(position)) {
                "No ViewHolder for adapter position $position"
            }
            checkNotNull(holder.itemView.findViewById<View>(childViewId)) {
                "No child view $childViewId at adapter position $position"
            }.performClick()
            uiController.loopMainThreadUntilIdle()
        }
    }

private class ViewPredicateIdlingResource(
    private val activity: Activity,
    @param:IdRes private val viewId: Int,
    private val predicate: (View) -> Boolean,
) : IdlingResource {

    @Volatile
    private var callback: IdlingResource.ResourceCallback? = null

    private val preDrawListener = ViewTreeObserver.OnPreDrawListener {
        notifyIfIdle()
        true
    }

    override fun getName(): String = "ViewPredicateIdlingResource($viewId)"

    override fun isIdleNow(): Boolean {
        return isIdle()
    }

    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback) {
        this.callback = callback
        activity.runOnUiThread {
            activity.window.decorView.viewTreeObserver.addOnPreDrawListener(preDrawListener)
            notifyIfIdle()
        }
    }

    fun dispose() {
        activity.runOnUiThread {
            activity.window.decorView.viewTreeObserver.removeOnPreDrawListener(preDrawListener)
        }
    }

    private fun notifyIfIdle() {
        if (isIdle()) callback?.onTransitionToIdle()
    }

    private fun isIdle(): Boolean = activity.findViewById<View>(viewId)?.let(predicate) == true
}
