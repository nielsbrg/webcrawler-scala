package scala.WebCrawler_2
import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet
import scala.io.Source
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * The topic filter analyzes whether the
   content of parsed pages is related to topic or not. If the page is
   relevant, the URLs extracted from it will be added to the URL
   queue, otherwise added to the Irrelevant table
 */

class topicFilter {
  private val URLQueue = crawlerMain.queue
  val relevantPages = new HashMap[String, Int]
  val relevantThreshold = 100
  val highlyRelevantThreshold = 500
  
  def filter(url:(String, Int)): Boolean = {
    if (isHighlyRelevant(url._2)) {
      println("HIGHLY RELEVANT: " + url._1)
      crawlerMain.getRelevantWordsFromPage(url._1)
      relevantPages += url._1 -> url._2
      return true
    }
    else if (isRelevant(url._2)) {
      relevantPages += url._1 -> url._2
      return true
    }
    else return false
  }
  
  def addKeyWord(key: String) = {
    if (key.length() > 1) {
      URLQueue.unvisitedSearchTerms.enqueue(key)
      crawlerMain.queueIsUpdated()
    }
  }
  def isRelevant(urlRelevance: Int): Boolean = urlRelevance > relevantThreshold
  def isHighlyRelevant(urlRelevance: Int): Boolean = urlRelevance > highlyRelevantThreshold
}