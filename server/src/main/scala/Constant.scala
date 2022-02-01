import io.grpc.{Context, Metadata}
import io.grpc.Metadata.ASCII_STRING_MARSHALLER

object Constant {
  // Add a JWT_METADATA_KEY
  val JWT_METADATA_KEY: Metadata.Key[String] =
    Metadata.Key.of("jwt", ASCII_STRING_MARSHALLER)

  // Add a JWT Context Key
  val JWT_CTX_KEY: Context.Key[String] = Context.key("jwt")

  // Add a JWT Context Key
  val USER_ID_CTX_KEY: Context.Key[String] = Context.key("userId")
}
