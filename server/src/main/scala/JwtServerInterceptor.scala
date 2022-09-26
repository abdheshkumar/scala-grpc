import com.typesafe.scalalogging.LazyLogging
import common.Constant
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
    println(s"JwtServerInterceptor#interceptCall token: $token")
    val ctx = Context.current
      .withValue(Constant.JWT_CTX_KEY, s"my-value2-$token")
    Contexts.interceptCall(ctx, serverCall, metadata, serverCallHandler)
  }
}

object JwtServerInterceptor {
  def NOOP_LISTENER[ReqT](): ServerCall.Listener[ReqT] =
    new ServerCall.Listener[ReqT]() {}
}
