package akuru

import MongoTypes.MongoSortObject
import MongoTypes.MongoObject.mongo

trait SortFuncs {

  def noSort: MongoSortObject = MongoSortObject(mongo)
}