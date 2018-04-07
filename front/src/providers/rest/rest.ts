import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';

/*
  Generated class for the RESTProvider provider.

  See https://angular.io/guide/dependency-injection for more info on providers
  and Angular DI.
*/
@Injectable()
export class RESTProvider {
  private readonly url: string;
  private version: string;

  constructor(public http: HttpClient) {
    this.url = window.location.href.split(":", 2).join(":") + ":8080"
  }

  githubTag(callback) {
    if (this.version != null) {
      callback(this.version)
    } else {
      this.http.get<{ tag_name: string }>("https://api.github.com/repos/chopeks/video-organizer/releases/latest")
        .subscribe(it => {
          this.version = it.tag_name
          callback(this.version)
        })
    }
  }

  loadArtists() {
    return this.http.get<any[]>(this.url + "/actors")
  }

  loadArtistImage(id) {
    return this.http.get<any[]>(this.url + "/image/actor/" + id)
  }

  saveArtist(artist) {
    return this.http.post<any>(this.url + "/actor", JSON.stringify(artist), {
      headers: {"Content-Type": "application/json"}
    })
  }

  loadCategories() {
    return this.http.get<any[]>(this.url + "/categories")
  }

  loadCategoryImage(id) {
    return this.http.get<any[]>(this.url + "/image/category/" + id)
  }

  saveCategory(category) {
    return this.http.post<any>(this.url + "/category", JSON.stringify(category), {
      headers: {"Content-Type": "application/json"}
    })
  }

  loadMovies(id, count, categories, actors, filter) {
    let catList = categories.map(it => it.toString());
    let artList = actors.map(it => it.toString());

    return this.http.get<{ count: number, movies: any[] }>(this.url + "/movie/" + id + "/" + count, {
      params: {
        category: catList.length == 0 ? null : catList.reduce((acc, value) => acc.concat(",").concat(value)),
        actor: artList.length == 0 ? null : artList.reduce((acc, value) => acc.concat(",").concat(value)),
        filter: String(filter)
      }
    })
  }

  loadMovieDetails(id) {
    return this.http.get<any>(this.url + "/movie/" + id)
  }

  deleteMovie(id: number) {
    return this.http.delete<any>(this.url + "/movie/" + id)
  }


  loadMovieImage(id) {
    return this.http.get<any[]>(this.url + "/image/movie/" + id)
  }

  loadMovieImages(id) {
    return this.http.get<any[]>(this.url + "/images/movie/" + id)
  }

  playMovie(id: number) {
    return this.http.get<any>(this.url + "/movie/play/" + id)
  }

  bindArtist(movie, artist) {
    return this.http.post<any>(this.url + "/actors/" + artist + "/" + movie, "")
  }

  bindCategory(movie, category) {
    return this.http.post<any>(this.url + "/categories/" + category + "/" + movie, "")
  }

  removeArtist(movie, artist) {
    return this.http.delete<any>(this.url + "/actors/" + artist + "/" + movie)
  }

  removeCategory(movie, category) {
    return this.http.delete<any>(this.url + "/categories/" + category + "/" + movie)
  }

  refreshImage(movie) {
    return this.http.get<any[]>(this.url + "/image/movie/" + movie + "?refresh=true")
  }

  loadDirectories() {
    return this.http.get<any[]>(this.url + "/directories")
  }

  saveDirectory(dir) {
    return this.http.post<any[]>(this.url + "/directory", JSON.stringify(dir), {
      headers: {"Content-Type": "application/json"}
    })
  }

  removeDirectory(dir) {
    return this.http.post<any[]>(this.url + "/directory/remove", JSON.stringify(dir), {
      headers: {"Content-Type": "application/json"}
    })
  }

  loadDuplicates() {
    return this.http.get<any[]>(this.url + "/duplicates")
  }

  //region settings
  loadSettings() {
    return this.http.get<{ browser: string, moviePlayer: string }>(this.url + "/settings")
  }

  saveSettings(settings: { browser: string, moviePlayer: string }) {
    return this.http.post<any[]>(this.url + "/settings", JSON.stringify(settings), {
      headers: {"Content-Type": "application/json"}
    })
  }

  //endregion
}
