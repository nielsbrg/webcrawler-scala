package scala.WebCrawler_2

import scala.io.Source
import akka.actor._
import akka.pattern._
import akka.util.Timeout
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.Await
import scala.util.{Success, Failure}
import scala.collection.mutable._
import scala.collection.mutable.ListBuffer
import java.net.URL
import java.io._
import scala.util.Random

/**
 *  The web page downloader fetches URLs from URL queue and downloads
 *  corresponding pages from the internet.   
 */

object crawlerMain {
  val queue = new URLQueue()
  val system = ActorSystem("ActorSystem")
  private val pageIndexer = new pageIndexer()
  private val pageLoader = new pageLoader()
  val alreadyVisitedPages = new HashSet[String]
  val maxRecursionLevel = 100
  
  case object Crawl
  case class SearchPage(baseURL: String, page: String)
  case class ParsePage(baseURL: String, url: String)
  case object AddRecursionLevel
  
  val actor = system.actorOf(Props[crawlerActor], "seedActor")
  val counterActor = system.actorOf(Props[counterActor], "countRecursion")
  
  def main(args: Array[String]): Unit = {
    actor ! Crawl
    
  }
  
  class crawlerActor extends Actor {
    private var children = 0
    def receive() = {
      case Crawl => 
         while(queue.unvisitedSearchTerms.isEmpty == false) {
           val searchTerm = queue.unvisitedSearchTerms.dequeue()
           println("Searching google for \"" + searchTerm + "\"")
           val data = pageLoader.getDataFromGoogle(searchTerm)
           if (data.size > 0) {
             data.foreach { 
               url => 
                 println(url)
                 if (!alreadyVisitedPages.contains(url)) {
                   alreadyVisitedPages += url
                   val newActor = context.actorOf(Props[crawlerActor], "child" + children)
                   children += 1
                   newActor ! ParsePage(new URL(url).getProtocol + "://" + new URL(url).getHost, url)
                 }
             }
           }
         }
         if (context.children.size < 1 && queue.unvisitedSearchTerms.isEmpty) {
           system.terminate
           println("system terminated because of bad initial response from google")
         }
      case ParsePage(baseURL, url) => val f = Future { pageIndexer.index(pageLoader.parseContent(baseURL, url)) }
        f.onComplete { 
          case Success(value) => 
            println("Going to " + url)
            counterActor ! AddRecursionLevel           
              pageIndexer.interestingPages.foreach { link => 
                pageIndexer.addToFinalResults(link)
                val parsedGoogleRedirectedURL = pageLoader.getDocumentLocation(link)
                val baseURL = new URL(parsedGoogleRedirectedURL).getProtocol + "://" + new URL(parsedGoogleRedirectedURL).getHost
                val pages = pageLoader.getLinks(baseURL, link)
                pages.foreach { link => 
                  if (!alreadyVisitedPages.contains(link)) {
                    alreadyVisitedPages += link
                    children += 1
                    if (link.length() > 1) {
                      if (link.charAt(0) == '/') {
                        val relativeLink = baseURL + link
                        if (context != null)
                        context.actorOf(Props[crawlerActor], "child" + children) ! ParsePage(baseURL, relativeLink)
                      }
                      else if(link.startsWith("http")) {
                        if (context != null) { 
                          val newActor = context.actorOf(Props[crawlerActor], "child" + children)
                          newActor ! ParsePage(baseURL, link)
                        }
                      }
                    }
                  }
                }
              } 
          case Failure(ex) =>  println(ex)
        }
    }
  }
  
  class counterActor extends Actor {
    private var recursionLevel = 0
    def receive() = {
      case AddRecursionLevel => 
        if (recursionLevel >= maxRecursionLevel && queue.unvisitedSearchTerms.isEmpty) { 
          println("RECURSION HAS REACHED : " + recursionLevel + " LEVELS DEEP. SHUTTING DOWN.");
          system.terminate
          saveResultsToFile()
        } 
        else 
          recursionLevel += 1; 
    }
  }
  
  def getRelevantWordsFromPage(url: String) = {
    val mostCommonWordOnThisPage = pageLoader.getMostCommonWord(url)
    pageIndexer.addKeyWord(mostCommonWordOnThisPage)
  }
  
  def queueIsUpdated() = {
    actor ! Crawl
  }
  
  def saveResultsToFile() {
    val dir = Source.fromInputStream(getClass().getClassLoader().getResourceAsStream("outputPath.txt")).mkString
    val file = new File(dir)
    var output = ""
    output += "--------------------------------------------------------------------------------------------------------------------" + System.getProperty("line.separator")
    output += "RESULTS OF CRAWL FOR SEARCHES WITH RECURSION LEVEL OF " + maxRecursionLevel +  System.getProperty("line.separator")
    output += "--------------------------------------------------------------------------------------------------------------------" + System.getProperty("line.separator")
    for(line <- Source.fromInputStream(getClass().getClassLoader().getResourceAsStream("seedurls.txt")).getLines()) {
      output += line + System.getProperty("line.separator")
    }
    output += "--------------------------------------------------------------------------------------------------------------------" + System.getProperty("line.separator")
    output += "KEYWORDS" + System.getProperty("line.separator")
    output += "--------------------------------------------------------------------------------------------------------------------" + System.getProperty("line.separator")
    for(line <- Source.fromInputStream(getClass().getClassLoader().getResourceAsStream("keywords.txt")).getLines()) {
      output += line + System.getProperty("line.separator")
    }
    output += "--------------------------------------------------------------------------------------------------------------------" + System.getProperty("line.separator")
    pageIndexer.finalResults.foreach {(f:(String,Int)) =>  output += "RELEVANCE SCORE : " + f._2 + " for URL " + pageLoader.getDocumentLocation(f._1) + System.getProperty("line.separator") }
    val actualFile = new File(file.getAbsolutePath, "CrawlOutput" + new Random().nextInt() + ".txt")
    println(output)
    val physicalFile = actualFile.createNewFile()
    if (physicalFile) {
      val filewriter = new FileWriter(actualFile.getAbsoluteFile)
      try {
        filewriter.write(output)
        system.terminate
      }
      catch {
        case ex: Exception => system.terminate
      }
      finally {
        println("Making file" + actualFile.getName + " in " + dir)
        filewriter.close()
      }
    }
  }
}