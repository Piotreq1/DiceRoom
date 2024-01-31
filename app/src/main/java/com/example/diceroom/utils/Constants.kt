package com.example.diceroom.utils

class Constants {
    companion object {
        const val MEETING_ID: String = "meetingId"
        const val BASE_BOARD_GAME_URL = "https://boardgamegeek.com/boardgame/"
        const val HOT_LIST_API_ENDPOINT: String =
            "https://boardgamegeek.com/xmlapi2/hot?type=boardgame"
        const val GAME_INFO_API_ENDPOINT: String = "https://boardgamegeek.com/xmlapi2/thing?id="

        const val GAMES_PREFS: String = "gameListPrefs"
        const val FAVOURITES_KEY: String = "isFavourites"

        const val MEET_PREFS: String = "meetsPrefs"
        const val USER_MEETINGS_KEY: String = "userMeetings"

        const val IS_FIRST_RUN_PREFS: String = "isFirstRun"

        const val GAME_ID: String = "gameId"
        const val IS_GAME_FAVOURITE_KEY: String = "isFavourite"
        const val CURRENT_ITEM_KEY: String = "currentItem"

    }
}