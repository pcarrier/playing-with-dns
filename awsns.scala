import akka.actor.{ActorSystem, Props, Actor}
import java.net.{InetAddress, InetSocketAddress}

object Main extends App {
  import akka.io.{IO, UdpFF}
  import akka.io.UdpFF.Bind
  implicit val system = ActorSystem("AWSNS")
  val server = system.actorOf(Props[Server], "server")
  IO(UdpFF) ! Bind(server, new InetSocketAddress(5354))
}

class Server extends Actor {
  import akka.io.UdpFF.{Send, Received}
  import akka.util.ByteString
  import org.xbill.DNS._
  private val LoopbackIP = InetAddress.getByAddress(Array[Byte](8, 8, 8, 8))
  def receive = {
    case Received(dataByteString, remoteAddress) => {
      val req = new Message(dataByteString.toArray[Byte])
      val rep = new Message {
        getHeader setID req.getHeader.getID
        getHeader setFlag Flags.QR
        getHeader setFlag Flags.AA
        addRecord(req.getQuestion, Section.QUESTION)
      }
      req.getQuestion.getRRsetType match {
        case Type.A => rep addRecord(new ARecord(req.getQuestion.getName, DClass.IN, 0L, LoopbackIP), Section.ANSWER)
        case _ => rep.getHeader setRcode Rcode.NOTIMP
      }
      sender ! Send(ByteString.fromArray(rep.toWire), remoteAddress)
    }
  }
}
