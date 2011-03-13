/**
 * Copyright (c) $today.year Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

import MongoTypes.MongoUpdateObject

/**
 * { $pull : { field : _value } }
 * removes all occurrences of value from field, if field is an array. If field is present but is not an array, an error condition is raised.
 * In addition to matching an exact value you can also use expressions ($pull is special in this way):
 *
 * { $pull : { field : {field2: value} } } removes array elements with field2 matching value
 * { $pull : { field : {$gt: 3} } } removes array elements greater than 3
 * { $pull : { field : {<match-criteria>} } } removes array elements meeting match criteria
 *
 * Because of this feature, to use the embedded doc as a match criteria, you cannot do exact matches on array elements.
 */
trait PullFuncs { this:Funcs =>

  object PullFuncs {
    val functionName = "$pull"
  }

  import PullFuncs._

  def pull(col:String, value:MongoObject): MongoUpdateObject =  toMongoUpdateObject($funcMongo("$pull", col, value))

  def pull(col:String, value:AnyRef): MongoUpdateObject =  toMongoUpdateObject($funcPrimitive("$pull", col, value))

  def pull[O <: DomainObject, T : ClassManifest](fv:FieldValue[O, T]): MongoUpdateObject = toMongoUpdateObject(anyFunction1[O, T](functionName, fv))
}