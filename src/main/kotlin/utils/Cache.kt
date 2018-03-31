package utils

import model.SettingsPojo
import org.apache.jdbm.DBMaker
import utils.OsCheck.OSType.*
import java.util.*
import java.util.Locale


object Cache {
  var db = DBMaker.openFile("settings")
    .make()!!

  val cacheStringMap: SortedMap<String, String>
    get() = try {
      db.createTreeMap("settings")
    } catch (e: Throwable) {
      db.getTreeMap("settings")
    }

  var moviePlayer: String
    get() = cacheStringMap["moviePlayer"] ?: when (OsCheck.operatingSystemType) {
      Windows -> "explorer"
      MacOS -> "open"
      Linux -> "xdg-open"
      Other -> "sh" // this is clearly wrong
    }
    set(value) {
      cacheStringMap["moviePlayer"] = value
      db.commit()
    }

  var browser: String
    get() = cacheStringMap["browser"] ?: when (OsCheck.operatingSystemType) {
      Windows -> "explorer"
      MacOS -> "open"
      Linux -> "xdg-open"
      Other -> "sh" // this is clearly wrong
    }
    set(value) {
      cacheStringMap["browser"] = value
      db.commit()
    }

  var settings: SettingsPojo
    get() = SettingsPojo(browser, moviePlayer)
    set(value) {
      value.also {
        browser = it.browser
        moviePlayer = it.moviePlayer
      }
    }
}

object OsCheck {
  val operatingSystemType: OSType
    get() {
      val os = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH)
      return if (os.indexOf("mac") >= 0 || os.indexOf("darwin") >= 0) {
        MacOS
      } else if (os.indexOf("win") >= 0) {
        Windows
      } else if (os.indexOf("nux") >= 0) {
        Linux
      } else {
        Other
      }
    }

  enum class OSType {
    Windows, MacOS, Linux, Other
  }
}