/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

final class MongoCursorSpec extends CommonSpec with AkuruDSL {

  import Task._
  import MongoTypes.MongoObject.sort
  "A MongoCursor" should "limit results on finds" in {
    (setup ~~>
      ( find (Task) where (priorityField > 1) constrainedBy Limit(2) withResults { tasks => tasks.size should equal (2);  success }
              withoutResults error("expected 2 got 0") ) ~~>
      ( find (Task) where (priorityField > 1) constrainedBy Limit(4) withResults {tasks => tasks.size should equal (4);  success }
              withoutResults error("expected 4 got 0") )
    ) ~~>() verifySuccess
  }

  it should "sort by the fields supplied" in {
    (setup ~~>
      ( find (Task) where (priorityField > 1) constrainedBy (Order(ownerField, ASC) + Order(priorityField, DSC) and Limit(2))
              withResults {tasks =>
                tasks.size should equal (2)
                tasks(0).name.value should equal ("Polish Ring")
                tasks(1).name.value should equal ("Eat second-breakfast")
                success
              } withoutResults error("expected 2 but got 0") )
    ) ~~>() verifySuccess
  }

  private def setup: FutureConnection = {
    initTask ~~>
        save(Task(nameField("fix roof"), priorityField(10), ownerField("Jazzy"))) ~~>
        save(Task(nameField("paint lounge"), priorityField(7), ownerField("Leaf"))) ~~>
        save(Task(nameField("Polish Ring"), priorityField(5), ownerField("Bilbo"))) ~~>
        save(Task(nameField("Take a snooze"), priorityField(3), ownerField("Frodo"))) ~~>
        save(Task(nameField("Eat second-breakfast"), priorityField(5), ownerField("Frodo")))
  }
}