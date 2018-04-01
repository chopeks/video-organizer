import db.*
import db.MovieTable.thumbnail
import db.PathsTable.count
import db.PathsTable.path
import db.SchemaVerionsTable.version
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.content.default
import io.ktor.content.resource
import io.ktor.content.static
import io.ktor.features.CORS
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveOrNull
import io.ktor.response.respond
import io.ktor.response.respondRedirect
import io.ktor.routing.Routing
import io.ktor.routing.delete
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import model.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import utils.*
import java.io.File
import java.sql.Connection
import java.text.DateFormat
import java.time.Duration
import java.util.*
import java.util.Random
import javax.imageio.ImageIO
import kotlin.concurrent.thread


fun Application.module() {
  install(CORS) {
    method(HttpMethod.Options)
    method(HttpMethod.Get)
    method(HttpMethod.Post)
    method(HttpMethod.Delete)
    method(HttpMethod.Put)
    method(HttpMethod.Head)
    method(HttpMethod.Patch)
    anyHost()
    maxAge = Duration.ofDays(365)
  }
  install(ContentNegotiation) {
    gson {
      setDateFormat(DateFormat.LONG)
      setPrettyPrinting()
    }
  }

  install(Routing) {
    static("/") {
      arrayOf("ionicons.eot",
        "ionicons.scss",
        "ionicons.svg",
        "ionicons.ttf",
        "ionicons.woff",
        "ionicons.woff2",
        "noto-sans.scss",
        "noto-sans-bold.ttf",
        "noto-sans-bold.woff",
        "noto-sans-regular.ttf",
        "noto-sans-regular.woff",
        "roboto.scss",
        "roboto-bold.ttf",
        "roboto-bold.woff",
        "roboto-bold.woff2",
        "roboto-light.ttf",
        "roboto-light.woff",
        "roboto-light.woff2",
        "roboto-medium.ttf",
        "roboto-medium.woff",
        "roboto-medium.woff2",
        "roboto-regular.ttf",
        "roboto-regular.woff",
        "roboto-regular.woff2"
      ).forEach { resource("assets/fonts/$it") }
      arrayOf(
        "add_new_landscape.svg",
        "add_new_portrait.svg",
        "picture.svg"
      ).forEach { resource("assets/imgs/$it") }

      resource("build/0.js")
      resource("build/1.js")
      resource("build/2.js")
      resource("build/main.js")
      resource("build/main.css")
      resource("build/polyfills.js")
      resource("build/sw-toolbox.js")
      resource("build/vendor.js")
      resource("index.html")
      resource("manifest.json")
      resource("service-worker.js")
      default("index.html")

    }
    get("/") {
      call.respondRedirect("/index.html", true)
    }
    // option lists
    get("/categories") { call.respond(transaction { Category.all().sortedBy { it.name }.map { it.pojo } }) }
    get("/actors") { call.respond(transaction { Actor.all().sortedBy { it.name }.map { it.pojo } }) }
    post("/categories/{category}/{movie}") {
      val row = transaction {
        MovieCategories.select { (MovieCategories.movie eq call.parameters["movie"]) and (MovieCategories.category eq call.parameters["category"]) }
          .firstOrNull()
      }
      if (row != null) {
        call.respond(HttpStatusCode.Conflict)
      } else {
        transaction {
          MovieCategories.insert {
            it[movie] = call.parameters["movie"]!!.toInt()
            it[category] = call.parameters["category"]!!.toInt()
          }
        }
        call.respond("{}")
      }
    }
    get("/delete/categories/{category}/{movie}") {
      transaction {
        MovieCategories.deleteWhere { (MovieCategories.movie eq call.parameters["movie"]) and (MovieCategories.category eq call.parameters["category"]) }
      }
      call.respond("{}")
    }
    post("/actors/{actor}/{movie}") {
      val row = transaction {
        MovieActors.select { (MovieActors.movie eq call.parameters["movie"]) and (MovieActors.actor eq call.parameters["actor"]) }
          .firstOrNull()
      }
      if (row != null) {
        call.respond(HttpStatusCode.Conflict)
      } else {
        transaction {
          MovieActors.insert {
            it[movie] = call.parameters["movie"]!!.toInt()
            it[actor] = call.parameters["actor"]!!.toInt()
          }
        }
        call.respond("{}")
      }
    }
    get("/delete/actors/{actor}/{movie}") {
      transaction {
        MovieActors.deleteWhere { (MovieActors.movie eq call.parameters["movie"]) and (MovieActors.actor eq call.parameters["actor"]) }
      }
      call.respond("{}")
    }
    //region categories crud
    post("/category") {
      call.receiveOrNull<CategoryPojo>()?.let {
        call.respond(HttpStatusCode.OK, transaction {
          if (Category.find { CategoryTable.id eq it.id }.firstOrNull() != null) {
            CategoryTable.update({ CategoryTable.id eq it.id }) { obj ->
              obj[name] = it.name
              obj[image] = it.image
            }
          } else {
            CategoryTable.insert { new ->
              new[name] = it.name
              new[image] = it.image
            }
          }
          "{}"
        })
      }
    }
    delete("/category/{id}") {
      transaction { CategoryTable.deleteWhere { CategoryTable.id eq call.parameters["id"] } }
      call.respond(HttpStatusCode.OK)
    }
    //endregion
    //region actor crud
    get("/actor/{id}") {
      call.respond(transaction { ActorTable.select { ActorTable.id eq call.parameters["id"] } }.firstOrNull()
        ?: HttpStatusCode.NotFound)
    }
    post("/actor") {
      call.receiveOrNull<ActorPojo>()?.let {
        if (it.image?.startsWith("http") == true) {
          it.image = it.image?.urlImageToBase64()
        }
        call.respond(HttpStatusCode.OK, transaction {
          if (Actor.find { ActorTable.id eq it.id }.firstOrNull() != null) {
            ActorTable.update({ ActorTable.id eq it.id }) { obj ->
              obj[name] = it.name
              obj[image] = it.image
            }
          } else {
            ActorTable.insert { new ->
              new[name] = it.name
              new[image] = it.image
            }
          }
          "{}"
        })
      }
    }
    delete("/actor/{id}") {
      transaction { ActorTable.deleteWhere { ActorTable.id eq call.parameters["id"] } }
      call.respond(HttpStatusCode.OK)
    }
    //endregion
    //region movie
    get("/movie/play/{id}") {
      transaction {
        MovieTable.select { MovieTable.id eq call.parameters["id"] }.limit(1).firstOrNull().also {
          if (it != null) {
            "${Cache.moviePlayer} \"${it[MovieTable.path]}\"".runCommand(File(it[MovieTable.path]).parentFile)
          }
        }
      }
      call.respond(HttpStatusCode.OK)
    }
    get("/movie/{from}/{count}") {
      call.respond(transaction {
        val categories = call.request.queryParameters["category"]
        val actors = call.request.queryParameters["actor"]

        var columnCategory: Column<*> = MovieTable.id
        val viaCategory = when (categories) {
          null, "null" -> MovieTable.slice(MovieTable.id).selectAll()
          "0" -> MovieTable
            .join(MovieCategories, JoinType.LEFT, MovieTable.id, MovieCategories.movie)
            .slice(MovieTable.id)
            .select { MovieCategories.category.isNull() }
          else -> categories.split(",").let {
            columnCategory = MovieCategories.movie
            MovieCategories
              .slice(MovieCategories.movie)
              .select { MovieCategories.category inList it }
              .groupBy(MovieCategories.movie)
              .having { MovieCategories.movie.count() eq it.size }
          }
        }.alias("q1")

        var columnActor: Column<*> = MovieTable.id
        val viaActor = when (actors) {
          null, "null" -> MovieTable.slice(MovieTable.id).selectAll()
          "0" -> MovieTable
            .join(MovieActors, JoinType.LEFT, MovieTable.id, MovieActors.movie)
            .slice(MovieTable.id)
            .select { MovieActors.actor.isNull() }
          else -> actors.split(",").let {
            columnActor = MovieActors.movie
            MovieActors
              .slice(MovieActors.movie)
              .select { MovieActors.actor inList it }
              .groupBy(MovieActors.movie)
              .having { MovieActors.movie.count() eq it.size }
          }
        }.alias("q2")

        Join(MovieTable)
          .join(viaActor, JoinType.LEFT, MovieTable.id, viaActor[columnActor])
          .join(viaCategory, JoinType.LEFT, MovieTable.id, viaCategory[columnCategory])
          .select { viaActor[columnActor].isNotNull() and viaCategory[columnCategory].isNotNull() }
          .groupBy(MovieTable.id)
          .orderBy(MovieTable.id, false)
          .limit(call.parameters["count"]!!.toInt(), call.parameters["from"]!!.toInt())
          .map { MoviePojo(it[MovieTable.id].value, it[MovieTable.name], it[MovieTable.duration]) }
      })
    }
    get("/movie/{id}") {
      call.respond(transaction {
        mapOf<String, Any>(
          "categories" to MovieCategories.select { MovieCategories.movie eq call.parameters["id"] }
            .map { it[MovieCategories.category].toString() },
          "actors" to MovieActors.select { MovieActors.movie eq call.parameters["id"] }
            .map { it[MovieActors.actor].toString() }
        )
      })
    }
    //endregion
    //region images
    get("/category/image/{id}") {
      call.respond(transaction { arrayOf(Category.findById(call.parameters["id"]!!.toInt())?.image) }
        ?: HttpStatusCode.NotFound)
    }
    get("/actor/image/{id}") {
      call.respond(transaction { arrayOf(Actor.findById(call.parameters["id"]!!.toInt())?.image) }
        ?: HttpStatusCode.NotFound)
    }
    get("/movie/image/{id}") {
      val image = transaction {
        MovieTable.select { MovieTable.id eq call.parameters["id"] }.firstOrNull().also {
          if (it != null) {
            if (it[thumbnail] != null && call.request.queryParameters["refresh"] != "true") {
              return@transaction it[thumbnail]
            } else {
              val tempDir = File(UUID.randomUUID().toString().substring(0..7)).apply { mkdirs() }
              val images = mutableListOf<String>()
              makeScreenshot(tempDir, File(it[MovieTable.path]), (1..999).random().toLong()).also { img ->
                ImageIO.read(img.readBytes().inputStream())
                  .normalizeImage()
                  .let { ImageIO.write(it, "jpg", img) }
                transaction {
                  MovieTable.update({ MovieTable.id eq call.parameters["id"] }, body = {
                    it[thumbnail] = "data:image/jpg;base64," + String(Base64.getMimeEncoder().encode(img.readBytes()))
                  })
                }
                images.add("data:image/jpg;base64," + String(Base64.getMimeEncoder().encode(img.readBytes())))
                img.delete()
              }
              tempDir.delete()
              return@transaction images[0]
            }
          }
        }
      }
      call.respond(arrayOf(image))
    }
    get("/movie/images/{id}") {
      val images = transaction {
        MovieTable.select { MovieTable.id eq call.parameters["id"] }.firstOrNull().also {
          if (it != null) {
            val tempDir = File(UUID.randomUUID().toString().substring(0..7)).apply { mkdirs() }
            val images = mutableListOf<String>()
            makeScreenshots(tempDir, File(it[MovieTable.path])).forEach {
              images.add("data:image/jpg;base64," + String(Base64.getMimeEncoder().encode(it.readBytes())))
              it.delete()
            }
            tempDir.delete()
            return@transaction images
          }
        }
      }
      call.respond(images ?: HttpStatusCode.NotFound)
    }
    //endregion

    //region directories
    get("/directories") {
      call.respond(transaction {
        PathsTable.selectAll().map { PathPojo(it[path], it[count]) }
      })
    }
    post("/directory") {
      call.receiveOrNull<PathPojo>()?.let { json ->
        transaction {
          try {
            PathsTable.insert {
              it[path] = json.path
              it[count] = getFiles(File(json.path)).size
            }
          } catch (e: Throwable) {
          }
        }
        call.respond("{}")
      }
      thread { RefreshUtils.refresh() }
    }
    post("/directory/remove") {
      call.receiveOrNull<PathPojo>()?.let { json ->
        transaction {
          PathsTable.deleteWhere { PathsTable.path eq json.path }
          MovieTable.deleteWhere { MovieTable.path like "${json.path}%" }
        }
      }
      call.respond("{}")
    }
    //endregion
    //region settings
    get("/settings") {
      call.respond(HttpStatusCode.OK, Cache.settings)
    }
    post("/settings") {
      call.receiveOrNull<SettingsPojo>()?.let {
        Cache.settings = it
      }
      call.respond(HttpStatusCode.OK, Cache.settings)
    }
    //endregion

    "${Cache.browser} http://localhost:8080".runCommand(File("./"))
//    thread {
//      javafx.application.Application.launch(Window::class.java)
//    }
  }
}

val videoExtensions = arrayOf("264", "3g2", "3gp", "3gp2", "3gpp", "3gpp2", "3mm", "3p2", "60d", "787", "890", "aaf", "aec", "aecap", "aegraphic", "aep", "aepx", "aet", "aetx", "ajp", "ale", "am", "amc", "amv", "amx", "anim", "anx", "aqt", "arcut", "arf", "asf", "asx", "avb", "avc", "avchd", "avd", "avi", "avm", "avp", "avs", "avs", "avv", "awlive", "axm", "axv", "bdm", "bdmv", "bdt2", "bdt3", "bik", "bin", "bix", "bmc", "bmk", "bnp", "box", "bs4", "bsf", "bu", "bvr", "byu", "camproj", "camrec", "camv", "ced", "cel", "cine", "cip", "clk", "clpi", "cmmp", "cmmtpl", "cmproj", "cmrec", "cmv", "cpi", "cpvc", "crec", "cst", "cvc", "cx3", "d2v", "d3v", "dad", "dash", "dat", "dav", "db2", "dce", "dck", "dcr", "dcr", "ddat", "dif", "dir", "divx", "dlx", "dmb", "dmsd", "dmsd3d", "dmsm", "dmsm3d", "dmss", "dmx", "dnc", "dpa", "dpg", "dream", "dsy", "dv", "dv-avi", "dv4", "dvdmedia", "dvr", "dvr-ms", "dvx", "dxr", "dzm", "dzp", "dzt", "edl", "evo", "evo", "exo", "eye", "eyetv", "ezt", "f4f", "f4m", "f4p", "f4v", "fbr", "fbr", "fbz", "fcarch", "fcp", "fcproject", "ffd", "ffm", "flc", "flh", "fli", "flic", "flv", "flx", "fpdx", "ftc", "fvt", "g2m", "g64", "g64x", "gcs", "gfp", "gifv", "gl", "gom", "grasp", "gts", "gvi", "gvp", "gxf", "h264", "hdmov", "hdv", "hkm", "ifo", "imovielibrary", "imoviemobile", "imovieproj", "imovieproject", "inp", "int", "ircp", "irf", "ism", "ismc", "ismclip", "ismv", "iva", "ivf", "ivr", "ivs", "izz", "izzy", "jdr", "jmv", "jnr", "jss", "jts", "jtv", "k3g", "kdenlive", "kmv", "ktn", "lrec", "lrv", "lsf", "lsx", "lvix", "m15", "m1pg", "m1v", "m21", "m21", "m2a", "m2p", "m2t", "m2ts", "m2v", "m4e", "m4u", "m4v", "m75", "mani", "meta", "mgv", "mj2", "mjp", "mjpeg", "mjpg", "mk3d", "mkv", "mmv", "mnv", "mob", "mod", "modd", "moff", "moi", "moov", "mov", "movie", "mp21", "mp21", "mp2v", "mp4", "mp4infovid", "mp4v", "mpe", "mpeg", "mpeg1", "mpeg2", "mpeg4", "mpf", "mpg", "mpg2", "mpg4", "mpgindex", "mpl", "mpl", "mpls", "mproj", "mpsub", "mpv", "mpv2", "mqv", "msdvd", "mse", "msh", "mswmm", "mt2s", "mts", "mtv", "mvb", "mvc", "mvd", "mve", "mvex", "mvp", "mvp", "mvy", "mxf", "mxv", "mys", "n3r", "ncor", "nfv", "nsv", "ntp", "nut", "nuv", "nvc", "ogm", "ogv", "ogx", "orv", "osp", "otrkey", "pac", "par", "pds", "pgi", "photoshow", "piv", "pjs", "playlist", "plproj", "pmf", "pmv", "pns", "ppj", "prel", "pro", "pro4dvd", "pro5dvd", "proqc", "prproj", "prtl", "psb", "psh", "pssd", "psv", "pva", "pvr", "pxv", "pz", "qt", "qtch", "qtindex", "qtl", "qtm", "qtz", "r3d", "rcd", "rcproject", "rcrec", "rcut", "rdb", "rec", "rm", "rmd", "rmd", "rmp", "rms", "rmv", "rmvb", "roq", "rp", "rsx", "rts", "rts", "rum", "rv", "rvid", "rvl", "san", "sbk", "sbt", "sbz", "scc", "scm", "scm", "scn", "screenflow", "sdv", "sec", "sec", "sedprj", "seq", "sfd", "sfera", "sfvidcap", "siv", "smi", "smi", "smil", "smk", "sml", "smv", "snagproj", "spl", "sqz", "srt", "ssf", "ssm", "stl", "str", "stx", "svi", "swf", "swi", "swt", "tda3mt", "tdt", "tdx", "theater", "thp", "tid", "tivo", "tix", "tod", "tp", "tp0", "tpd", "tpr", "trec", "trp", "ts", "tsp", "ttxt", "tvlayer", "tvrecording", "tvs", "tvshow", "usf", "usm", "v264", "vbc", "vc1", "vcpf", "vcr", "vcv", "vdo", "vdr", "vdx", "veg", "vem", "vep", "vf", "vft", "vfw", "vfz", "vgz", "vid", "video", "viewlet", "viv", "vivo", "vix", "vlab", "vmlf", "vmlt", "vob", "vp3", "vp6", "vp7", "vpj", "vr", "vro", "vs4", "vse", "vsp", "vtt", "w32", "wcp", "webm", "wfsp", "wgi", "wlmp", "wm", "wmd", "wmmp", "wmv", "wmx", "wot", "wp3", "wpl", "wsve", "wtv", "wve", "wvm", "wvx", "wxp", "xej", "xel", "xesc", "xfl", "xlmv", "xml", "xmv", "xvid", "y4m", "yog", "yuv", "zeg", "zm1", "zm2", "zm3", "zmv")

fun getFiles(dir: File) = dir.walkTopDown()
  .filter { it.isFile }
  .filter { it.extension in videoExtensions }
  .toCollection(mutableListOf())

fun ClosedRange<Int>.random() =
  Random().nextInt(endInclusive - start) + start

fun main(args: Array<String>) {
  Database.connect("jdbc:sqlite:movies.sqlite", driver = "org.sqlite.JDBC")
  TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_READ_UNCOMMITTED
  transaction {
    SchemaUtils.create(SchemaVerionsTable)
    if (SchemaVerionsTable.selectAll().count() == 0) {
      SchemaVerionsTable.insert {
        it[version] = 1
      }
    }
    loop@ while (true) {
      when (SchemaVerionsTable.selectAll().first()[version]) {
        1 -> {
          SchemaUtils.create(CategoryTable, ActorTable, MovieTable, MovieCategories, MovieActors, PathsTable)
          SchemaVerionsTable.inc()
        }
        2 -> { // add file count for each path
          SchemaUtils.createMissingTablesAndColumns(PathsTable)
          SchemaVerionsTable.inc()
        }
        else -> break@loop
      }
    }
  }
  // delete files that were removed
  transaction {
    MovieTable.selectAll().forEach {
      if (!File(it[MovieTable.path]).exists()) {
        MovieTable.deleteWhere { MovieTable.id eq it[MovieTable.id] }
      }
    }
    PathsTable.selectAll().forEach { table ->
      PathsTable.update({ path eq table[path] }) {
        it[count] = getFiles(File(table[path])).size
      }
    }
  }

  RefreshUtils.refresh()

  embeddedServer(Netty, 8080, module = Application::module)
    .start(wait = true)
}
