package main.scala

/**
 * Created by ligen on 16/1/6.
 */
object HelloWorld {
  var name: String = "llugen"
  var age, stature = 2

    def main(args: Array[String]): Unit = {
      Console.println(welcome("ligen"))
      System.out.println("Hello, Scala!")
    }

  def welcome(name:String) = {"Hello " + name}

  def appay() {}

}