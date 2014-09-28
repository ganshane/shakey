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

  @XmlElement(name = "stocks")
  var stocks: String = _
  @XmlElement(name = "volume_strategy")
  var volumeStrategy: VolumeStrategy = VolumeStrategy.FiveMinute //Day

  @XmlElement(name = "rate_overflow")
  var rateOverflow: Double = 1.618
  @XmlElement(name = "top_percent")
  var topPercent: Double = 0.2
}
