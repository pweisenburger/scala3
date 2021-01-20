trait ObjectInterface with
  def equals(obj: Any): Boolean
  def hashCode(): Int
  def toString(): String

trait SAMPlainWithExtends extends ObjectInterface with
  def first(): String

trait SAMCovariantOutExtends[+O] extends ObjectInterface with
  def first(): O

trait SAMContravariantInExtends[-I] extends ObjectInterface with
  def first(in: I): Unit

trait SAMInvariantExtends[T] extends ObjectInterface with
  def first(in: T): T

trait SAMInOutExtends[-I, +O] extends ObjectInterface with
  def first(in: I): O

type CustomString = String
type CustomBoolean = Boolean
type CustomInt = Int

trait SAMWithCustomAliases with
  def first(): String
  def equals(obj: Any): CustomBoolean
  def hashCode(): CustomInt
  def toString(): CustomString

object Test with

  def main(args: Array[String]): Unit =
    val samPlainWithExtends : SAMPlainWithExtends = () => "o"
    val samCovariantOutExtends : SAMCovariantOutExtends[_] = () => "o"
    val samContravariantInExtends : SAMContravariantInExtends[Int] = (x: Int) => ()
    val samInvariantExtends : SAMInvariantExtends[String] = (x: String) => x
    val samInOutExtends : SAMInOutExtends[Int, String] = (x: Int) => x.toString
    val samWithCustomAliases : SAMWithCustomAliases = () => "o"

    println(samPlainWithExtends.first())
    println(samCovariantOutExtends.first())
    println(samContravariantInExtends.first(10))
    println(samInvariantExtends.first("i"))
    println(samInOutExtends.first(10))
    println(samWithCustomAliases.first())
