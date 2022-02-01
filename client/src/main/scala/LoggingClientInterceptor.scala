import io.grpc.ClientCall.Listener
import io.grpc._

class LoggingClientInterceptor extends ClientInterceptor {

  override def interceptCall[ReqT, RespT](method: MethodDescriptor[ReqT, RespT], callOptions: CallOptions, next: Channel): ClientCall[ReqT, RespT] = {
    LoggingClientCall(method, next.newCall(method, callOptions))
  }


}

case class LoggingClientCall[ReqT, RespT](method: MethodDescriptor[ReqT, RespT],
                                          delegateCall: ClientCall[ReqT, RespT]) extends ForwardingClientCall.SimpleForwardingClientCall[ReqT, RespT](delegateCall) {
  override def start(responseListener: Listener[RespT], headers: Metadata) {
    val startedAt = System.currentTimeMillis()
    val listener: Listener[RespT] = LoggingListener(method, responseListener, headers, startedAt)
    delegate().start(listener, headers)
  }
}

case class LoggingListener[RespT, ReqT](methodDescriptor: MethodDescriptor[ReqT, RespT],
                                        responseListener: Listener[RespT],
                                        headers: Metadata, startedAt: Long) extends ForwardingClientCallListener.SimpleForwardingClientCallListener[RespT](responseListener) {
  override def onClose(status: Status, m: Metadata): Unit = {
    delegate().onClose(status, m)

    // Use the metadata in start() instead of the one in onClose().
    // It seems that the original metadata is not passed down to the listener
    val duration = System.currentTimeMillis() - startedAt
    println(s"gRPC Call " +
      s"service=${methodDescriptor.getServiceName}," +
      s"method=${methodDescriptor.getFullMethodName}," +
      s"grpcType=${methodDescriptor.getType}," +
      s"grpcStatus=${status.getCode.name}," +
      s"statusDesc=${status.getDescription}," +
      s"duration=${duration}",
      status.getCause
    )
  }
}