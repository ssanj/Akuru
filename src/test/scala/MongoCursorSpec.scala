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

//  it should "sort by the fields supplied" in {
//    (setup ~~>
//      find(priorityField > 1) (_.orderBy(sort(ownerField, ASC) and sort(priorityField, DSC)).limit(2)) {tasks:Seq[Task] =>
//        tasks.size should equal (2)
//        tasks(0).name.value should equal ("Polish Ring")
//        tasks(1).name.value should equal ("Eat second-breakfast")
//        success
//      }
//    ) ~~>() verifySuccess
//  }

  private def setup: FutureConnection = {
    onTestDB ~~> drop[Task] ~~>
        save(Task(nameField("fix roof"), priorityField(10), ownerField("Jazzy"))) ~~>
        save(Task(nameField("paint lounge"), priorityField(7), ownerField("Leaf"))) ~~>
        save(Task(nameField("Polish Ring"), priorityField(5), ownerField("Bilbo"))) ~~>
        save(Task(nameField("Take a snooze"), priorityField(3), ownerField("Frodo"))) ~~>
        save(Task(nameField("Eat second-breakfast"), priorityField(5), ownerField("Frodo")))
  }
}