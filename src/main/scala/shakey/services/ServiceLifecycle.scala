// Copyright 2014 Jun Tsai. All rights reserved.
// site: http://www.ganshane.com
package shakey.services

/**
 * service lifecycle
 */
trait ServiceLifecycle {
  /**
   * 启动服务
   */
  def start()

  /**
   * 服务关闭
   */
  def shutdown()
}
