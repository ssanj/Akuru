package akuru

import MongoTypes.MongoSortObject
import MongoTypes.MongoObject.mongo

trait SortFuncs {

  def sort[O <: DomainObject, T](fv:Field[O, T], order:SortOrder): MongoSortObject = MongoSortObject(mongo.putPrimitiveObject(fv.name, order.id))

  def noSort: MongoSortObject = MongoSortObject(mongo)
}