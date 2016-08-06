import scala.util.Random

package maze {

class Maze(width: Int, height: Int) {

    def getWidth = width

    def getHeight = height

    private val totalNodes = width * height
    private var mazeLinks = new Array[Boolean](totalNodes * totalNodes)

    private def addMazeLink(node1: Int, node2: Int) = mazeLinks(node1 + node2 * totalNodes) = true

    def isMazeLink(node1: Int, node2: Int) = mazeLinks(node1 + node2 * totalNodes)

    private def neighbors(node: Int) = {
        val x = node % width
        val y = node / width

        var adj: List[(Int, Int)] = List()

        if (x > 0) adj = (x - 1, y) :: adj
        if (x < width - 1) adj = (x + 1, y) :: adj
        if (y > 0) adj = (x, y - 1) :: adj
        if (y < height - 1) adj = (x, y + 1) :: adj

        adj map (node => node._1 + node._2 * width)
    }

    def generate = {
        var visited = new Array[Boolean](totalNodes)

        def visitNode(node: Int): Unit = {
            visited(node) = true
            val nextNodes = Random.shuffle(neighbors(node))
            nextNodes foreach { nextNode =>
                if (!visited(nextNode)) {
                    addMazeLink(node, nextNode)
                    addMazeLink(nextNode, node)
                    visitNode(nextNode)
                }
            }
        }

        visitNode(0)
    }
}

}
