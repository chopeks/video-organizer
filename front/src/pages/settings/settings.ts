import {Component} from '@angular/core';
import {AlertController, IonicPage, NavController, NavParams} from 'ionic-angular';
import {RESTProvider} from "../../providers/rest/rest";
import {GridPage} from "../grid/grid";
import {ActorsPage} from "../actors/actors";
import {CategoriesPage} from "../categories/categories";

/**
 * Generated class for the SettingsPage page.
 *
 * See https://ionicframework.com/docs/components/#navigation for more info on
 * Ionic pages and navigation.
 */

@IonicPage()
@Component({
  selector: 'page-settings',
  templateUrl: 'settings.html',
})
export class SettingsPage {
  directories: Array<{
    path: string,
    count: number
  }> = [];
  settings: {
    browser: string,
    moviePlayer: string
  } = {
    browser: "",
    moviePlayer: ""
  };

  constructor(
    public navCtrl: NavController,
    public navParams: NavParams,
    private alertCtrl: AlertController,
    public restProvider: RESTProvider
  ) {
    this.refreshDirs()
    this.restProvider.loadSettings()
      .subscribe(it => this.settings = it)
  }

  refreshDirs() {
    this.restProvider.loadDirectories()
      .subscribe(items => {
        this.directories = [];
        items.forEach(it => {
          this.directories.push(it)
        })
      })
  }

  addDirectory(event) {
    let alert = this.alertCtrl.create({
      title: 'Copy directory here, eg. C:\\Porn\\Movies',
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
          this.restProvider.saveDirectory({path: data.url})
            .subscribe(it => this.refreshDirs())
        }
      }]
    });
    alert.present();
  }

  removeDirectory(event, item) {
    this.restProvider.removeDirectory(item)
      .subscribe(it => {
        this.directories.splice(this.directories.indexOf(item), 1)
      })

  }

  saveSettings(event) {
    this.restProvider.saveSettings(this.settings)
  }

  onKey(event) {
    if (event.altKey == true) {
      switch (event.code) {
        case "Digit1":
          this.navCtrl.setRoot(ActorsPage);
          break;
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
