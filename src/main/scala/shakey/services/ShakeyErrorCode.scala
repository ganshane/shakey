package shakey.services

/**
 * Created by jcai on 14-9-25.
 */
object ShakeyErrorCode {

  object FAIL_PARSE_XML extends ErrorCode(1001)

  object FAIL_TO_GET_LOCAL_KV_SNAPSHOT extends ErrorCode(1002)

  object VERSION_EXISTS_IN_KV extends ErrorCode(1003)

  object INVALID_LOCAL_VERSION extends ErrorCode(1004)

}
