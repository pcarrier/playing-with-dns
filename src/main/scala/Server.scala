import java.util.concurrent.Executors

object Server {
  def main(args: Array[String]) {
    val factory = new NioDatagramChannelFactory(Executors.newCachedThreadPool())
    val bootstrap = new ConnectionlessBootstrap(factory)
    bootstrap.setPipelineFactory(new ChannelPipelineFactory() {

    }
  }
}
