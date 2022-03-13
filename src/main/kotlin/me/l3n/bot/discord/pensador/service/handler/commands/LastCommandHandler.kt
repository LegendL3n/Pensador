package me.l3n.bot.discord.pensador.service.handler.commands

import me.l3n.bot.discord.pensador.repository.QuoteRepository
import me.l3n.bot.discord.pensador.service.handler.CommandContext
import me.l3n.bot.discord.pensador.service.handler.CommandHandler
import me.l3n.bot.discord.pensador.service.replyQuote
import me.l3n.bot.discord.pensador.util.success
import org.jboss.logging.Logger
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

@ApplicationScoped
class LastCommandHandler(
    private val quoteRepository: QuoteRepository
) : CommandHandler() {

    override val name = "last"

    @Inject
    private lateinit var log: Logger

    override suspend fun handle(args: List<String>, context: CommandContext): Result<Unit> {
        log.debug("Getting history for '${context.user.username}'")

        val lastQuote = quoteRepository.getLast()
            ?: return Result.failure(IllegalStateException("Last command requested with no quotes sent yet!"))

        context.message.replyQuote(lastQuote)

        return Result.success()
    }
}
