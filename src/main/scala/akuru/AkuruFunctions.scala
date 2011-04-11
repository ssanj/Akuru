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
    def execute(implicit server: Either[String, MongoServer]): Either[String, T] = {
      runSafelyWithEither{ server.right.flatMap (s => wu(s.getDatabase("").getCollection(_))) } match {
        case Left(error) => Left(error)
        case Right(result) => result
      }
    }
  }
}