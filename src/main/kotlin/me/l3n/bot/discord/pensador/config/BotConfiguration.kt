package me.l3n.bot.discord.pensador.config

import io.smallrye.config.ConfigMapping
import io.smallrye.config.WithDefault


@ConfigMapping(prefix = "bot")
interface BotConfiguration {

    fun noImageUrl(): String

    @WithDefault("2000")
    fun charLimit(): Int
}