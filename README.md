# pdf-elm-viewer

Server that displays the files in a directory on one device and from there open the file on the
serving device. 
Run on raspberry pi where mobile device selects sheet music and display the sheets as PDF on the monitor attached to pi.

We use Elm for the mobile device UI and serve from a HTTP4s server. The PDF viewer is just HTML and javascript.

### Compile the elm

`elm make src/main/assets/elm/Main.elm --output src/main/resources/assets/elm.js`

### Ship to pi

`sbt assembly`

`scp target/scala-2.13/pdf-elm-viewer-assembly-0.0.1.jar pi@192.168.178.87:~/Desktop`

### Start on pi

`ssh pi@192.168.178.87`

`cd Desktop`

`java -jar -Dconfig.file=application.conf pdf-elm-viewer-assembly-0.0.1.jar`

#### Notes to self

- Websockets example usage: https://github.com/MartinSnyder/http4s-chatserver/blob/master/src/main/scala/com/martinsnyder/chatserver/ChatRoutes.scala

- sbt-elm: https://github.com/choucrifahed/sbt-elm/blob/master/src/main/scala/sbt/elm/SbtElm.scala

- static: https://github.com/ChristopherDavenport/http4s-static-resource-example/blob/master/src/main/scala/io/chrisdavenport/http4sstaticresourceexample/StaticSite.scala