package scala.WebCrawler_2
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import scala.collection.mutable.HashSet
import scala.collection.mutable.HashMap
/**
 * The parser and extractor
 * extracts information such as the terms and the hyperlink URLs
 * from a downloaded page.
 */

class pageParser {
  private val relevanceCalculator = new relevanceCalculator()
  
  def parsePage(baseURL: String, url:String, page: Document): HashSet[String] = {
      val links = new HashSet[String]
      try { 
       val elements = page.select("a")
       for(i <- 0 until elements.size()) {
         links += elements.get(i).attr("href")
       }
       links.map { line => parseRelativeLinks(baseURL, line) }
      }
      catch {
        case ex:java.nio.charset.MalformedInputException => println(ex)
      }
      return links
  }
  
  def parseContent(url: String, page: Document): (String, Int) = {
    return relevanceCalculator.calculateURLRelevance(url, page)
  }
  
  def parseMostCommonWord(page: Document): String = {
    val allElements = page.body.text().split(" ")
    return relevanceCalculator.extractMostCommonWord(allElements)
  }
  
  private def parseRelativeLinks(baseURL: String, isolatedLink: String): String = {
    if (isolatedLink.startsWith("http")) {
      return isolatedLink
    }
    else {
      if (baseURL.startsWith("https://")) {
        return "https://" + isolatedLink
      }
      else {
        return "http://" + isolatedLink
      }
    }
    if (isolatedLink.length() < 1) {
      if (isolatedLink.charAt(0).==('#')) {
        return baseURL + "/#" + isolatedLink
      }
      return baseURL
    }
    if (isolatedLink.charAt(0) == '/' && isolatedLink.charAt(1) == '/') {
      return isolatedLink.substring(2)
    }
    else if (isolatedLink.charAt(0) == '/' && isolatedLink.charAt(1) != '/') {
      if (isolatedLink.endsWith("/")) { 
        return baseURL.substring(0, baseURL.length() - 1) + isolatedLink
      }
      else {
        return baseURL + isolatedLink
      }
    }
    else isolatedLink
  }
  private def isolateLinkInLine(baseURL: String, link:String): String = {
    /*println(link)
    val indexOfLink = link.indexOf("href=")
    println(indexOfLink)
    if (indexOfLink < link.length() && indexOfLink >= 0) {
      val startAtHref = link.substring(link.indexOf("href=")).split("\"")
      if (startAtHref.size > 1) {
        if (startAtHref(1).length() > 1) {
          return startAtHref(1)
        }
        else return baseURL
      } else return baseURL
    }
    else return baseURL*/
    link
  }
}