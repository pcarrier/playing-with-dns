import io.netty.bootstrap.Bootstrap
import io.netty.buffer.Unpooled
import io.netty.channel.socket.nio.NioDatagramChannel
import io.netty.channel.{ChannelHandlerContext, ChannelInboundMessageHandlerAdapter}
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.util.internal.logging.{Log4JLoggerFactory, InternalLoggerFactory}
import io.netty.channel.socket.DatagramPacket
import java.net.InetAddress
import org.apache.log4j.BasicConfigurator
import org.xbill.DNS._

object Server {
  val LoopbackIP = InetAddress.getByAddress(Array[Byte](8, 8, 8, 8))

  def main(args: Array[String]): Unit = {
    BasicConfigurator.configure
    InternalLoggerFactory setDefaultFactory new Log4JLoggerFactory

    val bootstrap = new Bootstrap
    bootstrap.group(new NioEventLoopGroup).channel(classOf[NioDatagramChannel])

    bootstrap handler new ChannelInboundMessageHandlerAdapter[DatagramPacket] {
      override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace()
        ctx.channel.close
      }

      def messageReceived(ctx: ChannelHandlerContext, msg: DatagramPacket) {
        val payload = new Array[Byte](msg.data.readableBytes)
        msg.data.readBytes(payload)
        val req = new Message(payload)
        val rep = new Message
        rep.getHeader setID req.getHeader.getID
        rep.getHeader setFlag Flags.QR
        rep addRecord(req.getQuestion, Section.QUESTION)
        rep.getQuestion.getRRsetType match {
          case Type.A => {
            rep.getHeader setFlag Flags.AA
            rep addRecord(new ARecord(req.getQuestion.getName, DClass.IN, 0L, LoopbackIP), Section.ANSWER)
          }
          case _ => rep.getHeader setRcode Rcode.NOTIMP
        }
        ctx.write(new DatagramPacket(Unpooled.copiedBuffer(rep.toWire), msg.remoteAddress()))
      }
    }
    bootstrap bind 53
  }
}
