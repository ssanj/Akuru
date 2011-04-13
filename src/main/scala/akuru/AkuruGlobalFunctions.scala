/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

import MongoTypes.MongoServer

trait AkuruGlobalFunctions {

  def Success[R](wr:WorkResult[R]): WorkResult[R] = wr

  def join[T <: DomainObject, R](wu:WorkUnit[T, R])(implicit server: Either[String, MongoServer]): WorkResult[R] = new Executor[T, R](wu).execute

  def +>[T <: DomainObject, R](wu:WorkUnit[T, R])(implicit server: Either[String, MongoServer]): WorkResult[R] = join[T, R](wu)

  def Success[R](value:R): WorkResult[R] = Right(value)

  def Failure[R](error:String): WorkResult[R] = Left(error)

}