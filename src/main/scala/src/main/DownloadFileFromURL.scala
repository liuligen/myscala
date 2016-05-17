package src.main

import sys.process._
import java.net.URL
import java.io.File
import akka.actor.{Actor, ActorSystem, Props, ReceiveTimeout}


final case class GetCode(code: String)

class DownloadFileFromURL(fileName:String) extends Actor {

  def receive = {
    case GetCode(code: String) => fileDownloader("http://market.finance.sina.com.cn/downxls.php?date="+fileName+"&symbol="+code, "download/20160513/"+code+"-"+fileName+".xls")
    case ReceiveTimeout        => throw new RuntimeException("received timeout")
  }

  def fileDownloader(url: String, filename: String) = {
    new URL(url) #> new File(filename) !!
  }
}

object DownloadFileFromURL extends App {

  val system = ActorSystem()

//  CodeData.getCodes.map(invoke(_))
  def invoke(code : String): Unit ={
    val actor = system.actorOf(Props(new DownloadFileFromURL("2016-05-13")))
    actor ! GetCode(code)
  }

  CodeData.getCodes.par.map(invoke(_))
  CodeData.getCodes2.par.map(invoke(_))
  CodeData.getCodes3.par.map(invoke(_))
  CodeData.getCodes4.par.map(invoke(_))
  CodeData.getCodes5.par.map(invoke(_))


//  system.shutdown


}
