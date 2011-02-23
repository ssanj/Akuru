/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

final class MongoCursorSpec extends CommonSpec {

  import Task._
  import MongoTypes.MongoObject.sort
  "A MongoCursor" should "limit results on finds" in {
    (setup ~~>
      find(priorityField > 1) (_.limit(2)) {tasks: Seq[Task] => tasks.size should equal (2);  success }  ~~>
      find(priorityField > 1) (_.limit(4)) {tasks: Seq[Task] => tasks.size should equal (4);  success }
    ) ~~>() verifySuccess
  }

  it should "sort by the fields supplied" in {
    (setup ~~>
      find(priorityField > 1) (_.orderBy(sort(ownerField, ASC) and sort(priorityField, DSC)).limit(2)) {tasks:Seq[Task] =>
        tasks.size should equal (2)
        tasks(0).name.value should equal ("Polish Ring")
        tasks(1).name.value should equal ("Eat second-breakfast")
        success
      }
    ) ~~>() verifySuccess
  }

  private def setup: FutureConnection = {
    onTestDB ~~> drop[Task] ~~>
        save(Task(name = nameField("fix roof"), priority = priorityField(10), owner = ownerField("Jazzy"))) ~~>
        save(Task(name = nameField("paint lounge"), priority = priorityField(7), owner = ownerField("Leaf"))) ~~>
        save(Task(name = nameField("Polish Ring"), priority = priorityField(5), owner = ownerField("Bilbo"))) ~~>
        save(Task(name = nameField("Take a snooze"), priority = priorityField(3), owner = ownerField("Frodo"))) ~~>
        save(Task(name = nameField("Eat second-breakfast"), priority = priorityField(5), owner = ownerField("Frodo")))
  }
}