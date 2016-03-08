package test

import dotty.tools.dotc.Compiler
import dotty.tools.dotc.core.Phases.Phase
import dotty.tools.dotc.typer.FrontEnd
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.ast.tpd
import dotty.tools.dotc.ast.Trees._

/** All tests need to extend this trait and define `source` and `assertion`
 *
 * Instances also need to be placed in the `DottyDocTests::tests` sequence to
 * be included in the run
 */
trait DottyDocTest extends DottyTest { self =>
  /** Source code in string format */
  def source: String

  /** A test to run against the resulting code */
  def assertion: PartialFunction[Tree[Untyped], Unit]

  private def defaultAssertion: PartialFunction[Tree[Untyped], Unit] = {
    case x => assert(false, "Couldn't match resulting AST to expected AST in: " + x.show)
  }

  private def compiler(assertion: PartialFunction[Tree[Untyped], Unit]) = new Compiler {
    override def phases = {
      val checker = new Phase {
        def phaseName = "assertionChecker"
        override def run(implicit ctx: Context): Unit =
          (assertion orElse defaultAssertion)(ctx.compilationUnit.untpdTree)
      }

      List(List(new FrontEnd)) ::: List(List(checker))
    }
  }

  def checkDocString(actual: Option[String], expected: String): Unit = actual match {
    case Some(str) => {
      assert(str == expected, s"""Docstring: "$str" didn't match expected "$expected"""")
    }
    case None =>
      assert(false, s"""No docstring found, expected: "$expected"""")
  }

  def run(): Unit = {
    val c = compiler(assertion)
    c.rootContext(ctx)
    c.newRun.compile(source)
    println(s"${self.getClass.getSimpleName.split("\\$").last} passed")
  }
}

/** Add tests to the `tests` sequence */
object DottyDocTests extends DottyTest {
  private[this] val tests = Seq(
    SingleClassInPackage,
    MultipleOpenedOnSingleClassInPackage,
    MultipleClassesInPackage,
    SingleCaseClassWithoutPackage,
    SingleTraitWihoutPackage,
    MultipleTraitsWithoutPackage,
    MultipleMixedEntitiesWithPackage
  )

  def main(args: Array[String]): Unit = {
    println("------------ Testing DottyDoc  ------------")
    tests.foreach(_.run)
    println("--------- DottyDoc tests passed! ----------")
  }
}

case object SingleClassInPackage extends DottyDocTest {
  override val source =
    """
    |package a
    |
    |/** Hello world! */
    |class Class(val x: String)
    """.stripMargin

    override def assertion = {
      case PackageDef(_, Seq(t @ TypeDef(name, _))) if name.toString == "Class" =>
        checkDocString(t.rawComment, "/** Hello world! */")
    }
}

case object MultipleOpenedOnSingleClassInPackage extends DottyDocTest {
  override val source =
    """
    |package a
    |
    |/** Hello /* multiple open */ world! */
    |class Class(val x: String)
    """.stripMargin

  override def assertion = {
    case PackageDef(_, Seq(t @ TypeDef(name, _))) if name.toString == "Class" =>
      checkDocString(t.rawComment, "/** Hello /* multiple open */ world! */")
  }
}

case object MultipleClassesInPackage extends DottyDocTest {
  override val source =
    """
    |package a
    |
    |/** Class1 docstring */
    |class Class1(val x: String)
    |
    |/** Class2 docstring */
    |class Class2(val x: String)
    """.stripMargin

  override def assertion = {
    case PackageDef(_, Seq(c1 @ TypeDef(_,_), c2 @ TypeDef(_,_))) => {
      checkDocString(c1.rawComment, "/** Class1 docstring */")
      checkDocString(c2.rawComment, "/** Class2 docstring */")
    }
  }
}

case object SingleCaseClassWithoutPackage extends DottyDocTest {
  override val source =
    """
    |/** Class without package */
    |case class Class(val x: Int)
    """.stripMargin

  override def assertion = {
    case PackageDef(_, Seq(t @ TypeDef(_,_))) => checkDocString(t.rawComment, "/** Class without package */")
  }
}

case object SingleTraitWihoutPackage extends DottyDocTest {
  override val source = "/** Trait docstring */\ntrait Trait"

  override def assertion = {
    case PackageDef(_, Seq(t @ TypeDef(_,_))) => checkDocString(t.rawComment, "/** Trait docstring */")
  }
}

case object MultipleTraitsWithoutPackage extends DottyDocTest {
  override val source =
    """
    |/** Trait1 docstring */
    |trait Trait1
    |
    |/** Trait2 docstring */
    |trait Trait2
    """.stripMargin

  override def assertion = {
    case PackageDef(_, Seq(t1 @ TypeDef(_,_), t2 @ TypeDef(_,_))) => {
      checkDocString(t1.rawComment, "/** Trait1 docstring */")
      checkDocString(t2.rawComment, "/** Trait2 docstring */")
    }
  }
}

case object MultipleMixedEntitiesWithPackage extends DottyDocTest {
  override val source =
    """
    |/** Trait1 docstring */
    |trait Trait1
    |
    |/** Class2 docstring */
    |class Class2(val x: Int)
    |
    |/** CaseClass3 docstring */
    |case class CaseClass3()
    |
    |case class NoComment()
    |
    |/** AbstractClass4 docstring */
    |abstract class AbstractClass4(val x: Int)
    """.stripMargin

  override def assertion = {
    case PackageDef(_, Seq(t1 @ TypeDef(_,_), c2 @ TypeDef(_,_), cc3 @ TypeDef(_,_), _, ac4 @ TypeDef(_,_))) => {
      checkDocString(t1.rawComment, "/** Trait1 docstring */")
      checkDocString(c2.rawComment, "/** Class2 docstring */")
      checkDocString(cc3.rawComment, "/** CaseClass3 docstring */")
      checkDocString(ac4.rawComment, "/** AbstractClass4 docstring */")
    }
  }
}
