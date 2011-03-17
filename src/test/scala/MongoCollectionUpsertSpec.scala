/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru;
final class MongoCollectionUpsertSpec extends AkuruDSL with CommonSpec {

  import Task._
  import MongoTypes.MongoObject.$set

  val task = Task(Task.nameField === "Cartoon", Task.priorityField === 5, Task.ownerField === "Bugs Bunny")
  "MongoCollection with Upserts" should "insert a Domain Object that does not exist" in {
    ( initTask ~~>
        ( find * Task where (ownerField === "Bugs Bunny") withResults (t => error("Found a Task")) withoutResults (success) ) ~~>
        ( upsert a Task where (ownerField === "Bugs Bunny") withValues (task) returnErrors ) ~~>
        ( find * Task where (ownerField === "Bugs Bunny") withResults {tasks =>
          tasks.size should equal (1)
          val t = tasks(0)
          t.name.value should equal ("Cartoon")
          t.priority.value should equal (5)
          t.owner.value should equal ("Bugs Bunny")
          success
        } withoutResults error("Did not find Task") )
    ) ~~>() verifySuccess
  }

  it should "only update a Domain Object that exist" in {
    ( initTask ~~>
        (save(task)) ~~>
        ( find * Task where (ownerField === "Bugs Bunny") withResults { tasks =>
          tasks.size should equal (1)
          val t = tasks(0)
          t.name.value should equal ("Cartoon")
          t.priority.value should equal (5)
          t.owner.value should equal ("Bugs Bunny")
          success
        } withoutResults error("Did not find Bugs") ) ~~>
        ( upsert a Task where (ownerField === "Bugs Bunny") withValues ($set(ownerField === "Elmer Fudd" & priorityField === 1)) returnErrors ) ~~>
        ( find * Task where (ownerField === "Bugs Bunny") withResults (t => error("Found Bugs not Elmer")) withoutResults (success) ) ~~>
        ( find * Task where (ownerField === "Elmer Fudd") withResults {tasks =>
          tasks.size should equal (1)
          val t = tasks(0)
          t.owner.value should equal ("Elmer Fudd")
          t.priority.value should equal (1)
          success
        } withoutResults error("Did find not Elmer") )
    ) ~~>() verifySuccess
  }
}