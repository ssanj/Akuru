/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package runners

import akuru._
import akuru.domain.TestDomainObjects
import MongoTypes.Executor
import MongoTypes.afind
import MongoTypes.DBName
import Tools._
import MongoTypes.MongoServer
import akuru.dsl.{DSLTools, AkuruDSL}

object ApiUsage extends TestDomainObjects with AkuruFinder with AkuruMongoWrapper with AkuruFunctions with Tools with DSLTools {

  import Blog._
  import Book._
  def main(args: Array[String]) {
    import Config._
    val ex1 = new Executor[Blog, Int](afind((Blog.titleField === (".*"/)).splat)(c => c)((blogs:Seq[Blog]) => Right(blogs.size))(Left("Got 0 Blogs")))
    val ex2 = (ver:Int) => new Executor[Book, Book](afind((Book.printVersionField === ver).splat)(c => c)((books:Seq[Book]) => Right(books(0)))(Left("Got 0 Books")))

    ex1.flatMap(ex2).execute.fold(l => println(l), (r:Book) => println(r))


    (find * Blog where Blog.titleField === (".*"/) withResults (blogs =>Right(blogs(0).title.value)) withoutResults (Right("No dice!"))).
            execute.fold(l => println(l), r => println("The title is -> " + r))
  }

  object Config {

    implicit def WorkUnitToExecutor[T <: DomainObject, R](wu:WorkUnit[T, R]): Executor[T, R] = new Executor[T, R](wu)

    //set a default on DomainTemplate and allow the user to override as necessary.
    implicit val blogCT:DBName[Blog] = new DBName[Blog] { val name = "akuru_test" }
    implicit val bookCT:DBName[Book] = new DBName[Book] { val name = "akuru_test" }

    implicit val server:Either[String, MongoServer] = runSafelyWithEither(new MongoServer())

    //add a JVM Hook to shutdown the mongo connection.
  }
}