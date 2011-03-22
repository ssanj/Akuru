package akuru

import MongoTypes.MongoUpdateObject
import MongoObject.namePath

/**
 * { $$push : { field : value } }
 * appends value to field, if field is an existing array, otherwise sets field to the array [value] if field is not present.
 * If field is present but is not an array, an error condition is raised.
 */
trait PushFuncs  { this:Funcs =>

  private object PushFuncs {
    val functionName = "$push"
  }

  import PushFuncs._

  /**
   * Allows pushing NestedObjects into a NestedArrayField.
   */
  def $push[O <: DomainObject, T <: NestedObject : ClassManifest : NestedToMongo](nested:NestedArrayField[O, T], value: => T): MongoUpdateObject[O] =
    push[O, T](nested.path, implicitly[NestedToMongo[T]].apply(value))

  /**
   * Allows pushing NestedObjects into an ArrayField.
   */
  def $push[O <: DomainObject, T <: NestedObject : ClassManifest : NestedToMongo](nested:ArrayField[O, T], value: => T): MongoUpdateObject[O] =
    push[O, T](nested.path, implicitly[NestedToMongo[T]].apply(value))


  /**
   * Allows pushing primitive objects into an Array.
   */
  def $push[O <: DomainObject, T : ClassManifest](flat:ArrayField[O, T], value: => T): MongoUpdateObject[O] =
      toMongoUpdateObject[O](anyFunction1[O, T](functionName, new Field[O, T](flat.name) === value))

  private def push[O <: DomainObject, T <: NestedObject : ClassManifest](path:String, value: => MongoObject): MongoUpdateObject[O] = {
    toMongoUpdateObject[O]($funcMongo(functionName, mongo.putMongo(path, value)))
  }
}

