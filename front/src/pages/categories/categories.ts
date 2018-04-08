import {Component, ViewChild} from '@angular/core';
import {NavController, NavParams, Searchbar} from 'ionic-angular';
import {DomSanitizer, SafeUrl} from "@angular/platform-browser";
import {RESTProvider} from "../../providers/rest/rest";
import {GridPage} from "../grid/grid";
import {CategoryDetailsPage} from "../category-details/category-details";
import {ActorsPage} from "../actors/actors";
import {debugMode} from "../../app/main";

/**
 * Generated class for the CategoriesPage page.
 *
 * See https://ionicframework.com/docs/components/#navigation for more info on
 * Ionic pages and navigation.
 */

@Component({
  selector: 'page-categories',
  templateUrl: 'categories.html',
})
export class CategoriesPage {
  allItems: Array<{ id: any, name: string, image: SafeUrl }> = [];
  items: Array<{ id: any, name: string, image: SafeUrl }> = [];

  @ViewChild('navSearchbar') searchbar: Searchbar;

  constructor(
    public navCtrl: NavController,
    public navParams: NavParams,
    public moviesProvider: RESTProvider,
    private sanitizer: DomSanitizer
  ) {

  }

  ionViewWillEnter() {
    this.items = [];
    this.moviesProvider.loadCategories()
      .subscribe(categories => {
        categories.forEach(it => {
          this.allItems.push({
            id: it.id,
            name: it.name,
            image: "assets/imgs/picture.svg"
          });
          if (!debugMode) {
            setTimeout(() => {
              this.moviesProvider.loadCategoryImage(it.id)
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
    this.navCtrl.push(CategoryDetailsPage, {
      item: item
    })
  }

  itemTapped(event, item) {
    this.navCtrl.setRoot(GridPage, {
      category: item
    })
  }

  add(event) {
    this.navCtrl.push(CategoryDetailsPage)
  }

  onKey(event) {
    if (event.altKey == true) {
      switch (event.code) {
        case "Digit1":
          this.navCtrl.setRoot(ActorsPage);
          break;
        case "Digit3":
          this.navCtrl.setRoot(GridPage);
          break;
      }
    }
  }
}
