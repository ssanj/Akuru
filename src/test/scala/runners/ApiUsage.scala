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
import akuru.dsl.AkuruDSL
import Tools._
import MongoTypes.MongoServer

object ApiUsage extends TestDomainObjects {

  import Blog._
  import Book._
  def main(args: Array[String]) {
    import Config._
    val ex1 = new Executor[Blog, Int](afind((Blog.titleField === (".*"/)).splat)(c => c)((blogs:Seq[Blog]) => Right(blogs.size))(Left("Got 0 Blogs")))
    val ex2 = (ver:Int) => new Executor[Book, Book](afind((Book.printVersionField === ver).splat)(c => c)((books:Seq[Book]) => Right(books(0)))(Left("Got 0 Books")))

    ex1.flatMap(ex2).execute.fold(l => println(l), (r:Book) => println(r))
  }

  object Config {

    //set a default on DomainTemplate and allow the user to override as necessary.
    implicit val blogCT:DBName[Blog] = new DBName[Blog] { val name = "akuru_test" }
    implicit val bookCT:DBName[Book] = new DBName[Book] { val name = "akuru_test" }

    implicit val server:Either[String, MongoServer] = runSafelyWithEither(new MongoServer())

    //add a JVM Hook to shutdown the mongo connection.
  }
}