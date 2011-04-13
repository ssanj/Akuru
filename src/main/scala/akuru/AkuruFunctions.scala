/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

import MongoTypes.MongoServer
import Tools._

trait AkuruFunctions {

  class Executor[T <: DomainObject, R] private[akuru] (private val wu: WorkUnit[T, R]) {
    def execute(implicit server: Either[String, MongoServer]): WorkResult[R] = {
      runSafelyWithEither{ server.right.flatMap (s => wu((db,col) => s.getDatabase(db).getCollection(col)) ) } match {
        case Left(error) => Left(error)
        case Right(result) => result
      }
    }
  }

  final case class ExecutionResult[R] private[akuru](private val wr:WorkResult[R]) {
    def withSuccess[T](success:R => T) = new {
      def withFailure(error:String => T): T = wr.fold(l => error(l), r => success(r))
    }

    def workResult: WorkResult[R] = wr
  }
}