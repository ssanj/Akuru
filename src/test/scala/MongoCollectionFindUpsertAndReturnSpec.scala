/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru

final class MongoCollectionFindUpsertAndReturnSpec extends CommonSpec {

  import Task._
  import MongoTypes.MongoObject._
  "A MongoCollection with findUpsertAndReturn" should "create a non-existant object" in {
    var handlerCalled = false
    (onTestDB ~~>
            drop[Task] ~~>
            findAndUpsertAndReturn[Task](nameField("Clean Room")) (noSort) { createTask } { t =>
                t.name.value should equal ("Clean Room")
                t.priority.value should equal (5)
                t.owner.value should equal ("sanj")
                success
              } { throw new RuntimeException("Could not upsert Task") }
    ) ~~>() verifySuccess
  }

  it should "update an existing object" in {
    (onTestDB ~~>
            drop[Task] ~~>
            save(createTask) ~~>
            save(createHPTask1) ~~>
            save(createHPTask2) ~~>
            findAndUpsertAndReturn[Task](nameField("Clean Room")) { sort(priorityField, DSC) and sort(ownerField, ASC) } {
              set(nameField("Clean Den"), priorityField(6)) } {t =>
              t.name.value should equal ("Clean Den")
              t.priority.value should equal (6)
              t.owner.value should equal ("Litterbug")
              success
            } { throw new RuntimeException("Could not upsert Task") } ~~>
            find[Task](nameField("Clean Room") and (priorityField < 6)) { all } {tasks =>
              tasks.size should equal (1)
              verifyEqual(tasks(0), createTask)
              success
            } ~~>
            find[Task](nameField("Clean Room") and (priorityField |<>| (7, 10))) { all } { tasks =>
              tasks.size should equal (1)
              verifyEqual(tasks(0), createHPTask1)
              success
            }
    ) ~~>() verifySuccess
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