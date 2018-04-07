import {Component} from '@angular/core';
import {AlertController, IonicPage, NavController, NavParams} from 'ionic-angular';
import {SafeUrl} from "@angular/platform-browser";
import {RESTProvider} from "../../providers/rest/rest";
import {appVersion} from "../../app/main";

/**
 * Generated class for the ActorDetailsPage page.
 *
 * See https://ionicframework.com/docs/components/#navigation for more info on
 * Ionic pages and navigation.
 */

@IonicPage({
  name: 'page-actor-details',
})
@Component({
  selector: 'page-actor-details',
  templateUrl: 'actor-details.html',
})
export class ActorDetailsPage {
  actor: {
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
    public rest: RESTProvider
  ) {
    if (navParams.get('item') != null) {
      let actor = navParams.get('item')
      this.actor = {
        id: actor.id,
        name: actor.name,
        image: actor.image
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
          this.actor = {
            id: this.actor.id,
            name: this.actor.name,
            image: data.url
          };
          console.log(this.actor);
        }
      }]
    });
    alert.present();
  }

  save(event) {
    this.rest.saveArtist(this.actor)
      .subscribe(it => {
        this.navCtrl.pop({
          updateUrl: true
        })
      })
  }
}
