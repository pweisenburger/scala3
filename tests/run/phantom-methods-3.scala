object Test {
  import Boo._

  def main(args: Array[String]): Unit = {
    fun(phantomFun3(boo[Blinky]))
    fun(phantomFun3(boo[Inky]))
    fun(phantomFun3(boo[Pinky]))
  }

  def fun(unused top: Blinky): Unit = println("fun")

  unused def phantomFun3[P <: Blinky](unused p7: P): Blinky = p7

}

object Boo extends Phantom {
  type Blinky <: Boo.Any
  type Inky <: Blinky
  type Pinky <: Inky
  unused def boo[B <: Boo.Any]: B = assume
}
