/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru

trait Tools {

  val  defaultExceptionMessage = "Exception message returned was null. Possible NullPointerException."

  def leftToOption[T](f: Either[String, T]): Option[String] = f.left.toOption

  implicit def eitherToLeft[T](e:Either[String, T]): Lefty[T] = Lefty(e)

  case class Lefty[T](e:Either[String, T]) {
    def toLeftOption: Option[String] = e.left.toOption
  }

  def runSafelyWithEither[T](f: => T): Either[String, T] = runSafelyWithDefault[Either[String, T]](Right(f)){e => Left(nullSafeExceptionMessage(e))}

  def runSafelyWithEitherCustomError[E, T](f: => T)(x:Exception => E): Either[E, T] = runSafelyWithDefault[Either[E, T]](Right(f))(e => Left(x(e)))

  def runSafelyWithOptionReturnError[T](f: => T): Option[String] = runSafelyWithDefault[Option[String]]{f; None}(
    e => Some(nullSafeExceptionMessage(e)))

  def runSafelyWithOptionReturnResult[T](f: => T): Option[T] = runSafelyWithDefault[Option[T]]{Some(f)}(_ => None)

  def runSafelyWithDefault[T](f: => T)(default:(Exception) => T): T = {
    try {
      f
    }  catch {
      case e:Exception => default(e)
    }
  }

  def nullSafeExceptionMessage(ex:Exception): String = {
    foldOption(nullToOption(ex.getMessage))(stringAdd("\n")(defaultExceptionMessage, ex.getStackTraceString))(e => ex.getMessage)
  }

  /**
   * This function tries to safely run a resource than needs to be:
   * 1. Supplied
   * 2. Used
   * 3. Closed
   *
   * If the open function fails then no attempt is made to run f or close. The error is returned as Some(_).
   * If open succeeds and f fails then an attempt is made to run close. Whether close succeeds or fails, the error raised by f is returned as Some(_).
   * If open and f succeed, but close fails then the close error is returned as Some(_).
   * if open, f and close all succeed, None is returned.
   *
   * note: Usually open and close (and possibly f) are side-effecting functions. Given that, a failed open/f/close combination would cause
   * side-effects; although not through Exceptions, which will be transformed to Some(_).
   */
  def runSafelyWithResource[R, S, T](f:R => S)(open: => R)(close: R => T): Option[String] = {

    def functionFailed(resource:R)(error:String): Either[String, T] = { runSafelyWithEither[T](close(resource)); Left(error) }

    def functionPassed(resource:R)(result:S): Either[String, T] = runSafelyWithEither[T](close(resource))

    runSafelyWithEither[R](open).right.flatMap(resource => runSafelyWithEither[S](f(resource)).
            fold(functionFailed(resource), functionPassed(resource))).toLeftOption
  }

  def addOption[T](op1:Option[T], op2:Option[T])(f:(T, T) => T): Option[T] = {
    (op1, op2) match {
      case (None, None) => None
      case (None, Some(v)) => Some(v)
      case (Some(v), None) => Some(v)
      case (Some(v1), Some(v2)) => Some(f(v1, v2))
    }
  }

  implicit def getErrors(errors:Seq[Option[String]])(implicit f:(String, String) => String): Option[String] =  {
    if (errors.isEmpty) None else { errors.drop(1).foldLeft(errors.head)(addOption[String](_, _)(f)) }
  }

  def stringAdd(sep:String)(str1:String, str2:String): String = str1 + sep + str2

  def stringToOption(str:String): Option[String] = if (str.isEmpty) None else Some(str)

  def getStringOrDefault: ( => String) => String  => String = f => d => runSafelyWithDefault(f)(_ => d)

  //TODO: Test
  def nullToOption[A](f: => A): Option[A] = {
    val result = f
    if (result == null) None else Some(result)
  }

  //TODO: Test
  def foldOption[T, R](op:Option[T])(n: => R)(s:T => R): R = if (op.isEmpty) n else s(op.get)

  case class FoldOption[T](op:Option[T]) {
    def fold[R](n: => R, s: T => R): R = foldOption[T, R](op)(n)(s)
  }

  implicit def toFoldOption[T](op:Option[T]): FoldOption[T] = FoldOption[T](op)

  def toBoolean(value:AnyRef): Boolean = value.toString.toLowerCase match {
    case "true" => true
    case "1.0" => true
    case "1" => true
    case _ => false
  }

  def sameType[T : ClassManifest, U : ClassManifest]: Boolean = implicitly[ClassManifest[T]] >:> implicitly[ClassManifest[U]]

  def isMatch[T : ClassManifest](value:AnyRef): Boolean =
    getMatchedElement[T](value)(Some(_)) match {
      case Some(_) => true
      case None => false
    }

    def getMatchedElement[T : ClassManifest](value:AnyRef)(f: T => Option[T]): Option[T] = {
      value.asInstanceOf[Any]  match {
        case n:Int => if (sameType[T, Int]) f(n.asInstanceOf[T]) else None
        case l:Long => if (sameType[T, Long]) f(l.asInstanceOf[T]) else None
        case s:Short => if (sameType[T, Short]) f(s.asInstanceOf[T]) else None
        case by:Byte => if (sameType[T, Byte]) f(by.asInstanceOf[T]) else None
        case b:Boolean => if (sameType[T, Boolean]) f(b.asInstanceOf[T]) else None
        case c:Char => if (sameType[T, Char]) f(c.asInstanceOf[T]) else None
        case fl:Float => if (sameType[T, Float]) f(fl.asInstanceOf[T]) else None
        case d:Double => if (sameType[T, Double]) f(d.asInstanceOf[T]) else None
        case x:Any => if (implicitly[ClassManifest[T]].erasure.isAssignableFrom(x.asInstanceOf[AnyRef].getClass)) f(x.asInstanceOf[T]) else None
      }
    }

    def getElement[T : ClassManifest](value:AnyRef) : Option[T] = getMatchedElement[T](value)(Some(_))

}

object Tools extends Tools