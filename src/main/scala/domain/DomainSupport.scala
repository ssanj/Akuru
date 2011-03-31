/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru
package domain

import akuru.Tools

trait DomainSupport extends
          DomainTemplateFieldSupport with
          DomainTemplateSupport with
          OwnerFieldSupport with
          DomainFuncsSupport
