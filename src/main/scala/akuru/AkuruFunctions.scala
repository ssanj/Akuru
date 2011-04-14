/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

import Tools._

trait AkuruFunctions {

  /**
   * "Runs" or executes a given <code>WorkUnit</code> to return a <code>WorkResult</code>within the context of a MongoServer/Database/Collection
   * combination when the <code>execute</code> method is invoked. This class is largely invisible to the user due to global implicit conversion that
   * converts a <code>WorkUnit</code> to an <code>Executor</code>. The <code>execute</code> can be called without parameters since the server
   * will be supplied implicitly.
   *
   * It can be used as follows:
   *
   * <code>
   *   someWorkUnit[T,R] execute
   * </code>
   */
  class Executor[T <: DomainObject, R] private[akuru] (private val wu: WorkUnit[T, R]) {
    def execute(implicit server: Either[String, MongoServer]): WorkResult[R] = {
      runSafelyWithEither{ server.right.flatMap (s => wu((db,col) => s.getDatabase(db).getCollection(col)) ) } match {
        case Left(error) => Left(error)
        case Right(result) => result
      }
    }
  }

  /**
   * This simple wrapper around <code>WorkResult</code> provides a short DSL to handle success and failure conditions. There is a global implicit
   * that converts a <code>WorkResult</code> to an <code>ExecutionResult</code>.
   *
   * It can be used as follows:
   *
   * <code>
   *   someWorkResult[R] withSuccess(R => ) withFailure(String => )
   * </code>
   *
   */
  final case class ExecutionResult[R] private[akuru](private val wr:WorkResult[R]) {
    def withSuccess[T](success:R => T) = new {
      def withFailure(error:String => T): T = wr.fold(l => error(l), r => success(r))
    }

    def workResult: WorkResult[R] = wr
  }

  /**
   * Define the Akuru configuration for your project by extending this trait. Then make that config available via a object or some other mechanism.
   *
   * This configuration provides the user with the following:
   *
   * 1. A MongoServer instance. (implicit)
   * 2. A default database name to use with all <code>DomainObject</code>s.
   * 3. A JVM shutdownHook to close an open connections.
   *
   * The simplest config, simply overrides <code>defaultDBName</code> and provides the name of the default database to use for all
   * <code>DomainObject</code>s:
   * <code>
   * object Config extends AkuruConfig {
   *   override defaultDBName = "my_database_name"
   * }
   * </code>
   *
   * If you prefer a different MongoServer configuration, then simply override the server definition in your config object.
   *
   * If you need a different database for one or more <code>DomainObject</code>s you need to add additional implicits for each custom database name
   * in your config object. Implicits can be defined in terms of <code>defineDBName</code>.
   *
   * Eg. Given a <code>Person</code> <code>DomainObject</code>, it would be of the form:
   *
   * <code>
   * implicit def personDBName(dt:DomainTemplate[Person]): DBName[Person] = defineDBName[Person](custom_database_name)
   * </code>
   *
   * Now all <code>DomainObject</code>s other than <code>Person</code> will use the default database name and <code>Person</code> will use the
   * custom_database_name.
   */
  trait AkuruConfig {
    //the name of the default database to use for all domain objects.
    val defaultDBName:String

    //define custom database names where required by calling this method in your config object.
    def defineDBName[T <: DomainObject](dbn:String): DBName[T] = new DBName[T] { val name = dbn }

    implicit def commonDBName[T <: DomainObject](dt:DomainTemplate[T]): DBName[T] = defineDBName[T](defaultDBName)

    //If you need a different server configuration, override this method in your config object.
    implicit lazy val server:Either[String, MongoServer] = runSafelyWithEither(new MongoServer())

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