package com.example.rewardsrader.config

import android.content.Context
import java.io.IOException

class CardConfigLoader(
    private val context: Context,
    private val parser: CardConfigParser = CardConfigParser()
) {
    fun loadFromAssets(assetPath: String = "card_config.json"): CardConfigResult {
        val jsonString = try {
            context.assets.open(assetPath).bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            return CardConfigResult.Failure(listOf("Failed to read asset $assetPath: ${e.message.orEmpty()}"))
        }
        return parser.parse(jsonString)
    }
}
