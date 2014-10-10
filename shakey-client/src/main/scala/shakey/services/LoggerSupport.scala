// Copyright 2014 Jun Tsai. All rights reserved.
// site: http://www.ganshane.com
package shakey.services

import org.slf4j.LoggerFactory

/**
 * logger support
 */
trait LoggerSupport {
  protected val logger = LoggerFactory getLogger getClass
}
