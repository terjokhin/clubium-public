import Dependencies._

def dockerNamespace: String = "ghcr.io/terjokhin"

// Docker tags can't contain '+' symbols
ThisBuild / dynverSeparator := "-"

lazy val root = (project in file("."))
  .enablePlugins(sbtdocker.DockerPlugin, JavaAppPackaging)
  .settings(
    name         := "clubium-public",
    scalaVersion := "3.1.2",
    libraryDependencies ++= Seq(
      Telegramium.core,
      Telegramium.high,
      Log4Cats.slf4j,
      FS2.core,
      Other.logbackClassic,
      Other.doobie,
      Other.flyway,
      Other.sqlite,
      Other.munit,
      Other.munitCatsEffect
    )
  )
  .settings(
    docker / imageNames := Seq(
      // Sets the latest tag
      ImageName(s"${dockerNamespace}/${name.value}:latest"),
      // Sets a name with a tag that contains the project version
      ImageName(
        namespace = Some(dockerNamespace),
        repository = name.value,
        tag = Some("v" + version.value)
      )
    ),
    docker / dockerfile := {
      val appDir: File = stage.value
      val targetDir    = "/app"
      new Dockerfile {
        // eclipse-temurin:11 tag is the eclipse-temurin:11-jdk-jammy alias
        from("eclipse-temurin:11-jre-jammy")
        workDir(targetDir)
        entryPoint(s"$targetDir/bin/${executableScriptName.value}")
        copy(appDir, targetDir, chown = "daemon:daemon")
      }
    }
  )
