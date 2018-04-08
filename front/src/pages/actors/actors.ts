import {Component, ViewChild} from '@angular/core';
import {NavController, NavParams, Searchbar} from 'ionic-angular';
import {DomSanitizer, SafeUrl} from "@angular/platform-browser";
import {RESTProvider} from "../../providers/rest/rest";
import {ActorDetailsPage} from "../actor-details/actor-details";
import {GridPage} from "../grid/grid";
import {CategoriesPage} from "../categories/categories";
import {appVersion, debugMode} from "../../app/main";

@Component({
  selector: 'page-actors',
  templateUrl: 'actors.html',
})
export class ActorsPage {
  hideNewVersion: boolean = true;
  allItems: Array<{ id: any, name: string, image: SafeUrl }> = [];
  items: Array<{ id: any, name: string, image: SafeUrl }> = [];

  @ViewChild('navSearchbar') searchbar: Searchbar;


  constructor(
    public navCtrl: NavController,
    public navParams: NavParams,
    public rest: RESTProvider,
    private sanitizer: DomSanitizer
  ) {
    this.rest.githubTag(value => {
      if (value.valueOf() != appVersion.valueOf()) {
        this.hideNewVersion = false
      }
    })
  }

  ionViewWillEnter() {
    this.items = [];
    this.rest.loadArtists()
      .subscribe(artists => {
        artists.sort((lhs, rhs) => lhs.name.toLowerCase().localeCompare(rhs.name.toLowerCase()));
        artists.forEach(it => {
          this.allItems.push({
            id: it.id,
            name: it.name,
            image: "assets/imgs/picture.svg"
          });
          if (!debugMode) {
            setTimeout(() => {
              this.rest.loadArtistImage(it.id)
                .subscribe(images => {
                  this.items.find(value => value.id == it.id).image = this.sanitizer.bypassSecurityTrustUrl(images[0])
                });
            }, 1);
          }
          this.filterItems({target: {value: ""}})
        })
      });
  }

  filterItems(event) {
    this.items = this.allItems.filter(it => {
      return it.name.toLowerCase().includes(event.target.value.toLowerCase())
    });
  }

  openDetails(event, item) {
    this.navCtrl.push(ActorDetailsPage, {
      item: item
    })
  }

  itemTapped(event, item) {
    this.navCtrl.setRoot(GridPage, {
      actor: item
    })
  }

  add(event) {
    this.navCtrl.push(ActorDetailsPage)
  }

  onKey(event) {
    if (event.altKey == true) {
      switch (event.code) {
        case "Digit2":
          this.navCtrl.setRoot(CategoriesPage);
          break;
        case "Digit3":
          this.navCtrl.setRoot(GridPage);
          break;
      }
    }
  }

  goToGithub() {
    window.open('https://github.com/chopeks/video-organizer/releases', '_system')
  }
}
