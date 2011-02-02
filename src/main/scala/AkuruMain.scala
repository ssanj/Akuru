/*
  * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru

import MongoTypes._

object AkuruMain extends DomainObjects with Tools with SideEffects with MongoFunctions with DomainSupport {


  def main(args: Array[String]) {
    val labelList = List("work", "movement")
    val blog = Blog(title = "Lessons learned", labels = labelList.toSeq)
    val blogs:List[Blog] = (for (n <- 1 to 100) yield Blog(title = "Lessons learned" + n , labels = labelList.toSeq)).toList

    val result = {withAkuru ->
            (save(blog)) ->> (labelList.map(l => save(Label(value = l)) _)) ->> (blogs.map(b => save(b) _))}.run.getOrElse("success >>")
    println(result)
  }

  def withAkuru: FutureConnection = withConnection(createServer)("akuru")
}