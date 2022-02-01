import cats.effect.{ExitCode, IO, IOApp, Resource}
import com.example.protos.my.{GreeterFs2Grpc, HelloRequest, HelloWorldFs2Grpc}
import io.grpc.netty.NettyChannelBuilder
import io.grpc.{ManagedChannel, Metadata}
import fs2.grpc.syntax.all._

object Client extends IOApp {

  val managedChannelResource: Resource[IO, ManagedChannel] =
    NettyChannelBuilder
      .forAddress("127.0.0.1", 9998)
      .usePlaintext()
      .intercept(new LoggingClientInterceptor)
      .resource[IO]

  def runProgram(
      stub: GreeterFs2Grpc[IO, Metadata],
      helloWorldFs2Grpc: HelloWorldFs2Grpc[IO, Metadata]
  ): IO[Unit] = {
    val metadata = new Metadata()
    metadata.put(
      Metadata.Key.of("header-name", Metadata.ASCII_STRING_MARSHALLER),
      "header value passed from the client"
    )
    metadata.put(
      Metadata.Key.of("jwt", Metadata.ASCII_STRING_MARSHALLER),
      "admin_token"
    )
    stub
      .sayHello(HelloRequest(" name..."), metadata)
      .flatMap(res => IO(println(res)))
      .flatMap(_ =>
        helloWorldFs2Grpc
          .sayHello(HelloRequest(" name..."), metadata)
          .map(println)
      )

  }

  val run: IO[Unit] = managedChannelResource
    .flatMap(ch =>
      GreeterFs2Grpc
        .stubResource[IO](ch)
        .flatMap(client =>
          HelloWorldFs2Grpc
            .stubResource[IO](ch)
            .map(client2 => (client, client2))
        )
    )
    .use(v => runProgram(v._1, v._2))

  override def run(args: List[String]): IO[ExitCode] =
    run.map(_ => ExitCode.Success)
}
