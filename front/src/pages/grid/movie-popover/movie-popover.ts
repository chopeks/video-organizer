import {Component} from '@angular/core';
import {NavParams, Platform, ViewController} from 'ionic-angular';
import {RESTProvider} from "../../../providers/rest/rest";
import {SafeUrl} from "@angular/platform-browser";

@Component({
  selector: 'movie-popover',
  template: `
    <ion-buttons class="graybg">
      <button ion-button clear color="secondary" (click)="generateImage()">generate thumbnail</button>
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

  openLocation() {

  }

  close(params) {
    this.viewCtrl.dismiss(params);
  }
}
