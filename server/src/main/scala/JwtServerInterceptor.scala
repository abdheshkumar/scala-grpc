import com.typesafe.scalalogging.LazyLogging
import io.grpc._

class JwtServerInterceptor(val issuer: String)
  extends ServerInterceptor
    with LazyLogging {

  override def interceptCall[ReqT, RespT](
                                           serverCall: ServerCall[ReqT, RespT],
                                           metadata: Metadata,
                                           serverCallHandler: ServerCallHandler[ReqT, RespT]
                                         ): ServerCall.Listener[ReqT] = {
    //  Get token from Metadata
    val token = metadata.get(Constant.JWT_METADATA_KEY)
    println(s"interceptCall token: $token")
    val ctx = Context.current
      //.withValue(Constant.USER_ID_CTX_KEY, s"my-value1-$token")
      .withValue(Constant.JWT_CTX_KEY, s"my-value2-$token")
    //logger.info(s"jwt.getPayload ${jwt.getPayload}")
    Contexts.interceptCall(ctx, serverCall, metadata, serverCallHandler)
  }
}

object JwtServerInterceptor {
  def NOOP_LISTENER[ReqT](): ServerCall.Listener[ReqT] = new ServerCall.Listener[ReqT]() {}
}
