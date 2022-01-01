package me.l3n.bot.discord.pensador.service.crawler

import io.quarkus.arc.DefaultBean
import io.quarkus.arc.lookup.LookupIfProperty
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import javax.inject.Singleton


private val EXTRACT_QUOTE_REGEX = "(?<=“)(.*?)(?=”)".toRegex()
private val AUTHOR_NAME_REGEX = "^[A-Za-záàâãéèêíïóôõöúçñÁÀÂÃÉÈÍÏÓÔÕÖÚÇÑ' ]+\$".toRegex()

@LookupIfProperty(name = "source", stringValue = "goodreads", lookupIfMissing = true)
@DefaultBean
@Singleton
class GoodReadsCrawlerService : CrawlerService() {

    @ConfigProperty(name = "url.goodreads-quotes")
    private lateinit var quotesUrl: String

    override fun getMaxPageCount(): Int = 20

    override infix fun getPageUrl(page: Int): String = "$quotesUrl?page=$page"

    override infix fun extractQuotesHtml(rootHtml: Document): Elements =
        rootHtml.getElementsByClass("quoteDetails")

    override infix fun getQuoteContent(quoteHtml: Element): String =
        extractQuote(quoteHtml.getElementsByClass("quoteText").text())

    override infix fun getAuthorHtml(quoteHtml: Element): Element =
        quoteHtml

    override infix fun getAuthorName(authorHtml: Element): String =
        extractNameOnly(
            authorHtml
                .getElementsByClass("authorOrTitle").first()?.text()
                ?: throw IllegalAccessError("No author name")
        )

    override infix fun getAuthorImageUrl(authorHtml: Element): String? =
        authorHtml.getElementsByTag("img")
            .attr("src")

    private infix fun extractQuote(text: String) = EXTRACT_QUOTE_REGEX.find(text)?.value ?: ""

    /**
     * @return [text] with only the name (no commas for instance)
     */
    private fun extractNameOnly(text: String) = AUTHOR_NAME_REGEX.find(text)?.value ?: ""
}
