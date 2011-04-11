/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

import MongoTypes.MongoCollection
import MongoTypes.MongoServer
import MongoTypes.MongoDatabase
import Tools._

trait AkuruFunctions {

  type CollectionProvider = (String => MongoCollection)

  type WorkUnit[T] = CollectionProvider => Either[String, T]

  case class Executor[T](wu: WorkUnit[T]) {

    def map[U](f: T => U): Executor[U] = Executor[U](cp => wu(cp).fold(l => Left(l), r => Right(f(r))))

    def flatMap[U](f: T => Executor[U]): Executor[U] = Executor[U](cp => wu(cp).fold(l => Left(l), r => f(r).wu(cp).fold(l2 => Left(l2),
      r2 => Right(r2))))

    def execute(implicit server: Either[String, MongoServer]): Either[String, T] = {
      runSafelyWithEither{ server.right.flatMap (s => wu(s.getDatabase("").getCollection(_))) } match {
        case Left(error) => Left(error)
        case Right(result) => result
      }
    }
  }
}