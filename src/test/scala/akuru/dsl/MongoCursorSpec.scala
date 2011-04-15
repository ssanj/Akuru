/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru
package dsl

final class MongoCursorSpec extends AkuruSpecSupport {

  import Config._
  import Task._

  val tasks = Seq(Task(nameField === "fix roof", priorityField === 10, ownerField === "Jazzy"),
                  Task(nameField === "paint lounge", priorityField === 7, ownerField === "Leaf"),
                  Task(nameField === "Polish Ring", priorityField === 5, ownerField === "Bilbo"),
                  Task(nameField === "Take a snooze", priorityField === 3, ownerField === "Frodo"),
                  Task(nameField === "Eat second-breakfast", priorityField === 5, ownerField === "Frodo"))


  "A MongoCursor" should "limit results on finds" in {
    setup {
        +> (find * Task where (priorityField > 1) constrainedBy Limit(2) withResults {
          tasks => tasks.size should equal (2)
          +> (find * Task where (priorityField > 1) constrainedBy Limit(4) withResults {tasks => tasks.size should equal (4);  Empty }
              withoutResults Failure("expected 4 got 0"))
        } withoutResults Failure("expected 2 got 0"))
    }.execute verifySuccess
  }

  it should "sort by the fields supplied" in {
     setup {
        +> (find * Task where (priorityField > 1) constrainedBy (Order(ownerField -> ASC, priorityField -> DSC) and Limit(2))
                withResults {tasks =>
                  tasks.size should equal (2)
                  tasks(0).name.value should equal ("Polish Ring")
                  tasks(1).name.value should equal ("Eat second-breakfast")
                  Empty
                } withoutResults Failure("expected 2 but got 0"))
    }.execute verifySuccess
  }

  private def setup(f: => WorkResult[Unit]): WorkUnit[Task, Unit] = {
    (drop collection Task withResults {
      +> (save * Task withValues tasks withResults { f } withoutResults ( (t, _) => Failure("Could not save task: " + t)))
    })
  }
}