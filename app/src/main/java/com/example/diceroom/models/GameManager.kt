package com.example.diceroom.models

import android.util.Xml
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.xmlpull.v1.XmlPullParser
import java.io.IOException
import java.io.StringReader

data class GameInfo(
    val id: String, val thumbnail: String, val name: String, val yearPublished: String
)


data class GameDetails(
    val minPlayers: Int?,
    val maxPlayers: Int?,
    val yearPublished: Int?,
    val description: String?,
    val thumbnail: String?,
    val minAge: Int?
)

class GameManager {
    private val hotListEndpoint: String = "https://boardgamegeek.com/xmlapi2/hot?type=boardgame"
    private val gameInfoEndpoint: String = "https://boardgamegeek.com/xmlapi2/thing?id="
    private val client = OkHttpClient()

    private fun enqueueCall(request: Request, callback: (String?, Exception?) -> Unit) {
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null, e)
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val responseBody = response.body()?.string()
                    callback(responseBody, null)
                } catch (e: Exception) {
                    callback(null, e)
                }
            }
        })
    }

    fun fetchGamesInfo(callback: (List<GameInfo>?, Exception?) -> Unit) {
        val request = Request.Builder().url(hotListEndpoint).build()

        enqueueCall(request) { responseBody, exception ->
            try {
                val gameInfoList = parseGameInfoResponse(responseBody)
                callback(gameInfoList, exception)
            } catch (e: Exception) {
                callback(null, e)
            }
        }
    }

    private fun parseGameInfoResponse(responseBody: String?): List<GameInfo>? {
        if (responseBody.isNullOrBlank()) {
            return null
        }

        val gameInfoList = mutableListOf<GameInfo>()

        try {
            val parser = Xml.newPullParser()
            parser.setInput(StringReader(responseBody))

            var eventType = parser.eventType
            var gameId = ""
            var thumbnail = ""
            var name = ""
            var yearPublished = ""

            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        when (parser.name) {
                            "item" -> gameId = parser.getAttributeValue(null, "id")
                            "thumbnail" -> thumbnail = parser.getAttributeValue(null, "value")
                            "name" -> name = parser.getAttributeValue(null, "value")
                            "yearpublished" -> yearPublished = parser.getAttributeValue(null, "value")
                        }
                    }

                    XmlPullParser.END_TAG -> {
                        if (parser.name == "item") {
                            val gameInfo = GameInfo(gameId, thumbnail, name, yearPublished)
                            gameInfoList.add(gameInfo)
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

        return gameInfoList
    }

    fun fetchGameDetailsById(gameId: String, callback: (GameDetails?, Exception?) -> Unit) {
        val url = "$gameInfoEndpoint$gameId"
        val request = Request.Builder().url(url).build()

        enqueueCall(request) { responseBody, exception ->
            try {
                val gameDetails = parseGameDetailsResponse(responseBody)
                callback(gameDetails, exception)
            } catch (e: Exception) {
                callback(null, e)
            }
        }
    }

    private fun parseGameDetailsResponse(responseBody: String?): GameDetails? {
        if (responseBody.isNullOrBlank()) {
            return null
        }
        try {
            val parser = Xml.newPullParser()
            parser.setInput(StringReader(responseBody))

            var eventType = parser.eventType
            var minPlayers: Int? = null
            var maxPlayers: Int? = null
            var yearPublished: Int? = null
            var description: String? = null
            var thumbnail: String? = null
            var minAge: Int? = null

            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        when (parser.name) {
                            "minplayers" -> minPlayers =
                                parser.getAttributeValue(null, "value").toInt()

                            "maxplayers" -> maxPlayers =
                                parser.getAttributeValue(null, "value").toInt()

                            "yearpublished" -> yearPublished =
                                parser.getAttributeValue(null, "value").toInt()

                            "description" -> description = parser.getAttributeValue(null, "value")
                            "thumbnail" -> thumbnail = parser.getAttributeValue(null, "value")
                            "minage" -> minAge = parser.getAttributeValue(null, "value").toInt()
                        }
                    }
                }
                eventType = parser.next()
            }
            return GameDetails(
                minPlayers, maxPlayers, yearPublished, description, thumbnail, minAge
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

}