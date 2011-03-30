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

  /**
   * Allows pushing NestedObjects into an ArrayField.
   * Used for EmbeddedArray and NestedEmbeddedArray.
   */
  def $push[O <: DomainObject, T <: NestedObject : ClassManifest : NestedToMongo](nested:FieldType[O, Seq[T]], value: => T):
    MongoUpdateObject[O] = toMongoUpdateObject[O]($funcMongo(functionName, mongo.putMongo(nested.path, implicitly[NestedToMongo[T]].apply(value))))

  /**
   * Allows pushing primitive objects into an Array.
   * Used for ArrayField and NestedArrayField.
   */
  def $push[O <: DomainObject, T : ClassManifest : Primitive](af:FieldType[O, Seq[T]], value: => T): MongoUpdateObject[O] =
    toMongoUpdateObject[O](anyFunction1[O, T](functionName, new Field[O, T](af.path)(null) === value))

  private def pushNested[O <: DomainObject, T <: NestedObject : ClassManifest](path:String, value: => MongoObject): MongoUpdateObject[O] = {
    toMongoUpdateObject[O]($funcMongo(functionName, mongo.putMongo(path, value)))
  }
}

