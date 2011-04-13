/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

trait AkuruGlobalFunctions {

  def Success[R](wr:WorkResult[R]): WorkResult[R] = wr

  def merge[T <: DomainObject, R](wu:WorkUnit[T, R])(implicit server: Either[String, MongoServer]): WorkResult[R] = new Executor[T, R](wu).execute

  def +>[T <: DomainObject, R](wu:WorkUnit[T, R])(implicit server: Either[String, MongoServer]): WorkResult[R] = merge[T, R](wu)

  def Success[R](value:R): WorkResult[R] = Right(value)

  def Failure[R](error:String): WorkResult[R] = Left(error)

}