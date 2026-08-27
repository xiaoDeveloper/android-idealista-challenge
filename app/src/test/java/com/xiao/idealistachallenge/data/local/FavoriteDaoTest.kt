package com.xiao.idealistachallenge.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FavoriteDaoTest {

    private lateinit var database: FavoriteDatabase
    private lateinit var dao: FavoriteDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            FavoriteDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = database.favoriteDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `insert is observed with its durable identity and timestamp`() = runBlocking {
        dao.upsert(FavoriteEntity(adId = "listing-42", favoritedAtEpochMillis = 1_000L))

        assertEquals(
            FavoriteEntity(adId = "listing-42", favoritedAtEpochMillis = 1_000L),
            dao.observeFavorite("listing-42").first(),
        )
    }

    @Test
    fun `delete removes the observed favorite`() = runBlocking {
        dao.upsert(FavoriteEntity(adId = "listing-42", favoritedAtEpochMillis = 1_000L))

        dao.deleteByAdId("listing-42")

        assertNull(dao.observeFavorite("listing-42").first())
    }

    @Test
    fun `upsert replaces the timestamp for the same property code`() = runBlocking {
        dao.upsert(FavoriteEntity(adId = "listing-42", favoritedAtEpochMillis = 1_000L))

        dao.upsert(FavoriteEntity(adId = "listing-42", favoritedAtEpochMillis = 2_000L))

        assertEquals(
            FavoriteEntity(adId = "listing-42", favoritedAtEpochMillis = 2_000L),
            dao.observeFavorite("listing-42").first(),
        )
    }
}
