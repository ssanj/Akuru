/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

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

  trait AkuruConfig {
    val dbName:String

    implicit def defaultDBName[T <: DomainObject](dt:DomainTemplate[T]): DBName[T] = new DBName[T] { val name = dbName }

    implicit lazy val server:Either[String, MongoServer] = Tools.runSafelyWithEither(new MongoServer())

    //if you need to add additional databases to specific domain objects define an implicit conversion for each DomainObject:

    private def threaded(close: MongoServer => Unit): Option[Thread] = {
      server.fold(l => None, s => Some(new Thread(new Runnable { def run() { close(s); err("Closed connection") }})))
    }

    private def registerShutdownHook(ot: => Option[Thread]) {
      ot fold ({}, thread =>
        runSafelyWithDefault(Runtime.getRuntime.addShutdownHook(thread))(e =>
          err("Error Initializing Akuru Configuration. The Following error was received: " + e)))
      }

    private def err(message:String) { System.err.println(message) }

    registerShutdownHook(threaded(s => s.close))
  }
}