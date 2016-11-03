package scala.WebCrawler_2

import scala.collection.mutable.HashSet
import scala.collection.mutable.HashMap
import scala.io.Source
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.net.URL

/**
 * Relevance calculator calculates
   relevance of a page with respect to topic, and assigns score to URLs extracted from the page.
 */

class relevanceCalculator {
  private val keywordsSet = new HashSet[String]
  private val keyWordInTitleValue = 5
  private val keyWordInTextValue = 1
  private val keyWordInURLValue = 10
  private val keyWordInPathValue = 7
  private val sureNotToBeRelevantWords = new HashSet[String]
  private val addedKeyWordsDuringRunTime = new HashSet[String]
  
  for(irrelevantWord <- Source.fromInputStream(getClass().getClassLoader().getResourceAsStream("irrelevantSearchTerms.txt")).getLines()) {
    sureNotToBeRelevantWords += irrelevantWord
    
  }
  
  for(line <- Source.fromInputStream(getClass().getClassLoader().getResourceAsStream("keywords.txt")).getLines()) {
    keywordsSet += line
  }
  
  def calculateURLRelevance(url: String, page: Document): (String, Int) = {
    val URLRelevance = calculateKeyWordScoreInURL(page)
    val titleFrequency = calculateTitleFreq(page)
    val termFrequency = calculateTermFreq(page)
    return (url, termFrequency * (URLRelevance + titleFrequency))
  }
  
  def calculateKeyWordScoreInURL(url: Document): Int = {
    val properURL = new URL(url.location())
    val domain = properURL.getHost.split('.')
    val path = properURL.getPath.split("/")
    var score = 0
    for(pathPart <- path) {
      val splitparts = pathPart.split("-")
      for(splitPart_part <- splitparts) {
        if (keywordsSet.contains(splitPart_part)) score += keyWordInPathValue
      }
      if (keywordsSet.contains(pathPart)) score += keyWordInPathValue
    }
    if (domain.size > 0) {
      if (keywordsSet.contains(domain(0))) score += keyWordInURLValue
    }
    return score
  }
  
  def calculateTitleFreq(page: Document): Int = {
    val titles = page.select("h1")
    var titleFrequency: Int = 0
    for(i <- 0 until titles.size()) {
      keywordsSet.foreach { keyword => if (titles.get(i).toString().toLowerCase().contains(keyword.toLowerCase())) titleFrequency += keyWordInTitleValue }
    }
    return titleFrequency
  }
  
  def calculateTermFreq(page: Document): Int = { 
    val p = page.select("p")
    var termFrequency: Int = 0
    for(i <- 0 until p.size()) {
      keywordsSet.foreach { keyword => if(p.get(i).toString().toLowerCase().contains(keyword.toLowerCase())) termFrequency += keyWordInTextValue }
    }
    return termFrequency
  }
  
  def extractMostCommonWord(pageBodyText: Array[String]): String = {
    val wordCountMap = new HashMap[String, Int]
    val filteredSource = pageBodyText.filter { word => sureNotToBeRelevantWords.contains(word.toLowerCase()) == false  }
    
    for(word <- filteredSource) {
      if (!wordCountMap.contains(word)) {
        wordCountMap += word -> 1
      }
      else {
        if (keywordsSet.contains(word.toLowerCase())) {
          wordCountMap(word) = wordCountMap(word) + keyWordInTextValue * 2  
        }
        else {
          wordCountMap(word) = wordCountMap(word) + keyWordInTextValue          
        }
      }
    }
    val maxVal = wordCountMap.values.max
    var mostCommonWord = ""
    wordCountMap.foreach((f:(String, Int)) => if (wordCountMap(f._1) == maxVal) mostCommonWord = f._1 )

    if (mostCommonWord.size > 1) {
      println("MOST COMMON WORD: " + mostCommonWord)
      return mostCommonWord
    }
    return ""
  }
}