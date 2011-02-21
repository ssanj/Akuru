# Akuru


A function-oriented scala driver for [MongoDB](http://www.mongodb.org/).


## Concepts


### How to use Field and FieldValue


All fields stored within MongoDB are defined as Field[T], where T is the type of the field. All fields stored within objects are
stored as FieldValue[T]. There is a 1:1 correspondence between Field and FieldValue.

FieldValue objects contain a Field as well as typed value.

Given:

val nameField = Field[String]("name")

We can instantiate a FieldValue for the above Field as:
val name:FieldValue[String] = nameField("Harry Potter")

The above ensures that you don't apply an Int to a String value. By using Field as the entry point to FieldValues all stored values are
type safe.


### How to create a new domain object
---------------------------------

Let' say we want to create a new domain object to store information about a Task, its priority and who owns it.

 We want to store a name:String, priority:Int and owner:String.

1. Define the Task object. For each field:
  - define a Field[T] (where T is the intended type of the field)
  - define its name as you want it stored in MongoDB. 
  - define the name of the field variable. This will usually end in "Field".
  
  object Task {
    val nameField = Field[String]("name") //says that the nameField will hold a String and has a mongo key of "name"
    val priorityField = Field[Int]("priority") //says that the priorityField will hold an Int and has a mongo key of "priority"
    val ownerField = Field[String]("owner") //says that the ownerField will hold an Int and has a mongo key of "owner"
  }

  
2. Create the Task Class that extends DomainObject. All the fields are of type FieldValue[T].  With the exception of id, all the other 
fields should have corresponding Fields and types to those defined in the Task object.

 case class Task(override val id:FieldValue[MID] = defaultId, 
                 val name:FieldValue[String], 
                 val priority:FieldValue[Int], 
                 val owner:FieldValue[String])
                 


 name:FieldValue[String] <-> nameField:Field[String]
 priority:FieldValue[Int] <-> priorityField:Field[Int]
 owner:FieldValue[String] <-> ownerField[String]
 
 If you mix things up don't worry. If used correctly the compiler will complain about incompatible types.
 

3. Add an implicit conversion from Task to MongoObject within the Task object

    implicit def taskToMongoObject(task:Task): MongoObject =
      putDomainId(task).putPrimitve(task.title).putPrimitive(task.priority).putPrimitive(task.owner)

 The above will be used when converting from a Task to a MongoObject.
 
4. Add an implicit conversion from MongoObject to Task within the Task object

    implicit def mongoToTask(mo:MongoObject): Task =
      Task(id = idField(Some(mo.getId)) name = nameField(mo.getPrimitive(nameField)), priority = priorityField(mo.getPrimitive(priorityField)),
        owner = ownerField(mo.getPrimitive(ownerField)))

 The above will be used when converting from a MongoObject to a Task.        
 
5. Add an implicit object for CollectionName for Task within the Task object

    implicit object TaskCollection extends CollectionName[Task] {
      override val name = "task"
    }
 
 The CollectionName defines the name of the collection used for Task objects.
 
The full DomainObject is given below.
 
case class Task(override val id:FieldValue[MID] = defaultId,
                  val name:FieldValue[String],
                  val priority:FieldValue[Int],
                  val owner:FieldValue[String]) extends DomainObject
 
object Task {
    val nameField = Field[String]("name")
    val priorityField = Field[Int]("priority")
    val ownerField = Field[String]("owner")

    implicit def taskToMongoObject(task:Task): MongoObject =
      putDomainId(task).putPrimitve(task.title).putPrimitive(task.priority).putPrimitive(task.owner)

    implicit def mongoToTask(mo:MongoObject): Task =
      Task(id = idField(Some(mo.getId)) name = nameField(mo.getPrimitive(nameField)), priority = priorityField(mo.getPrimitive(priorityField)),
        owner = ownerField(mo.getPrimitive(ownerField)))

    implicit object TaskCollection extends CollectionName[Task] {
      override val name = "task"
    }
  }
   
