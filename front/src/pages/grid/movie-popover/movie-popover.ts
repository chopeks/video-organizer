import {Component} from '@angular/core';
import {AlertController, NavParams, Platform, ViewController} from 'ionic-angular';
import {RESTProvider} from "../../../providers/rest/rest";
import {SafeUrl} from "@angular/platform-browser";

@Component({
  selector: 'movie-popover',
  template: `
    <ion-buttons class="graybg">
      <button ion-button clear color="secondary" (click)="generateImage()">generate thumbnail</button>
      <button ion-button clear color="secondary" (click)="deleteMovie()">delete movie</button>
      <!--<button ion-button clear color="secondary" (click)="openLocation()" [hidden]="windowsOnly">open location</button>-->
    </ion-buttons>
  `
})
export class MoviePopover {
  item: {
    movieId: any,
    title: string,
    duration: string,
    icon: SafeUrl,
    artists: Array<{ id: any, name: string }>,
    categories: Array<{ id: any, name: string }>
  };

  windowsOnly: boolean;

  constructor(
    public navParams: NavParams,
    public viewCtrl: ViewController,
    public platform: Platform,
    public alertCtrl: AlertController,
    public rest: RESTProvider
  ) {
    this.item = navParams.get("movie");
    this.windowsOnly = !(platform.userAgent().indexOf("Windows") >= 0);
  }

  generateImage() {
    this.rest.refreshImage(this.item.movieId)
      .subscribe(images => {
        this.close({
          newImage: images[0]
        });
      });
  }

  deleteMovie() {
    let alert = this.alertCtrl.create({
      title: "Do you really want to delete this file?",
      subTitle: "<B>" + this.item.title + "</B><br/> will be deleted from <b>hardrive</b>. This operation cannot be reversed.",
      buttons: [{
        text: 'Cancel',
        role: 'cancel',
        handler: data => {
          this.close(null)
        }
      }, {
        text: 'Delete',
        handler: data => {
          this.rest.deleteMovie(this.item.movieId)
            .subscribe(it => {
              this.close({
                deletedMovie: true
              })
            })
        }
      }]
    });
    alert.present();
  }

  openLocation() {

  }

  close(params) {
    this.viewCtrl.dismiss(params);
  }
}
