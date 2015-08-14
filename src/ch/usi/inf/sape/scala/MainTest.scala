package ch.usi.inf.sape.scala

import scala.util.parsing.json.JSONObject
import scala.util.parsing.json.JSON

//import scala.reflect.internal.Trees.Tree

/**
 * @author Luis Mastrangelo (luis.mastrangelo@usi.ch)
 */
object MainTest {
  def power(b: String, n: Int): String =
    if (n == 0) s"1.0" else s"($b * ${power(b, n - 1)})"

  implicit class JsonHelper(val sc: StringContext) extends AnyVal {
    def json(args: Any*): String = {
      val strings = sc.parts.iterator
      val expressions = args.iterator
      var buf = new StringBuffer(strings.next)
      while (strings.hasNext) {
        buf append expressions.next
        buf append strings.next
      }
      buf.toString
    }
  }

  def main(args: Array[String]) {
    val name = "Hola"
    val id = 123

    println(json"{ name: $name, id: $id }")

    println(power("asdf", 1))
    println(power("asdf", 2))
    println(power("asdf", 3))
    println(power("asdf", 4))
  }
}
