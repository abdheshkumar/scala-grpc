import io.grpc.inprocess.{InProcessChannelBuilder, InProcessServerBuilder}
import com.example.protos.{GreeterGrpc, HelloReply, HelloRequest}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContext.Implicits
import scala.concurrent.Future

class GreeterSpec extends AnyFlatSpec with Matchers with ScalaFutures {

  class GreeterImp extends GreeterGrpc.Greeter {
    /** Sends a greeting
     */
    override def sayHello(request: HelloRequest): Future[HelloReply] = Future.successful(
      HelloReply.of("Hello, response")
    )
  }

  val serverName = InProcessServerBuilder.generateName()
  val channel = InProcessChannelBuilder.forName(serverName).build()
  val server = InProcessServerBuilder.forName(serverName)
    .addService(GreeterGrpc.bindService(new GreeterImp(), Implicits.global))
    .build()
    .start()

  val client = GreeterGrpc.stub(channel)

  "Service-client communication" should "run" in {
    whenReady(client.sayHello(HelloRequest.of("Hello, Request"))) {
      response =>
        response.message shouldBe "Hello, response"
    }
  }
}
