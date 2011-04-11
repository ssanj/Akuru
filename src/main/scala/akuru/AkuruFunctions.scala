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
  type WorkUnit[T <: DomainObject, R] = ConnectionProvider => WorkResult[R]

  type WorkResult[R] = Either[String, R]

  type ConnectionProvider = (String, String) => MongoCollection

  trait DBName[T <: DomainObject] {
    val name:String
  }

  class Executor[T <: DomainObject, R](val wu: WorkUnit[T, R]) {

    def map[S](f: R => S): Executor[T, S] = new Executor[T, S](cp => wu(cp).fold(l => Left(l), r => Right(f(r))))

    def flatMap[U <: DomainObject, S](f: R => Executor[U, S]): Executor[U, S] = new Executor[U, S](cp => wu(cp).fold(l => Left(l),
      r => f(r).wu(cp).fold(l2 => Left(l2), r2 => Right(r2))))

    def execute(implicit server: Either[String, MongoServer]): WorkResult[R] = {
      runSafelyWithEither{ server.right.flatMap (s => wu((db,col) => s.getDatabase(db).getCollection(col)) ) } match {
        case Left(error) => Left(error)
        case Right(result) => result
      }
    }
  }
}