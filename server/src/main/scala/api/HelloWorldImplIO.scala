package api

import cats.effect.IO
import com.example.protos.{HelloReply, HelloRequest, HelloWorldFs2Grpc}
import common.Constant
import io.grpc.Metadata

class HelloWorldImplIO extends HelloWorldFs2Grpc[IO, Metadata] {
  override def sayHello(
                         req: HelloRequest,
                         ctx: Metadata
                       ): IO[HelloReply] = {
    //fs2-grpc doesn't propagate  grpc's Context
    // https://github.com/typelevel/fs2-grpc/pull/115
    // https://github.com/typelevel/fs2-grpc/pull/86/commits/25456d44e84fc3e14a9961dc88d6704584724918
    println("****HelloWorldImplIO****" + Constant.JWT_CTX_KEY.get())
    val reply = HelloReply(message =
      "Hello " + req.name + " " + Constant.JWT_CTX_KEY.get()
    )
    IO.pure(reply)
  }
}