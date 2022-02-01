import _root_.com.example.protos.my.{
  GreeterFs2Grpc,
  GreeterGrpc,
  HelloReply,
  HelloRequest,
  HelloWorldFs2Grpc
}
import cats.effect.{ExitCode, IO, IOApp, Resource}
import io.grpc.netty.NettyServerBuilder

import scala.concurrent.{ExecutionContext, Future}
import io.grpc.{Metadata, ServerServiceDefinition};

object Server extends IOApp {

  class GreeterImplIO extends GreeterFs2Grpc[IO, Metadata] {
    override def sayHello(req: HelloRequest, ctx: Metadata): IO[HelloReply] = {
      println("********" + Constant.JWT_CTX_KEY.get())
      val reply = HelloReply(message =
        "Hello " + req.name + ctx.get(
          Metadata.Key.of("header-name", Metadata.ASCII_STRING_MARSHALLER)
        )
      )
      IO.pure(reply)
    }
  }

  class GreeterImplFuture extends GreeterGrpc.Greeter {
    override def sayHello(
        req: HelloRequest /*, ctx: Metadata*/
    ): Future[HelloReply] = {
      println("***GreeterImplFuture*****" + Constant.JWT_CTX_KEY.get())
      val reply = HelloReply(message = "Hello " + req.name)
      Future.successful(reply)
    }
  }

  class HelloWorldImplIO extends HelloWorldFs2Grpc[IO, Metadata] {
    override def sayHello(
        req: HelloRequest,
        ctx: Metadata
    ): IO[HelloReply] = {
      //fs2-grpc doesn't propagate  grpc's Context
      // https://github.com/typelevel/fs2-grpc/pull/115
      // https://github.com/typelevel/fs2-grpc/pull/86/commits/25456d44e84fc3e14a9961dc88d6704584724918
      println("****HelloWorldImplIO****" + Constant.JWT_CTX_KEY.get())
      val reply = HelloReply(message = "Hello " + req.name)
      IO.pure(reply)
    }
  }

  val helloService: Resource[IO, List[ServerServiceDefinition]] =
    HelloWorldFs2Grpc
      .bindServiceResource(new HelloWorldImplIO)
      .map(service =>
        List(
          GreeterGrpc.bindService(
            new GreeterImplFuture(),
            ExecutionContext.Implicits.global
          ),
          service
        )
      )

  // val helloService: Resource[IO, ServerServiceDefinition] = GreeterFs2Grpc.bindServiceResource(new GreeterImplIO())

  def run(services: ServerServiceDefinition*) = {
    Resource.make {
      IO {
        val builder = NettyServerBuilder
          .forPort(9998)
          .intercept(new LoggingServerInterceptor)
          .intercept(new JwtServerInterceptor("as"))

        services
          .foldLeft(builder)((s, se) => s.addService(se))
          .build()
          .start()
      }
    } { server =>
      IO {
        server.shutdown()
        server.awaitTermination()
      }
    }
  }

  override def run(args: List[String]): IO[ExitCode] =
    helloService.flatMap(services => run(services: _*)).use(_ => IO.never).map {
      _ => ExitCode.Success
    }
}
