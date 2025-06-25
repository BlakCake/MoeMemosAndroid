package me.mudkip.moememos.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.mudkip.moememos.ext.settingsDataStore
import me.mudkip.moememos.data.model.UserSettings
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    val blurNsfw = context.settingsDataStore.data.map { settings ->
        val user = settings.usersList.firstOrNull { it.accountKey == settings.currentUser }
        val userSettings = user?.settings ?: UserSettings.getDefaultInstance()
        if (userSettings.hasBlurNsfw()) userSettings.blurNsfw else true
    }.stateIn(viewModelScope, SharingStarted.Lazily, true)

    fun setBlurNsfw(enabled: Boolean) = viewModelScope.launch {
        context.settingsDataStore.updateData { settings ->
            val currentUser = settings.usersList.firstOrNull { it.accountKey == settings.currentUser }
                ?: return@updateData settings
            val index = settings.usersList.indexOf(currentUser)
            val updatedUser = currentUser.toBuilder().apply {
                this.settings = this.settings.toBuilder().setBlurNsfw(enabled).build()
            }.build()
            settings.toBuilder().setUsers(index, updatedUser).build()
        }
    }
}

val LocalSettings = androidx.compose.runtime.compositionLocalOf<SettingsViewModel> {
    error("Settings view model not found")
}
