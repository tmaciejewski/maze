import akka.actor._
import akka.io.{IO, Tcp}
import akka.util.ByteStringBuilder
import java.net.InetSocketAddress
import java.nio.ByteOrder

import maze._

class Server extends Actor with ActorLogging {
    import context.system

    IO(Tcp) ! Tcp.Bind(self, new InetSocketAddress("127.0.0.1", 9999))

    def receive = {
        case Tcp.Bound(localAddress) => log.info("server started")

        case Tcp.CommandFailed(_: Tcp.Bind) => context stop self

        case Tcp.Connected(remote, local) =>
            log.info("new player connected")
            val connection = sender()
            val handler = context.actorOf(Props(classOf[GameHandler], connection))
    }
}

class GameHandler(connection: ActorRef) extends Actor with ActorLogging {
    var m: Maze = new MazeDFS(20, 20)
    var playerX = 0
    var playerY = 0
    var exitX = 0
    var exitY = 0

    implicit val order = ByteOrder.BIG_ENDIAN

    connection ! Tcp.Register(self)
    initGame
    sendMaze
    sendPlayerPosition

    def receive = {
        case Tcp.Received(data) =>
            processCommand(data(0).toChar)
            sendPlayerPosition
            if (playerX == exitX && playerY == exitY) {
                log.info("player won")
                context stop self
            }
        case Tcp.PeerClosed =>
            log.info("player disconnected")
            context stop self
    }

    private def initGame = {
        m.generate
        playerX = 0
        playerY = 0

        exitX = m.width -1
        val exits = List.range(0, m.height).filter(y => !m.isWall(exitX, y)).reverse
        exitY = exits.head
    }

    private def sendMaze = {
        var builder = new ByteStringBuilder()
        builder.putInt(m.width)
        builder.putInt(m.height)

        List.range(0, m.height) foreach {y =>
            List.range(0, m.width) foreach {x =>
                if (m.isWall(x, y))
                    builder.putByte(1)
                else
                    builder.putByte(0)
            }
        }
        builder.putInt(exitX)
        builder.putInt(exitY)

        connection ! Tcp.Write(builder.result())
    }

    private def sendPlayerPosition = {
        log.info("sending player pos: " + playerX + " " + playerY)
        var builder = new ByteStringBuilder()
        builder.putInt(playerX)
        builder.putInt(playerY)
        connection ! Tcp.Write(builder.result())
    }

    private def processCommand(command: Char) = command match {
        case 'w' => {
            if (playerY > 0 && !m.isWall(playerX, playerY - 1))
                playerY -= 1
        }
        case 's' => {
            if (playerY < m.height - 1 && !m.isWall(playerX, playerY + 1))
                playerY += 1
        }
        case 'a' => {
            if (playerX > 0 && !m.isWall(playerX - 1, playerY))
                playerX -= 1
        }
        case 'd' => {
            if (playerX < m.width - 1 && !m.isWall(playerX + 1, playerY))
                playerX += 1
        }
        case _ => ()
    }
}

object MazeServer {
    def main(args: Array[String]): Unit = {
        val system = ActorSystem("MazeServer")
        val server = system.actorOf(Props[Server], name = "server")
    }
}
