package me.l3n.bot.discord.pensador.service.handler.commands

import dev.kord.core.behavior.reply
import dev.kord.core.entity.Message
import dev.kord.x.emoji.Emojis
import me.l3n.bot.discord.pensador.config.BotConfiguration
import me.l3n.bot.discord.pensador.service.crawler.CrawlerService
import me.l3n.bot.discord.pensador.service.crawler.Quote
import me.l3n.bot.discord.pensador.service.handler.CommandHandler
import me.l3n.bot.discord.pensador.service.isValid
import me.l3n.bot.discord.pensador.util.retry
import me.l3n.bot.discord.pensador.util.success
import org.jboss.logging.Logger
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.inject.Instance
import javax.inject.Inject


@ApplicationScoped
class GetQuoteCommandHandler(
    crawlerInstance: Instance<CrawlerService>,
    private val config: BotConfiguration,
) : CommandHandler() {

    private val crawler: CrawlerService =
        crawlerInstance.get() ?: throw IllegalArgumentException("Invalid source specified in config")

    override val name = "q"

    @Inject
    private lateinit var log: Logger

    override suspend fun handle(args: List<String>, message: Message): Result<Unit> {
        val author = message.author ?: return Result.failure(IllegalArgumentException("No author!"))
        val searchingMessage = message.reply { content = "Searching... ${Emojis.mag}" }

        log.debug("Crawling quote for '${author.username}'")

        val quote = crawlQuote().getOrNull()

        searchingMessage.delete()

        replyQuote(message, quote)

        return Result.success()
    }

    private suspend fun crawlQuote() = retry(
        5,
        block = {
            log.debug("Crawling a quote")

            val result = crawler.crawlRandomQuote(config.charLimit())
            log.info("Crawled a random quote")

            if (result.isValid()) Result.success(result)
            else {
                Result.failure(IllegalArgumentException("Quote not valid"))
            }
        },
        beforeRetry = { i ->
            log.debug("Retrying crawling a valid quote (#$i)")
        },
        afterRetry = { error -> log.debug(error.message) },
        retryExceeded = { times ->
            log.warn("Retry for crawling a valid quote exceeded ($times)")
        },
    )

    private suspend fun replyQuote(message: Message, quote: Quote?) = message.reply {
        if (quote == null)
            content = "Couldn't find a valid quote! ${Emojis.weary}"
        else {
            val quoteAuthor = quote.author

            this.embed {
                description = quote.text

                thumbnail {
                    this.url = quoteAuthor.imageUrl ?: ""
                }

                footer {
                    this.text = quote.author.name
                }
            }
        }
    }
}