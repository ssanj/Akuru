/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package object akuru extends akuru.AkuruImplicits with akuru.AkuruGlobalTypes

package akuru {

import domain.serial.NestedConversions
import domain.DomainSupport
import domain.DomainTypeSupport


object MongoTypes extends MongoServerTrait with
                            MongoDatabaseTrait with
                            MongoCollectionTrait with
                            MongoObjectTrait with
                            MongoObjectIdTrait with
                            MongoCursorTrait with
                            MongoWriteResultTrait with
                            MongoErrorTrait with
                            MongoFunctions with
                            MongoTools with
                            MongoRegEx with
                            DomainSupport with
                            QueryTypes with
                            SortTypes with
                            UpdateTypes with
                            OperatorTypes with
                            DomainTypeSupport with
                            NestedConversions with
                            AkuruFunctions with
                            AkuruMongoWrapper with
                            SideEffects
}