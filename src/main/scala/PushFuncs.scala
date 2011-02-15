package akuru

import MongoTypes.MongoObject

trait PushFuncs  { this:Funcs =>

  def push(col:String, value:MongoObject): MongoObject =  $funcMongo("$push", col, value)
}