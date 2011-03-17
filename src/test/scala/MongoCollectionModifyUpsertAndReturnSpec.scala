/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru

final class MongoCollectionModifyUpsertAndReturnSpec extends CommonSpec with AkuruDSL {

  import Task._
  import MongoTypes.MongoObject._
  "A MongoCollection with Modify and Upsert" should "create a non-existant object" in {
    ( initTask ~~>
            ( modify a Task where nameField === "Clean Room" upsertWith createTask withUpserted { t =>
                t.name.value should equal ("Clean Room")
                t.priority.value should equal (5)
                t.owner.value should equal ("sanj")
                success
              } onError error("Could not upsert Task") )
    ) ~~>() verifySuccess
  }

  it should "update an existing object" in {
    ( initTask ~~>
            save(createTask) ~~>
            save(createHPTask1) ~~>
            save(createHPTask2) ~~>
            ( modify a Task where nameField === "Clean Room" sortBy (priorityField -> DSC, ownerField -> ASC)
                    upsertWith ($set(nameField("Clean Den") & priorityField(6))) withUpserted {t =>
              t.name.value should equal ("Clean Den")
              t.priority.value should equal (6)
              t.owner.value should equal ("Litterbug")
              success
            } onError error("Could not upsert Task") ) ~~>
            ( find many Task where (nameField === "Clean Room" and priorityField < 6) withResults {tasks =>
              tasks.size should equal (1)
              verifyEqual(tasks(0), createTask)
              success
            } withoutResults error("expected 1 but got 0 hits") ) ~~>
            ( find many Task where (nameField === "Clean Room" and priorityField |<>| (7, 10)) withResults { tasks =>
              tasks.size should equal (1)
              verifyEqual(tasks(0), createHPTask1)
              success
            } withoutResults error("expected 1 but got 0 hits") )
    ) ~~>() verifySuccess
  }

  private def createTask: Task = Task(nameField("Clean Room"), priorityField(5), ownerField("sanj"))

  private def createHPTask1: Task = Task(nameField("Clean Room"), priorityField(10), ownerField("Meow"))

  private def createHPTask2: Task = Task(nameField("Clean Room"), priorityField(10), ownerField("Litterbug"))

  private def verifyEqual(task:Task, original:Task) {
    task.name.value should equal (original.name.value)
    task.priority.value should equal (original.priority.value)
    task.owner.value should equal (original.owner.value)
  }
}