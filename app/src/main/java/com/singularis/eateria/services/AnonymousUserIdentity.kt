package com.singularis.eateria.services

import android.content.Context
import java.util.regex.Pattern

/**
 * Detects guest / "Let Me Try" accounts so they are never shown as real identities
 * or offered as friends (they have no nickname).
 */
object AnonymousUserIdentity {
    private val emailPattern: Pattern =
        Pattern.compile("(?i)^anon_[0-9a-f\\-]+@anonymous\\.local$")

    private val anonymousNamePattern: Pattern =
        Pattern.compile("(?i)^(guest|anonymous([\\s._-].*)?|anon([\\s._-].*)?)$")

    fun isAnonymousEmail(email: String?): Boolean {
        val trimmed = email?.trim().orEmpty()
        if (trimmed.isEmpty()) return false
        if (emailPattern.matcher(trimmed).matches()) return true
        return trimmed.lowercase().endsWith("@anonymous.local")
    }

    fun isAnonymousDisplayName(name: String?): Boolean {
        val trimmed = name?.trim().orEmpty()
        if (trimmed.isEmpty()) return false
        return anonymousNamePattern.matcher(trimmed).matches()
    }

    fun isAnonymous(
        email: String?,
        nickname: String? = null,
        name: String? = null,
    ): Boolean {
        if (isAnonymousEmail(email)) return true
        if (isAnonymousDisplayName(name)) return true
        if (isAnonymousDisplayName(nickname)) return true
        return false
    }

    /** Apple Hide My Email — not useful in friend lists unless a nickname is set. */
    fun isPrivateRelayEmail(email: String?): Boolean {
        val lower = email?.lowercase() ?: return false
        return lower.contains("@privaterelay.appleid.com")
    }

    fun hasUsableNickname(nickname: String?): Boolean {
        val trimmed = nickname?.trim().orEmpty()
        if (trimmed.isEmpty()) return false
        return !isAnonymousDisplayName(trimmed)
    }

    fun trialUsageLabel(context: Context): String =
        Localization.tr(context, "profile.trial_usage", "Trial usage")

    fun defaultDisplayName(context: Context): String =
        Localization.tr(context, "profile.default_display_name", "Health Eater")

    fun isUsablePersonName(name: String?): Boolean {
        val trimmed = name?.trim().orEmpty()
        if (trimmed.isEmpty()) return false
        if (isAnonymousDisplayName(trimmed)) return false
        if (isAnonymousEmail(trimmed)) return false
        if (isPrivateRelayEmail(trimmed)) return false
        if (trimmed.contains("@")) return false
        if (trimmed.lowercase().startsWith("anon_")) return false
        return true
    }

    /** Name shown in profile/menu: nickname → real name → "Health Eater". */
    fun menuDisplayName(
        context: Context,
        nickname: String?,
        userName: String?,
    ): String {
        if (hasUsableNickname(nickname)) {
            return nickname!!.trim()
        }
        if (isUsablePersonName(userName)) {
            return userName!!.trim()
        }
        return defaultDisplayName(context)
    }

    /** Secondary line under the name — hide private relay / anonymous emails. */
    fun menuEmailSubtitle(email: String?): String? {
        val trimmed = email?.trim().orEmpty()
        if (trimmed.isEmpty()) return null
        if (isAnonymousEmail(trimmed) || isPrivateRelayEmail(trimmed)) return null
        return trimmed
    }

    /**
     * Whether a person should appear in Add Friend / friend pickers.
     * Nickname required — drops anonymous guests and Apple private-relay users
     * who never set a nickname (shared backend with iOS).
     */
    fun isAddFriendVisible(
        email: String?,
        nickname: String?,
    ): Boolean {
        if (!hasUsableNickname(nickname)) return false
        return !isAnonymous(email = email, nickname = nickname)
    }

    /** Safe list label: nickname only; never raw anon / private-relay emails. */
    fun friendDisplayName(
        email: String?,
        nickname: String?,
        fallback: String = "",
    ): String {
        if (hasUsableNickname(nickname)) return nickname!!.trim()
        return menuEmailSubtitle(email) ?: fallback
    }

    fun <T> addFriendVisible(
        users: List<T>,
        email: (T) -> String?,
        nickname: (T) -> String?,
    ): List<T> = users.filter { isAddFriendVisible(email(it), nickname(it)) }

    fun addFriendVisible(users: List<FriendsSearchWebSocket.UserSearchResult>): List<FriendsSearchWebSocket.UserSearchResult> =
        addFriendVisible(users, email = { it.email }, nickname = { it.nickname })

    fun addFriendVisiblePairs(friends: List<Pair<String, String>>): List<Pair<String, String>> =
        addFriendVisible(friends, email = { it.first }, nickname = { it.second })

    fun excludingAnonymous(friends: List<Pair<String, String>>): List<Pair<String, String>> =
        friends.filter { !isAnonymous(email = it.first, nickname = it.second) }
}
