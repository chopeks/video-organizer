import {Component} from '@angular/core';
import {NavController, NavParams} from 'ionic-angular';
import {DomSanitizer, SafeUrl} from "@angular/platform-browser";
import {RESTProvider} from "../../providers/rest/rest";
import {ActorDetailsPage} from "../actor-details/actor-details";
import {GridPage} from "../grid/grid";
import {CategoriesPage} from "../categories/categories";
import {debugMode} from "../../app/main";

@Component({
  selector: 'page-actors',
  templateUrl: 'actors.html',
})
export class ActorsPage {
  items: Array<{
    id: any,
    name: string,
    image: SafeUrl,
  }> = [];

  constructor(
    public navCtrl: NavController,
    public navParams: NavParams,
    public moviesProvider: RESTProvider,
    private sanitizer: DomSanitizer
  ) {

  }

  ionViewWillEnter() {
    this.items = [];
    this.moviesProvider.loadArtists()
      .subscribe(artists => {
        artists.forEach(it => {
          this.items.push({
            id: it.id,
            name: it.name,
            image: "assets/imgs/picture.svg"
          });
          if (!debugMode) {
            this.moviesProvider.loadArtistImage(it.id)
              .subscribe(images => {
                this.items.find(value => value.id == it.id).image = this.sanitizer.bypassSecurityTrustUrl(images[0])
              });
          }
        })
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
}
