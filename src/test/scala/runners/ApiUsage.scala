/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package runners

import akuru._
import akuru.domain.TestDomainObjects
import akuru.dsl.DSLTools

object ApiUsage extends TestDomainObjects with AkuruFinder with AkuruMongoWrapper with AkuruFunctions with Tools with DSLTools {

  def main(args: Array[String]) {
    import Config._
    (find * Blog where Blog.titleField === (".*"/) withResults (blogs => Success(blogs(0).title.value)) withoutResults (Failure("No dice!"))).
            execute withSuccess (r => println("The blog title is -> " + r)) withFailure (println(_))

    (find * Blog where Blog.titleField === ("d.*"/) withResults (blogs => Success(blogs(0).title.value)) withoutResults {
      +>(find * Book where Book.nameField === ("d.*"/) withResults (others => Success(others(0).name.value)) withoutResults {
        +>(find * Blog where Blog.titleField === (".*"/) withResults (blogs => Success(blogs(0).title.value)) withoutResults (
                Failure("Could not find Blog starting with 'd' nor books starting with 'd' nor any blogs of any title")))})}).
            execute withSuccess(r => println("The other blog title is -> " + r)) withFailure (e => println("failed => " + e))

    (find * Blog where Blog.titleField === (".*"/) withResults {blogs =>
      merge(find * Book where Book.nameField === ("pisc"/i) withResults (books => Success(blogs(0).title.value, books(0).name.value))
              withoutResults (Failure("Could not find matching blog and book")))
    } withoutResults (Failure("Could not find blogs!"))).execute withSuccess(r => println("The title2 is -> " + r)) withFailure (l => println(l))
  }

  trait AkuruConfig {
    val dbName:String

    implicit def defaultDBName[T <: DomainObject](dt:DomainTemplate[T]): DBName[T] = new DBName[T] { val name = dbName }

    implicit lazy val server:Either[String, MongoServer] = Tools.runSafelyWithEither(new MongoServer())

    //if you need to add additional databases to specific domain objects define an implicit conversion for each DomainObject:
    //Eg.
    // implicit def blogDBName(blog:DomainTemplate[Blog]): DBName[Blog] = new DBName[Blog] { val name = "meh" }

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

  object Config extends AkuruConfig {
    val dbName = "akuru_test"
    implicit def blogDBName(blog:DomainTemplate[Blog]): DBName[Blog] = new DBName[Blog] { val name = "akuru" }
  }
}