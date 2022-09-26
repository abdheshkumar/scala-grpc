import cats.effect.{ExitCode, IO, IOApp, Resource}
import com.example.protos._
import io.grpc.netty.NettyServerBuilder
import io.grpc.{Metadata, ServerServiceDefinition}

import scala.concurrent.{ExecutionContext, Future};

object Server extends IOApp {

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
    helloService
      .flatMap(services => run(services: _*))
      .use(_ => IO.never)
      .as(ExitCode.Success)
}
