package shakey.config

import javax.xml.bind.annotation._

/**
 * IB客户端配置
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="ShakeyConfig")
@XmlRootElement(name="shakey")
class ShakeyConfig {
  @XmlElement(name="log_file")
  var logFile:String = _
  @XmlElement(name="ib_account")
  var ibAccount:String = _
  @XmlElement(name="ib_api_host")
  var ibApiHost:String = _
  @XmlElement(name="ib_api_port")
  var ibApiPort:Int = _
}
