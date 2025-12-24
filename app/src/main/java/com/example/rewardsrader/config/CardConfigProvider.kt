package com.example.rewardsrader.config

interface CardConfigProvider {
    suspend fun load(): CardConfigResult
}

class DefaultCardConfigProvider(
    private val loader: CardConfigLoader
) : CardConfigProvider {
    override suspend fun load(): CardConfigResult = loader.loadFromAssets()
}
