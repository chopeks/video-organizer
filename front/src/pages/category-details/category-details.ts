import {Component} from '@angular/core';
import {AlertController, IonicPage, NavController, NavParams} from 'ionic-angular';
import {SafeUrl} from "@angular/platform-browser";
import {RESTProvider} from "../../providers/rest/rest";

/**
 * Generated class for the CategoryDetailsPage page.
 *
 * See https://ionicframework.com/docs/components/#navigation for more info on
 * Ionic pages and navigation.
 */

@IonicPage({
  name: 'page-category-details'
})
@Component({
  selector: 'page-category-details',
  templateUrl: 'category-details.html',
})
export class CategoryDetailsPage {
  category: {
    id: any,
    name: string,
    image: SafeUrl,
  } = {
    id: null,
    name: null,
    image: null
  };

  constructor(
    public navCtrl: NavController,
    public navParams: NavParams,
    private alertCtrl: AlertController,
    public moviesProvider: RESTProvider
  ) {
    if (navParams.get('item') != null) {
      let category = navParams.get('item');
      this.category = {
        id: category.id,
        name: category.name,
        image: category.image
      };
    }
  }

  addUrl(event) {
    let alert = this.alertCtrl.create({
      title: 'Copy url here',
      inputs: [{
        name: 'url',
        placeholder: 'Url'
      }],
      buttons: [{
        text: 'Cancel',
        role: 'cancel',
        handler: data => {
          console.log('Cancel clicked');
        }
      }, {
        text: 'Add',
        handler: data => {
          this.category = {
            id: this.category.id,
            name: this.category.name,
            image: data.url
          };
        }
      }]
    });
    alert.present();
  }

  save(event) {
    this.moviesProvider.saveCategory(this.category)
      .subscribe(it => {
        this.navCtrl.pop({
          updateUrl: true
        })
      })
  }

}
