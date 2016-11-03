package scala.WebCrawler_2

import scala.collection.mutable.HashSet
import scala.collection.mutable.Queue
import scala.collection.mutable.HashMap

class pageIndexer {
  private val topicFilter = new topicFilter()
  val interestingPages = new HashSet[String]
  val finalResults = new HashMap[String, Int]
  
  def index(relevance: (String, Int)): Boolean = {
    if (topicFilter.filter(relevance) == true) {
      interestingPages += relevance._1
      println("Found relevant page: " + relevance._1)
      true
    }
    else false
  }
  
  def addKeyWord(key: String) = {
    topicFilter.addKeyWord(key)
  }
  
  def addToFinalResults(link: String): Unit = {
    if (topicFilter.relevantPages.contains(link)) {
      finalResults += link -> topicFilter.relevantPages(link)
    }
  }
}