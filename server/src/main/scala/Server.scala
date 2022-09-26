import api.{GreeterImplFuture, HelloWorldImplIO}
import cats.effect.{ExitCode, IO, IOApp, Resource}
import com.example.protos._
import io.grpc.ServerServiceDefinition
import io.grpc.netty.NettyServerBuilder

import scala.concurrent.ExecutionContext

object Server extends IOApp {

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

  private def run(services: ServerServiceDefinition*) = {
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
