/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

import MongoTypes.MongoServer
import Tools._

trait AkuruFunctions {

  trait DBName[T <: DomainObject] {
    val name:String
  }

  class Executor[T <: DomainObject, R](val wu: WorkUnit[T, R]) {
    def execute(implicit server: Either[String, MongoServer]): WorkResult[R] = {
      runSafelyWithEither{ server.right.flatMap (s => wu((db,col) => s.getDatabase(db).getCollection(col)) ) } match {
        case Left(error) => Left(error)
        case Right(result) => result
      }
    }
  }
}