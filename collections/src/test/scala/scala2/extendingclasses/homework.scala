package scala2.extendingclasses

import scala.Extensionmethods.Circle
/*
object homework1 {
  extension (x: String)
    def + (x2: String): Int = (x.concat(x2)).toInt

    @main def part1Ex(): Unit ={
      println("1" + "33") // результат сложения все равно string
      // val y : Int = "1" + "33" - здесь extension игнорируется и пишет ошибку, что нужен Int. Как быть?
    }
}

object homework2 {
  object Completions {
    enum CompletionArg {
      case StringArg(s: String)
      case FloatArg (f: Float)
      case IntArg (i: Int)
    }

    object CompletionArg {
      given fromString: Conversion[String, CompletionArg] = StringArg(_)

      given fromInt: Conversion[Int, CompletionArg] = IntArg(_)

      given fromFloat: Conversion[Float, CompletionArg] = FloatArg(_)
    }

    import Completions.CompletionArg.*

    def complete[T] (arg: CompletionArg) = arg match
      case StringArg(s) => s
      case FloatArg(f) => f
      case IntArg(i) => i
  }
  //import Completions.CompletionArg.*

  @main def part2Ex(): Unit ={
    println(Completions.complete("String"))
    println(Completions.complete(1))
    println(Completions.complete(7f))
  }
}


object homework3 {
  opaque type Logarithm = Double

  object Logarithm{
    def apply(d: Double): Logarithm = math.log(d)

    def safe(d: Double): Option[Logarithm] = 
      if d > 0.0 then Some(math.log(d)) else None
  }

  extension (x: Logarithm)
    def toDouble: Double = math.exp(x)
    def + (y: Logarithm): Logarithm = Logarithm(math.exp(x) + math.exp(y))
    def * (y: Logarithm): Logarithm = x + y

  @main def part3Ex(): Unit ={
    import Logarithm

    val l = Logarithm(1.0)
    val l2 = Logarithm(2.0)
    val l3 = l * l2
    val l4 = l + l2

  }
}*/