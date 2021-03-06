package akuru

import MongoTypes.MongoUpdateObject
/**
 * { $push : { field : value } }
 * appends value to field, if field is an existing array, otherwise sets field to the array [value] if field is not present.
 * If field is present but is not an array, an error condition is raised.
 */
trait PushFuncs  { this:Funcs =>

  private object PushFuncs {
    val functionName = "$push"
  }

  import PushFuncs._
  import NestedObject._

  /**
   * Allows pushing NestedObjects into an ArrayField.
   * Used for EmbeddedArray and NestedEmbeddedArray.
   */
  def $push[O <: DomainObject, T <: NestedObject : ClassManifest](nested:FieldType[O, Seq[T]], value: => T): MongoUpdateObject[O] = {
    toMongoUpdateObject[O]($funcMongo(functionName, mongo.putMongo(nested.path, nestedObjectToMongo[O, T](value))))
  }

  /**
   * Allows pushing primitive objects into an Array.
   * Used for ArrayField and NestedArrayField.
   */
  def $push[O <: DomainObject, T : ClassManifest : Primitive : ToMongo](af:FieldType[O, Seq[T]], value: => T): MongoUpdateObject[O] =
    toMongoUpdateObject[O](anyFunction1[O, T](functionName, new Field[O, T](af.path) === value))
}

