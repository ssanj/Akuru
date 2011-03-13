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

  def push[O <: DomainObject, T : ClassManifest](f:Field[O, Seq[T]], value: => T): MongoUpdateObject =
    //toMongoUpdateObject(anyFunction1[T](functionName, new FieldValue[T](new Field[T](f.name), value)))
    toMongoUpdateObject(anyFunction1[O, T](functionName, new Field[O, T](f.name) === value))
}