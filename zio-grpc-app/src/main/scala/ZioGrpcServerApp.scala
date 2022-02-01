import UserDatabase.{User, UserDatabaseEnv}
import com.example.protos.my.ZioMy._
import com.example.protos.my.{HelloReply, HelloRequest}
import io.grpc.{Metadata, Status}
import scalapb.zio_grpc.{RequestContext, Server, ServerLayer}
import zio.{ExitCode, Has, IO, ZIO, ZLayer, console}

object UserDatabase {
  case class User(name: String)
  type UserDatabaseEnv = Has[UserDatabase.Service]
  trait Service {
    def fetchUser(name: String): IO[Status, User]
  }

  // accessor
  def fetchUser(name: String): ZIO[UserDatabaseEnv, Status, User] =
    ZIO.accessM[UserDatabaseEnv](_.get.fetchUser(name))

  val live = zio.ZLayer.succeed(new Service {
    def fetchUser(name: String): IO[Status, User] =
      IO.succeed(User(name))
  })
}

object GreeterWithDatabase extends RGreeter[UserDatabaseEnv] {
  def sayHello(
      request: HelloRequest
  ): ZIO[UserDatabaseEnv, Status, HelloReply] =
    UserDatabase.fetchUser(request.name).map { user =>
      HelloReply(s"Hello ${user.name}")
    }
}

object ZioGrpcServerApp extends zio.App {
  val USER_KEY = Metadata.Key.of("name", Metadata.ASCII_STRING_MARSHALLER)

  def authenticate(rc: RequestContext) = rc.metadata.get(USER_KEY).flatMap {
    case Some("bon") =>
      IO.fail(Status.PERMISSION_DENIED.withDescription("You are not allowed"))
    case Some(v) => ZIO.effect(println(s"Name: $v")).ignore *> IO.succeed(User(v))
    case None    => IO.fail(Status.UNAUTHENTICATED)
  }

  val serverLayer: ZLayer[UserDatabaseEnv, Throwable, Has[Server.Service]] =
    ServerLayer.fromServiceLayer(
      io.grpc.ServerBuilder.forPort(9000)
    )(GreeterWithDatabase.transformContextM(authenticate).toLayer)

  val ourApp = UserDatabase.live >>> serverLayer

  def run(args: List[String]): zio.URIO[zio.ZEnv, ExitCode] =
    ourApp.build.useForever.exitCode
}
