# Maze
Maze is a little game written in Scala using Akka library. The client is written in Python using PyGTK+.

## Running the server

To run the server type:

```bash
cd server
sbt run
```

It will start the server on a port `9999`.

## Running the client

To run the client type:

```bash
cd client
./MazeClient.py
```

It will connect to the localhost server on a port `9999`.

### Controls
* `w` -- up
* `a` -- left
* `s` -- down
* `d`` -- right
