package com.xiao.idealistachallenge

import android.view.View
import android.view.ViewTreeObserver
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingPolicies
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeLeft
import androidx.test.espresso.action.ViewActions.pressBack
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
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
class PropertyBrowsingJourneyTest {

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
    fun deterministicListingFixturesRenderEachSupportedMediaShape() {
        awaitListingRows(5)

        listOf(
            MediaShape(position = 0, pageCount = 3, showsPosition = true),
            MediaShape(position = 1, pageCount = 1, showsPosition = false),
            MediaShape(position = 2, pageCount = 1, showsPosition = false),
            MediaShape(position = 3, pageCount = 1, showsPosition = false),
            MediaShape(position = 4, pageCount = 3, showsPosition = true),
        ).forEach { expected ->
            onView(withId(R.id.listingRecyclerView)).perform(scrollToListingPosition(expected.position))
            awaitListingRow(expected.position)
            onView(withId(R.id.listingRecyclerView)).perform(
                assertChildMediaShape(
                    position = expected.position,
                    expectedPageCount = expected.pageCount,
                    expectedPositionVisible = expected.showsPosition,
                ),
            )
        }
    }

    @Test
    fun listingPagerSwipeKeepsNavigationAndFavoriteIndependentThenMediaTapOpensDetail() {
        awaitListingRows(5)
        awaitListingRow(0)

        onView(withId(R.id.listingRecyclerView)).perform(
            performChildActionAtPosition(0, R.id.listingImagePager, swipeLeft()),
        )
        onView(withId(R.id.listingRecyclerView)).perform(
            assertChildTextAtPosition(0, R.id.listingImagePosition, "2 / 3"),
            assertChildVisibilityAtPosition(0, R.id.favoriteDate, false),
        )
        onView(withId(R.id.detailScreen)).check(doesNotExist())

        onView(withId(R.id.listingRecyclerView)).perform(
            performChildActionAtPosition(0, R.id.favoriteButton, click()),
        )
        awaitListingFavoriteDate(0)

        onView(withId(R.id.listingRecyclerView)).perform(
            performChildActionAtPosition(0, R.id.listingImagePager, click()),
        )
        awaitView(R.id.detailSavedState) { it.isShown }
        onView(withContentDescription(startsWith("Quitar vivienda de guardados"))).check(matches(isDisplayed()))
        onView(withId(R.id.detailPrice)).check(matches(isDisplayed()))
    }

    @Test
    fun fixedDetailKeepsAllPhotoPositionsAndPresentsOnlyApprovedHierarchy() {
        awaitListingRows(5)
        awaitListingRow(0)
        onView(withId(R.id.listingRecyclerView)).perform(
            performChildActionAtPosition(0, R.id.listingCard, click()),
        )
        awaitView(R.id.detailImagePager) { view ->
            view is RecyclerView && view.isShown && view.adapter?.itemCount == 4
        }

        awaitMediaAnnouncement("Salón, foto 1 de 4")
        onView(withId(R.id.detailImagePosition)).check(matches(isDisplayed()))
        onView(withId(R.id.detailImagePager)).perform(swipeLeft())
        awaitMediaAnnouncement("Foto 2 de 4")
        onView(withId(R.id.detailImagePosition)).check(matches(withText("2 / 4")))
        onView(withContentDescription("Guardar vivienda")).check(matches(isDisplayed()))
        onView(withId(R.id.detailPropertyTypeOperation)).check(matches(withText("Piso · Venta")))
        onView(withId(R.id.detailPrimaryFacts)).check(matches(isDisplayed()))
        onView(withText("133 m²")).check(matches(isDisplayed()))
        onView(withText("3 hab.")).check(matches(isDisplayed()))
        onView(withText("2 baños")).check(matches(isDisplayed()))
        onView(withId(R.id.detailSecondaryFacts)).check(matches(isDisplayed()))
        onView(withText("Planta 2")).check(matches(isDisplayed()))
        onView(withText("Interior")).check(matches(isDisplayed()))
        onView(withText("Con ascensor")).check(matches(isDisplayed()))
        onView(withId(R.id.detailEnergyConsumptionLabel)).check(matches(withText("Consumo")))
        onView(withId(R.id.detailEnergyConsumptionValue)).check(matches(withText("E")))
        onView(withId(R.id.detailEnergyEmissionsLabel)).check(matches(withText("Emisiones")))
        onView(withId(R.id.detailEnergyEmissionsValue)).check(matches(withText("E")))

        onView(withContentDescription("Guardar vivienda")).perform(click())
        awaitView(R.id.detailSavedState) { it.isShown }
        onView(withId(R.id.detailImagePosition)).check(matches(withText("2 / 4")))
        onView(withId(R.id.detailImagePager)).perform(pressBack())
        awaitListingRows(5)
    }

    @Test
    fun fixedDetailExpandsAndCollapsesTheCompleteMultiParagraphDescription() {
        awaitListingRows(5)
        onView(withId(R.id.listingRecyclerView)).perform(
            performChildActionAtPosition(0, R.id.listingCard, click()),
        )
        awaitView(R.id.detailDescriptionToggle) { it.isShown }

        onView(withId(R.id.detailDescriptionToggle)).perform(scrollTo(), click())
        onView(withId(R.id.detailDescription)).check { view, noViewFoundException ->
            checkNotNull(view) { noViewFoundException?.message ?: "Description view was not found." }
            check((view as TextView).maxLines == Int.MAX_VALUE) { "Description was not expanded." }
            check(view.text.toString().contains("\n\n")) { "Paragraph break was lost." }
        }
        onView(withId(R.id.detailDescriptionToggle)).check(matches(withText("Ver menos")))

        onView(withId(R.id.detailDescriptionToggle)).perform(click())
        onView(withId(R.id.detailDescription)).check { view, noViewFoundException ->
            checkNotNull(view) { noViewFoundException?.message ?: "Description view was not found." }
            check((view as TextView).maxLines == 6) { "Description was not collapsed." }
            check(view.text.toString().contains("\n\n")) { "Paragraph break was lost after collapse." }
        }
        onView(withId(R.id.detailDescriptionToggle)).check(matches(withText("Ver más")))
    }

    private fun awaitMediaAnnouncement(expected: String) = awaitView(R.id.propertyImage) { view ->
        view.isShown && view.contentDescription?.toString() == expected
    }

    private fun awaitListingRows(expectedCount: Int) = awaitView(R.id.listingRecyclerView) { view ->
        view is RecyclerView && view.isShown && view.adapter?.itemCount == expectedCount
    }

    private fun awaitListingRow(position: Int) = awaitView(R.id.listingRecyclerView) { view ->
        (view as? RecyclerView)?.findViewHolderForAdapterPosition(position) != null
    }

    private fun awaitListingFavoriteDate(position: Int) = awaitView(R.id.listingRecyclerView) { view ->
        val recyclerView = view as? RecyclerView ?: return@awaitView false
        val holder = recyclerView.findViewHolderForAdapterPosition(position) ?: return@awaitView false
        holder.itemView.findViewById<View>(R.id.favoriteDate)?.isShown == true
    }

    private fun awaitView(viewId: Int, predicate: (View) -> Boolean) {
        lateinit var activity: MainActivity
        activityRule.scenario.onActivity { activity = it }
        val resource = PropertyBrowsingViewPredicateIdlingResource(activity, viewId, predicate)
        IdlingRegistry.getInstance().register(resource)
        try {
            onView(withId(viewId)).check { view, noViewFoundException ->
                checkNotNull(view) { noViewFoundException?.message ?: "Expected view was not found." }
                check(predicate(view)) { "View $viewId did not reach its expected state." }
            }
        } finally {
            IdlingRegistry.getInstance().unregister(resource)
            resource.dispose()
        }
    }

    private fun app(): App = InstrumentationRegistry.getInstrumentation()
        .targetContext.applicationContext as App
}

private data class MediaShape(
    val position: Int,
    val pageCount: Int,
    val showsPosition: Boolean,
)

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

private fun scrollToListingPosition(position: Int): androidx.test.espresso.ViewAction =
    object : androidx.test.espresso.ViewAction {
        override fun getConstraints(): Matcher<View> = org.hamcrest.Matchers.instanceOf(RecyclerView::class.java)

        override fun getDescription(): String = "scroll listing to adapter position $position"

        override fun perform(uiController: androidx.test.espresso.UiController, view: View) {
            (view as RecyclerView).scrollToPosition(position)
            uiController.loopMainThreadUntilIdle()
        }
    }

private fun assertChildMediaShape(
    position: Int,
    expectedPageCount: Int,
    expectedPositionVisible: Boolean,
): androidx.test.espresso.ViewAction = object : androidx.test.espresso.ViewAction {
    override fun getConstraints(): Matcher<View> = org.hamcrest.Matchers.instanceOf(RecyclerView::class.java)

    override fun getDescription(): String =
        "assert listing $position has $expectedPageCount media pages and position visibility $expectedPositionVisible"

    override fun perform(uiController: androidx.test.espresso.UiController, view: View) {
        val itemView = childAtPosition(view as RecyclerView, position, R.id.listingCard)
        val pager = itemView.findViewById<RecyclerView>(R.id.listingImagePager)
        check(pager.adapter?.itemCount == expectedPageCount) {
            "Expected $expectedPageCount media pages at $position but found ${pager.adapter?.itemCount}."
        }
        val positionView = itemView.findViewById<View>(R.id.listingImagePosition)
        check(positionView.visibility == if (expectedPositionVisible) View.VISIBLE else View.GONE) {
            "Unexpected position indicator visibility at listing $position."
        }
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

private class PropertyBrowsingViewPredicateIdlingResource(
    private val activity: MainActivity,
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

    override fun isIdleNow(): Boolean = isIdle()

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
