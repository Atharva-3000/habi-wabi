package com.habitflow.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "habitflow_prefs")

class PreferencesManager(private val context: Context) {

    companion object {
        val ONBOARDING_COMPLETE        = booleanPreferencesKey("onboarding_complete")
        val USER_NAME                  = stringPreferencesKey("user_name")
        val LAST_QUOTE_DATE            = stringPreferencesKey("last_quote_date")
        val LAST_QUOTE_INDEX           = stringPreferencesKey("last_quote_index")
        val GLOBAL_NOTIFICATIONS       = booleanPreferencesKey("global_notifications")
        val WATER_DAILY_GOAL_ML        = stringPreferencesKey("water_daily_goal_ml")
        val QUIET_HOURS_START          = stringPreferencesKey("quiet_hours_start") // "HH:mm"
        val QUIET_HOURS_END            = stringPreferencesKey("quiet_hours_end")
        val REMINDER_OFFSET_MINUTES    = intPreferencesKey("reminder_offset_minutes")
        // Notification tracking
        val WELCOME_NOTIF_SENT         = booleanPreferencesKey("welcome_notif_sent")
        val UNREAD_NOTIF_COUNT         = intPreferencesKey("unread_notif_count")
    }

    val onboardingComplete: Flow<Boolean> = context.dataStore.data
        .map { it[ONBOARDING_COMPLETE] ?: false }

    val userName: Flow<String> = context.dataStore.data
        .map { it[USER_NAME] ?: "" }

    val globalNotificationsEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[GLOBAL_NOTIFICATIONS] ?: true }

    val waterDailyGoalMl: Flow<Int> = context.dataStore.data
        .map { it[WATER_DAILY_GOAL_ML]?.toIntOrNull() ?: 2500 }

    val quietHoursStart: Flow<String> = context.dataStore.data
        .map { it[QUIET_HOURS_START] ?: "22:00" }

    val quietHoursEnd: Flow<String> = context.dataStore.data
        .map { it[QUIET_HOURS_END] ?: "07:00" }

    val reminderOffsetMinutes: Flow<Int> = context.dataStore.data
        .map { it[REMINDER_OFFSET_MINUTES] ?: 0 }

    val welcomeNotifSent: Flow<Boolean> = context.dataStore.data
        .map { it[WELCOME_NOTIF_SENT] ?: false }

    val unreadNotifCount: Flow<Int> = context.dataStore.data
        .map { it[UNREAD_NOTIF_COUNT] ?: 0 }

    suspend fun setOnboardingComplete(complete: Boolean) {
        context.dataStore.edit { it[ONBOARDING_COMPLETE] = complete }
    }

    suspend fun setUserName(name: String) {
        context.dataStore.edit { it[USER_NAME] = name }
    }

    suspend fun setGlobalNotifications(enabled: Boolean) {
        context.dataStore.edit { it[GLOBAL_NOTIFICATIONS] = enabled }
    }

    suspend fun setWaterDailyGoalMl(ml: Int) {
        context.dataStore.edit { it[WATER_DAILY_GOAL_ML] = ml.toString() }
    }

    suspend fun setQuietHours(start: String, end: String) {
        context.dataStore.edit {
            it[QUIET_HOURS_START] = start
            it[QUIET_HOURS_END] = end
        }
    }

    suspend fun setReminderOffsetMinutes(minutes: Int) {
        context.dataStore.edit { it[REMINDER_OFFSET_MINUTES] = minutes }
    }

    suspend fun setLastQuoteInfo(date: String, index: Int) {
        context.dataStore.edit {
            it[LAST_QUOTE_DATE] = date
            it[LAST_QUOTE_INDEX] = index.toString()
        }
    }

    suspend fun setWelcomeNotifSent(sent: Boolean) {
        context.dataStore.edit { it[WELCOME_NOTIF_SENT] = sent }
    }

    suspend fun setUnreadNotifCount(count: Int) {
        context.dataStore.edit { it[UNREAD_NOTIF_COUNT] = count }
    }

    suspend fun incrementUnreadNotifCount() {
        val current = context.dataStore.data.map { it[UNREAD_NOTIF_COUNT] ?: 0 }
        context.dataStore.edit { prefs ->
            prefs[UNREAD_NOTIF_COUNT] = (prefs[UNREAD_NOTIF_COUNT] ?: 0) + 1
        }
    }

    val lastQuoteDate: Flow<String> = context.dataStore.data
        .map { it[LAST_QUOTE_DATE] ?: "" }

    val lastQuoteIndex: Flow<Int> = context.dataStore.data
        .map { it[LAST_QUOTE_INDEX]?.toIntOrNull() ?: 0 }
}
