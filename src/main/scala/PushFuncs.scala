package akuru

import MongoTypes.MongoObject

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

  def push(col:String, value:MongoObject): MongoObject =  $funcMongo(functionName, col, value)

  def push[T] = anyFunction1[T](functionName)
}