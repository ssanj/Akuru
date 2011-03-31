/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru
package dsl

import MongoTypes.MongoSortObject
import MongoObject.mongo

trait DSLTools {

  def orderToSortObject[T <: DomainObject](orders:List[(FieldType[T, _], SortOrder)]): MongoSortObject = {
    MongoSortObject(orders.foldLeft(mongo){ case (mo, (k, v)) => mo.putPrimitiveObject(k.name, v.id) })
  }

  def noSorting: MongoSortObject = MongoSortObject(mongo)

}