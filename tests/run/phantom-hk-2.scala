object Test {
  import Boo._

  type HKPhantom[X <: BooAny] = X

  def main(args: Array[String]): Unit = {
    fun(hkFun2(boo[Blinky]))
    fun(hkFun2(boo[Inky]))
    fun(hkFun2(boo[Pinky]))
  }

  def fun(unused top: BooAny): Unit = println("hk2")

  unused def hkFun2[Y <: BooAny](unused p10: HKPhantom[Y]): HKPhantom[Y] = p10
}


object Boo extends Phantom {
  type BooAny = Boo.Any
  type Blinky <: Boo.Any
  type Inky <: Blinky
  type Pinky <: Inky
  unused def boo[B <: Boo.Any]: B = assume
}
