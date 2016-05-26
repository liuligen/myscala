package src.main

import java.io.{IOException, File}
import java.net.{UnknownHostException, URL}

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.routing.RoundRobinRouter

import scala.io.Source
import scala.sys.process._


final case class GetCode(code: String, path: String)

case class Msg_Req(date: String, code: String, path: String)

case class Msg_Resp(index: String)

case class Msg_Start(list: List[String], date: String, path: String)

case class Msg_Finished()


class DownloadFileFromURL(listener: ActorRef) extends Actor {

  var result: List[Int] = Nil
  var numWorkers = 0
  var count = 0

//  var router = {
//    val routees = Vector.fill(5) {
//      val r = context.actorOf(Props[Worker])
//      context watch r
//      ActorRefRoutee(r)
//    }
//    Router(RoundRobinRoutingLogic(), routees)
//  }

  def receive = {

//    case w: Worker =>
//      router.route(w, sender())

    case Msg_Start(list, date, path) =>
      numWorkers = list.size
      val workerRouter = context.actorOf(
        Props[Worker].withRouter(RoundRobinRouter(40)))
      list.par.map(x =>
//        context.actorOf(
//          Props[Worker].withRouter(RoundRobinRouter(1)), "Worker-Router" + x) ! Msg_Req(date, x, path)
          workerRouter ! Msg_Req(date, x, path)
      )

    case Msg_Resp(stockCode) =>
      print(".")
      if (!stockCode.isEmpty) println(stockCode + ": can't download")
      count = count + 1
      if (count >= numWorkers) {
        listener ! Msg_Finished()
        context.stop(self)
        println("Finish!")
      }
  }
}

class Worker extends Actor {
  def receive = {
    case Msg_Req(date: String, code: String, path: String) =>
      println(Thread.currentThread().getId())

      val result = fileDownloader("http://market.finance.sina.com.cn/downxls.php?date=" + date + "&symbol=" + code, path + date + "/" + code + "_交易明细_" + date + ".xls")

      sender ! Msg_Resp(if (result.isEmpty) "" else code)
  }

  def fileDownloader(url: String, filename: String) : String = {
    try{
      new URL(url) #> new File(filename) !!
    }catch {
      case e: RuntimeException => "error"
      case e: UnknownHostException => "error"
      case e: IOException => "error"
    }
  }
}

class Listener extends Actor {
  def receive = {
    case Msg_Finished() =>
      context.system.shutdown
  }
}

object DownloadFileFromURL {
  val system = ActorSystem("DownloadStockFile")
  def main(args: Array[String]) = {
    if (args.size < 3)
      throw new IllegalArgumentException("parameters's size not enough");

    var filePath = args.apply(0)
    var date = args.apply(1)
    var downloadPath = args.apply(2)
    var codeList: List[String] = Source.fromFile(filePath).getLines.toList

    var downloadDirectoryName: String = downloadPath + date
    var dir: File = new File(downloadDirectoryName)
    if (!dir.exists())
      dir.mkdir()

    val listener = system.actorOf(Props[Listener], "listener")
    downloadStockData(listener, codeList.slice(0,30), date, downloadPath)
  }

  def downloadStockData(listener:ActorRef, stockCodes: List[String], dateString: String, downloadPath: String): Unit = {
    val actor = system.actorOf(Props(new DownloadFileFromURL(listener)))
    actor ! Msg_Start(stockCodes, dateString, downloadPath)
  }

}
