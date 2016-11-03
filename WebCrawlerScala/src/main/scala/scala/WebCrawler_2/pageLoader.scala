package scala.WebCrawler_2
import scala.io.Source
import java.util.Random
import scala.collection.mutable.HashSet
import java.net.URL
import scala.collection.mutable.HashMap
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import akka.actor._

class pageLoader {
  private val pageParser = new pageParser()
  private val timeout = 30000
  val userAgent = Array(
      "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)",
      "Googlebot/2.1 (+http://www.googlebot.com/bot.html)",
      "Googlebot/2.1 (+http://www.google.com/bot.html)",
      "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.38 Safari/537.36",
      "Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; AS; rv:11.0) like Gecko",
      "Mozilla/5.0 (compatible; MSIE 10.6; Windows NT 6.1; Trident/5.0; InfoPath.2; SLCC1; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729; .NET CLR 2.0.50727) 3gpp-gba UNTRUSTED/1.0",
      "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 7.0; InfoPath.3; .NET CLR 3.1.40767; Trident/6.0; en-IN)",
      "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; WOW64; Trident/6.0)",
      "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; Trident/6.0)",
      "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; Trident/5.0)", 
      "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; Trident/4.0; InfoPath.2; SV1; .NET CLR 2.0.50727; WOW64)",
      "Mozilla/5.0 (compatible; MSIE 10.0; Macintosh; Intel Mac OS X 10_7_3; Trident/6.0)",
      "Mozilla/4.0 (Compatible; MSIE 8.0; Windows NT 5.2; Trident/6.0)",
      "Mozilla/4.0 (compatible; MSIE 10.0; Windows NT 6.1; Trident/5.0)",
      "Mozilla/1.22 (compatible; MSIE 10.0; Windows 3.1)"
  )
  
  def tryConnect(url: String): Document = {
    try {
      return Jsoup.connect(url).userAgent(userAgent(new Random().nextInt(userAgent.size))).timeout(timeout).get();
    }
    catch {
      case ex: org.jsoup.HttpStatusException => new org.jsoup.nodes.Document("http://www.google.com")
      case ex: Exception => println(ex); new org.jsoup.nodes.Document("http://www.google.com")
    }
  }

  def getLinks(baseURL: String, url: String): HashSet[String] = {
    try { 
     val page = tryConnect(url)
     return pageParser.parsePage(baseURL, url, page)
    }
    catch {
      case ex: Exception => println(ex.getMessage); return new HashSet[String]
    }
  }
  
  def getMostCommonWord(url: String) : String = {
    val page = tryConnect(url)
    return pageParser.parseMostCommonWord(page)
  }
  
  def getDocumentLocation(url: String): String = {
    val page = tryConnect(url)
    return page.location()
  }
  
  def getDataFromGoogle(query: String): HashSet[String] = {
    val result: HashSet[String] = new HashSet[String]
    val strRequest = "https://www.google.com/search?q=" + query + "&num20"
    
    try {
      val doc: Document = tryConnect(strRequest)
      val elements = doc.select("a")
      for(i <- 0 until elements.size()) {
        val url = elements.get(i).attr("href")
        if (url.startsWith("/url?q=")) {
          result += "http://www.google.com" + url
        }
      }
    }
    catch {
      case e: Exception => println("Something went wrong... " + e);
    }
    return result
  }
  
  def parseContent(baseURL:String, url: String): (String, Int) = {
    if (url.startsWith("/url?q=")) {
      val page = tryConnect("http://www.google.com" + url)

      return pageParser.parseContent(url, page)
    }
    else {
      val page = tryConnect(url)
      return pageParser.parseContent(url, page)
    }
  }
  
}
