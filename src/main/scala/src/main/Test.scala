package src.main

import util.control.Breaks._


object Test extends App {

  println(CodeData.getCodes.size)

  breakable {
    for (i <- 1 to 10) {
      if (i == 2) break() else println(i)
    }
  }

  for(j <- 11 to 13){
    for (i <- 1 to 10) {
      breakable {
        if (i == 2) break() else println(i)
      }
    }
    println(j)
  }



}
