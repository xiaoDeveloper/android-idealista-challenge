package com.xiao.idealistachallenge.core

import android.content.Context
import androidx.room.Room
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.xiao.idealistachallenge.data.local.FavoriteDatabase
import com.xiao.idealistachallenge.data.remote.IDEALISTA_BASE_URL
import com.xiao.idealistachallenge.data.remote.IdealistaApi
import com.xiao.idealistachallenge.data.remote.RemoteJson
import com.xiao.idealistachallenge.data.repository.AdRepository
import com.xiao.idealistachallenge.data.repository.FavoriteRepository
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class AppContainer(
    context: Context,
    private val idealistaApiOverride: IdealistaApi? = null,
    private val favoriteDatabaseOverride: FavoriteDatabase? = null,
) {

    private val applicationContext = context.applicationContext

    val idealistaApi: IdealistaApi by lazy {
        idealistaApiOverride ?: Retrofit.Builder()
            .baseUrl(IDEALISTA_BASE_URL)
            .addConverterFactory(
                RemoteJson.instance.asConverterFactory("application/json".toMediaType()),
            )
            .build()
            .create(IdealistaApi::class.java)
    }

    val favoriteDatabase: FavoriteDatabase by lazy {
        favoriteDatabaseOverride ?: Room.databaseBuilder(
            applicationContext,
            FavoriteDatabase::class.java,
            FAVORITES_DATABASE_NAME,
        ).build()
    }

    val favoriteRepository: FavoriteRepository by lazy {
        FavoriteRepository(favoriteDatabase.favoriteDao())
    }

    val adRepository: AdRepository by lazy {
        AdRepository(idealistaApi)
    }

    inline fun <reified T : ViewModel> viewModelFactory(
        noinline creator: (AppContainer) -> T,
    ): ViewModelProvider.Factory = AppViewModelFactory(T::class.java) { creator(this) }

    private companion object {
        const val FAVORITES_DATABASE_NAME = "favorites.db"
    }
}
