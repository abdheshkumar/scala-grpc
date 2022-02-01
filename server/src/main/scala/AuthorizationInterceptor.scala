import com.typesafe.scalalogging.LazyLogging
import io.grpc._

class AuthorizationInterceptor extends ServerInterceptor with LazyLogging {

  override def interceptCall[ReqT, RespT](serverCall: ServerCall[ReqT, RespT], metadata: Metadata,
                                          serverCallHandler: ServerCallHandler[ReqT, RespT]) = {
    //  Get token from Metadata
    val token = metadata.get(Constant.JWT_METADATA_KEY)
    logger.info(s"interceptCall token: $token")
    val ctx = Context.current.withValue(Constant.USER_ID_CTX_KEY, s"my-value1-${token}").withValue(Constant.JWT_CTX_KEY, s"my-value2-${token}")
    //logger.info(s"jwt.getPayload ${jwt.getPayload}")
    Contexts.interceptCall(ctx, serverCall, metadata, serverCallHandler)
  }
}

object AuthorizationInterceptor {
  val USERINFO_CONTEXT_KEY: Context.Key[String] = Context.key("user_info")
  val AUTHORIZATION = "Authorization"
  val AUTHORIZATION_METADATA_KEY =
    Metadata.Key.of(AUTHORIZATION, Metadata.ASCII_STRING_MARSHALLER)
}

trait UserService {
  def validate(authToken: String): UserInfo
}

case class UserInfo(name: String, roles: List[String])

object UserServiceImpl extends UserService {
  override def validate(authToken: String): UserInfo = {
    loadUserByAuthToken(authToken)
  }

  private def loadUserByAuthToken(authToken: String): UserInfo = { // Fetch from DB
    if (authToken == "admin_token")
      UserInfo("Rohit", List("ADMIN", "USER"))
    else UserInfo("John", List("USER"))
  }
}
