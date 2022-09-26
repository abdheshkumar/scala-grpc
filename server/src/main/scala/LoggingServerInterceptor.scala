import io.grpc.ForwardingServerCall.SimpleForwardingServerCall
import io.grpc.{Metadata, ServerCall, ServerCallHandler, ServerInterceptor, Status}

class LoggingServerInterceptor extends ServerInterceptor {
  override def interceptCall[ReqT, RespT](call: ServerCall[ReqT, RespT],
                                          headers: Metadata,
                                          next: ServerCallHandler[ReqT, RespT]): ServerCall.Listener[ReqT] = {
    val start = System.currentTimeMillis()
    val decoratedCall: ServerCall[ReqT, RespT] = new SimpleForwardingServerCall[ReqT, RespT](call) {
      override def close(status: Status, trailers: Metadata) {
        super.close(status, trailers)
        val approximateDuration = System.currentTimeMillis() - start
        val delegateCall = delegate()
        val message = s"gRPC CALL service=${delegateCall.getMethodDescriptor.getServiceName}, " +
          s"method=${delegateCall.getMethodDescriptor.getFullMethodName}, " +
          s"grpcType=${delegateCall.getMethodDescriptor.getType}, " +
          s"status=${status.getCode.name}, " +
          s"description=${status.getDescription}, " +
          s"durationMs=$approximateDuration"
        println(message)
      }
    }

    next.startCall(decoratedCall, headers)
  }
}
