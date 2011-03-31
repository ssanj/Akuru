/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru
package dsl

trait AkuruDSL extends FindDSL with
                       UpdateDSL with
                       ModifyDSL with
                       RemoveDSL with
                       MongoFunctions with
                       DSLTools with
                       Tools with
                       SideEffects