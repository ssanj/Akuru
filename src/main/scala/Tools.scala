/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru

trait Tools {

  def leftToOption[T](f: Either[String, T]): Option[String] = f.left.toOption

  implicit def eitherToLeft[T](e:Either[String, T]): Lefty[T] = Lefty(e)

  case class Lefty[T](e:Either[String, T]) {
    def toLeftOption: Option[String] = e.left.toOption
  }

  def runSafelyWithEither[T](f: => T): Either[String, T] = runSafelyWithDefault[Either[String, T]](Right(f))(e => Left(e.getMessage))

  def runSafelyWithEitherCustomError[E, T](f: => T)(x:Exception => E): Either[E, T] = runSafelyWithDefault[Either[E, T]](Right(f))(e => Left(x(e)))

  def runSafelyWithOptionReturnError[T](f: => T): Option[String] = runSafelyWithDefault[Option[String]]{f; None}(e => Some(e.getMessage))

  def runSafelyWithOptionReturnResult[T](f: => T): Option[T] = runSafelyWithDefault[Option[T]]{Some(f)}(_ => None)

  def runSafelyWithDefault[T](f: => T)(default:(Exception) => T): T = {
    try {
      f
    }  catch {
      case e:Exception => default(e)
    }
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
}