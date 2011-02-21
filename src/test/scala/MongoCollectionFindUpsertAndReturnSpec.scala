/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru

final class MongoCollectionFindUpsertAndReturnSpec extends CommonSpec {

  import Task._
  import MongoTypes.MongoObject._
  import MongoTypes.SortOrderEnum._
  import SortObjectJoiner._
  "A MongoCollection with findUpsertAndReturn" should "create a non-existant object" in {
    var handlerCalled = false
    (onTestDB ~~>
            drop[Task] ~~>
            findAndUpsertAndReturn(nameField("Clean Room")) (noSort) { createTask } { t:Task =>
                t.name.value should equal ("Clean Room")
                t.priority.value should equal (5)
                t.owner.value should equal ("sanj")
                success
              } { throw new RuntimeException("Could not upsert Task")
    } ~~>()) verifySuccess
  }

  it should "update an existing object" in {
    (onTestDB ~~>
            drop[Task] ~~>
            save(createTask) ~~>
            save(createHPTask1) ~~>
            save(createHPTask2) ~~>
            findAndUpsertAndReturn(nameField("Clean Room")) { sort(priorityField, DSC) and sort(ownerField, ASC) } {
              set(nameField("Clean Den"), priorityField(6)) } {t: Task =>
              t.name.value should equal ("Clean Den")
              t.priority.value should equal (6)
              t.owner.value should equal ("Litterbug")
              success
            } { throw new RuntimeException("Could not upsert Task") } ~~>
            find(nameField("Clean Room")) {tasks:Seq[Task] =>
              val sorted = tasks.sortWith((a, b) => a.priority.value < b.priority.value)
              sorted.size should equal (2)
              verifyEqual(sorted(0), createTask)
              verifyEqual(sorted(1), createHPTask1)
              success
    } ~~>()) verifySuccess
  }

  private def createTask: Task = Task(name = nameField("Clean Room"), priority = priorityField(5), owner = ownerField("sanj"))

  private def createHPTask1: Task = Task(name = nameField("Clean Room"), priority = priorityField(10), owner = ownerField("Meow"))

  private def createHPTask2: Task = Task(name = nameField("Clean Room"), priority = priorityField(10), owner = ownerField("Litterbug"))

  private def verifyEqual(task:Task, original:Task) {
    task.name.value should equal (original.name.value)
    task.priority.value should equal (original.priority.value)
    task.owner.value should equal (original.owner.value)
  }
}