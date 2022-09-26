package api

import com.example.protos.{GreeterGrpc, HelloReply, HelloRequest}
import common.Constant

import scala.concurrent.Future

class GreeterImplFuture extends GreeterGrpc.Greeter {
  override def sayHello(
                         req: HelloRequest /*, ctx: Metadata*/
                       ): Future[HelloReply] = {
    println("***GreeterImplFuture*****" + Constant.JWT_CTX_KEY.get())
    val reply = HelloReply(message =
      "Hello " + req.name + " " + Constant.JWT_CTX_KEY.get()
    )
    Future.successful(reply)
  }
}
