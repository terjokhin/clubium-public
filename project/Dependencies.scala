import sbt._

object Dependencies {

  object Telegramium {
    private val version = "7.62.0"

    val core = "io.github.apimorphism" %% "telegramium-core" % version
    val high = "io.github.apimorphism" %% "telegramium-high" % version
  }

  object Log4Cats {
    private val version = "2.3.0"

    val slf4j = "org.typelevel" %% "log4cats-slf4j" % "2.4.0"
  }

  object FS2 {
    private val version = "3.2.10"

    val core = "co.fs2" %% "fs2-core" % version
  }

  object Other {
    val logbackClassic  = "ch.qos.logback" % "logback-classic"     % "1.2.11"
    val sqlite          = "org.xerial"     % "sqlite-jdbc"         % "3.36.0.3"
    val doobie          = "org.tpolecat"  %% "doobie-core"         % "1.0.0-RC2"
    val flyway          = "org.flywaydb"   % "flyway-core"         % "9.0.1"
    val munit           = "org.scalameta" %% "munit"               % "0.7.29" % Test
    val munitCatsEffect = "org.typelevel" %% "munit-cats-effect-3" % "1.0.7"  % Test
  }
}
