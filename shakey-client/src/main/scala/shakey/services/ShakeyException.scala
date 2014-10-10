// Copyright 2014 Jun Tsai. All rights reserved.
// site: http://www.ganshane.com
package shakey.services

import java.io.{PrintWriter, PrintStream}


/**
 * 整个系统的消息异常
 * @author jcai
 */
object ShakeyException{
  def wrap(exception:Throwable, errorCode:ErrorCode,message:String):ShakeyException={
    if (exception.isInstanceOf[ShakeyException]) {
      val me = exception.asInstanceOf[ShakeyException]
      if (errorCode != null && errorCode != me.errorCode) {
        return new ShakeyException(message, exception, errorCode);
      }
      me
    } else {
      new ShakeyException(message, exception, errorCode)
    }
  }
  def wrap(exception:Throwable, errorCode:ErrorCode):ShakeyException={
    wrap(exception,errorCode,exception.getMessage);
  }
  def wrap(exception:Throwable):ShakeyException = wrap(exception, null)
}

class ShakeyException(message:String,cause:Throwable,val errorCode:ErrorCode) extends RuntimeException(message,cause){
  def this(errorCode:ErrorCode)=this(null,null,errorCode)
  def this(message:String, errorCode:ErrorCode)=this(message,null,errorCode)
  def this(cause:Throwable, errorCode:ErrorCode )=this(null,cause,errorCode)
  override def printStackTrace(s:PrintStream){
    s.synchronized{
      printStackTrace(new PrintWriter(s))
    }
  }

  override def printStackTrace(s:PrintWriter) {
    s.synchronized{
      s.println(this)
      getStackTrace.foreach{trace=>
        s.println("\tat " + trace)
      }
      if (cause != null) {
        cause.printStackTrace(s)
      }
      s.flush()
    }
  }

  override def toString={
    val sb  = new StringBuilder
    if(errorCode != null){
      sb.append("shakey-").append(errorCode.code).append(":")
      sb.append(errorCode.toString).append(" ")
    }else{
      sb.append("shakey-0000 UNKNOWN ")
    }
    if(message != null){sb.append(message)}
    if(cause!=null){sb.append(" -> ").append(cause)}

    sb.toString()
  }

}

abstract class ErrorCode(val code:Int)
