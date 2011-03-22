/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec

trait CommonSpec extends FlatSpec with ShouldMatchers
        with TestDomainObjects
        with MongoFunctions
        with MongoSpecSupport
        with Tools {

  def initBlog: FutureConnection =  onTestDB ~~> drop[Blog]

  def initDailySpend: FutureConnection =  onTestDB ~~> drop[DailySpend]

  def initBook: FutureConnection =  onTestDB ~~> drop[Book]

  def initTask: FutureConnection =  onTestDB ~~> drop[Task]

  def initPerson: FutureConnection =  onTestDB ~~> drop[Person]
}