package scala.WebCrawler_2

import scala.io.Source
import scala.collection.mutable.HashSet
import scala.collection.mutable.Queue

/**
 * The URL queue contains a list of unvisited URLs maintained
 * by the crawler and is initialized with seed URLs.
 */

class URLQueue {
  val unvisitedSearchTerms: Queue[String] = Queue()
  for(line <- Source.fromInputStream(getClass().getClassLoader().getResourceAsStream("seedurls.txt")).getLines()) {
    unvisitedSearchTerms.enqueue(line)
  }
  
  def print() {
    unvisitedSearchTerms.foreach { x => println("queue item: " + x) }
  }
}