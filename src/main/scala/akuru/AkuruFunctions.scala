/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

import MongoTypes.MongoCollection
import MongoTypes.MongoServer
import Tools._

trait AkuruFunctions {

  /**
   * A basic type that works on a DomainObject T and returns a result R.
   */
  type WorkUnit[T <: DomainObject, R] = MongoServer => Either[String, R]

  trait ConnectionType[T <: DomainObject] {
    val colName:String
    val dbName:String
  }

  case class Executor[T <: DomainObject, R](wu: WorkUnit[T, R]) {

    def map[S](f: R => S): Executor[T, S] = Executor[T, S](server => wu(server).fold(l => Left(l), r => Right(f(r))))

    def flatMap[U <: DomainObject, S](f: R => Executor[U, S]): Executor[U, S] = Executor[U, S](server => wu(server).fold(l => Left(l),
      r => f(r).wu(server).fold(l2 => Left(l2), r2 => Right(r2))))

    def execute(implicit server: Either[String, MongoServer]): Either[String, R] = {
      runSafelyWithEither{ server.right.flatMap (s => wu(s)) } match {
        case Left(error) => Left(error)
        case Right(result) => result
      }
    }
  }
}