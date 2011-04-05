/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

import MongoTypes.MongoError

trait WrapWithTrait {

  def wrapWithKey[T](f: => T)(key:String): Either[MongoError, T] = {
    try {
      Right(f)
    } catch {
      case n:NullPointerException => Left(MongoError(key + " is an invalid key.", n.getStackTraceString))
      case e => Left(MongoError(e.getMessage, e.getStackTraceString))
    }
  }

  def wrapWith[T](f: => T): Either[MongoError, T] = {
    try {
      Right(f)
    } catch {
      case e => Left(MongoError(e.getMessage, e.getStackTraceString))
    }
  }

}