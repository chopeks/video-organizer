import {Component, ViewChild} from '@angular/core';
import {ModalController, NavController, NavParams, PopoverController, Select} from 'ionic-angular';
import {RESTProvider} from "../../providers/rest/rest";
import {DomSanitizer, SafeUrl} from "@angular/platform-browser";
import {ActorsPage} from "../actors/actors";
import {CategoriesPage} from "../categories/categories";
import {debugMode} from "../../app/main";
import {SelectorDialog} from "./selector-dialog/selector-dialog";
import {MoviePopover} from "./movie-popover/movie-popover";

@Component({
  selector: 'page-grid',
  templateUrl: 'grid.html'
})
export class GridPage {
  gridCapacity = 15;
  currentId: any;
  icons: string[];
  categories: Array<{ id: any, name: string }> = [];
  artists: Array<{ id: any, name: string }> = [];
  items: Array<{
    movieId: any,
    title: string,
    duration: string,
    icon: SafeUrl,
    artists: Array<{ id: any, name: string }>,
    categories: Array<{ id: any, name: string }>
  }>;
  selectedArtists: Array<any> = [];
  selectedGenres: Array<any> = [];
  selectedFilter = 0;
  lastSelectedFilter = 0;

  interceptKey = true;

  //pagination
  currentPage: number;
  pageCount: number;

  @ViewChild("filterArtists") filterArtists: Select;
  @ViewChild("filterCategories") filterCategories: Select;

  constructor(
    public navCtrl: NavController,
    public navParams: NavParams,
    public rest: RESTProvider,
    public modalCtrl: ModalController,
    public popoverCtrl: PopoverController,
    private sanitizer: DomSanitizer
  ) {

  }

  static pad(number) {
    let s = String(number);
    while (s.length < (2)) {
      s = "0" + s;
    }
    return s;
  }

  static msToTime(s) {
    let ms = s % 1000;
    s = (s - ms) / 1000;
    let secs = s % 60;
    s = (s - secs) / 60;
    let mins = s % 60;
    let hrs = (s - mins) / 60;
    return hrs + ':' + GridPage.pad(mins) + ':' + GridPage.pad(secs);
  }

  ionViewWillEnter() {
    if (this.navParams.get('actor') != null) {
      this.selectedArtists.push(this.navParams.get('actor').id);
      this.filterArtists.setValue([this.navParams.get('actor').id])
    }
    if (this.navParams.get('category') != null) {
      this.selectedGenres.push(this.navParams.get('category').id);
      this.filterCategories.setValue([this.navParams.get('category').id])
    }
    // If we navigated to this page, we will have an item available as a nav param
    this.currentId = this.navParams.get('id');
    if (this.currentId == null) {
      this.currentId = 0
    }
    this.rest.loadCategories()
      .subscribe(categories => {
        categories.forEach(it => this.categories.push({
          id: it.id,
          name: it.name
        }))
      });
    this.rest.loadArtists()
      .subscribe(artists => {
        artists.forEach(it => this.artists.push({
          id: it.id,
          name: it.name
        }));
      });
    this.next(null, this.currentId);
  }

  changePage(event, pages) {
    let newPage = this.currentPage + pages;
    if (newPage > this.pageCount) {
      newPage = this.pageCount
    } else if (newPage < 1) {
      newPage = 1
    }
    if (newPage != this.currentPage) {
      this.next(event, (newPage - 1) * this.gridCapacity)
    }
  }

  next(event, id) {
    this.currentId = id;
    if (this.currentId < 0) {
      this.currentId = 0
    }
    this.items = [];
    this.rest.loadMovies(this.currentId, this.gridCapacity, this.selectedGenres, this.selectedArtists, this.selectedFilter)
      .subscribe(data => {
        this.pageCount = Math.ceil(data.count / this.gridCapacity);
        this.currentPage = Math.ceil(this.currentId / this.gridCapacity) + 1;
        data.movies.forEach(it => {
          this.items.push({
            movieId: it.id,
            title: it.name,
            duration: GridPage.msToTime(it.duration),
            icon: "assets/imgs/picture.svg",
            artists: [],
            categories: []
          });
          if (!debugMode) {
            setTimeout(() => {
              this.rest.loadMovieImage(it.id)
                .subscribe(image => {
                  this.items.find(value => value.movieId == it.id).icon = this.sanitizer.bypassSecurityTrustUrl(image[0])
                });
            }, 1);
          }
          this.rest.loadMovieDetails(it.id)
            .subscribe(details => {
              let item = this.items.find(value => value.movieId == it.id);
              item.categories = details.categories.map(id => this.categories.find(value => value.id == id));
              item.artists = details.actors.map(id => this.artists.find(value => value.id == id));
            })
        });

        this.filterArtists.registerOnChange(event => {
          this.selectedArtists = event;
          this.next(event, 0)
        });
        this.filterCategories.registerOnChange(event => {
          this.selectedGenres = event;
          this.next(event, 0)
        });
      })
  }

  itemTapped(event, item) {
    this.rest.playMovie(item.movieId)
      .subscribe()
  }

  onKey(event) {
    if (this.interceptKey) {
      if (event.altKey == true) {
        switch (event.code) {
          case "Digit1":
            this.navCtrl.setRoot(ActorsPage);
            break;
          case "Digit2":
            this.navCtrl.setRoot(CategoriesPage);
            break;
        }
      } else {
        if (event.code == "ArrowRight") {
          if (this.currentPage < this.pageCount) {
            this.next(event, this.currentId + this.gridCapacity)
          }
        } else if (event.code == "ArrowLeft") {
          if (this.currentPage > 1) {
            this.next(event, this.currentId - this.gridCapacity)
          }
        } else {
          if (event.code.startsWith("Digit") || event.code.startsWith("Key")) {
            switch (event.code) {
              case "Digit1":
                this.itemTapped(event, this.items[0]);
                break;
              case "Digit2":
                this.itemTapped(event, this.items[1]);
                break;
              case "Digit3":
                this.itemTapped(event, this.items[2]);
                break;
              case "Digit4":
                this.itemTapped(event, this.items[3]);
                break;
              case "Digit5":
                this.itemTapped(event, this.items[4]);
                break;
              case "KeyQ":
                this.itemTapped(event, this.items[5]);
                break;
              case "KeyW":
                this.itemTapped(event, this.items[6]);
                break;
              case "KeyE":
                this.itemTapped(event, this.items[7]);
                break;
              case "KeyR":
                this.itemTapped(event, this.items[8]);
                break;
              case "KeyT":
                this.itemTapped(event, this.items[9]);
                break;
              case "KeyA":
                this.itemTapped(event, this.items[10]);
                break;
              case "KeyS":
                this.itemTapped(event, this.items[11]);
                break;
              case "KeyD":
                this.itemTapped(event, this.items[12]);
                break;
              case "KeyF":
                this.itemTapped(event, this.items[13]);
                break;
              case "KeyG":
                this.itemTapped(event, this.items[14]);
                break;
            }
          }
        }
      }
    }
  }

  openCategories(event, item) {
    let modal = this.modalCtrl.create(SelectorDialog, {
      data: this.categories,
      selected: item.categories.slice(),
      type: 'categories'
    }, {showBackdrop: true, enableBackdropDismiss: false, cssClass: "big-modal"});
    modal.onDidDismiss(it => {
      this.interceptKey = true;
      this.categories = it.items;
      it.selected.forEach(sel => {
        this.rest.bindCategory(item.movieId, sel.id)
          .subscribe(response => {
            let movie = this.items.find(value => value.movieId == item.movieId);
            movie.categories.push(this.categories.find(value => value.id == sel.id));
          })
      });

    });
    this.interceptKey = false;
    modal.present();
  }

  openArtists(event, item) {
    let modal = this.modalCtrl.create(SelectorDialog, {
      data: this.artists,
      selected: item.artists.slice(),
      type: 'actors'
    }, {showBackdrop: true, enableBackdropDismiss: false, cssClass: "big-modal"});
    modal.onDidDismiss(it => {
      this.interceptKey = true;
      this.artists = it.items;
      it.selected.forEach(sel => {
        this.rest.bindArtist(item.movieId, sel.id)
          .subscribe(response => {
            let movie = this.items.find(value => value.movieId == item.movieId);
            movie.artists.push(this.artists.find(value => value.id == sel.id));
          })
      });
    });
    this.interceptKey = false;
    modal.present();
  }

  removeCategory(event, item, category) {
    this.rest.removeCategory(item.movieId, category.id)
      .subscribe(response => {
        item.categories.splice(item.categories.indexOf(category), 1)
      })
  }

  removeArtist(event, item, artist) {
    this.rest.removeArtist(item.movieId, artist.id)
      .subscribe(response => {
        item.artists.splice(item.artists.indexOf(artist), 1)
      })
  }

  openMovieMenu(event, item) {
    let popover = this.popoverCtrl.create(MoviePopover, {
      movie: item
    }, {cssClass: "popover-bg"});
    popover.onDidDismiss((data, role) => {
      if (data != null) {
        if (data.newImage != undefined) {
          this.items.find(value => value.movieId == item.movieId).icon = this.sanitizer.bypassSecurityTrustUrl(data.newImage)
        }
        if (data.deletedMovie == true) {
          this.next(null, this.currentId)
        }
      }
    });
    popover.present({
      ev: event
    });
  }

  onFilterChanged(changes: any) {
    if (this.lastSelectedFilter != this.selectedFilter) {
      this.lastSelectedFilter = this.selectedFilter;
      this.next(null, 0);
    }
  }
}
