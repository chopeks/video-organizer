import {Component} from '@angular/core';
import {AlertController, NavController, NavParams} from 'ionic-angular';
import {RESTProvider} from "../../providers/rest/rest";
import {GridPage} from "../grid/grid";
import {DomSanitizer} from "@angular/platform-browser";
import {debugMode} from "../../app/main";

/**
 * Generated class for the PossibleDuplicatesPage page.
 *
 * See https://ionicframework.com/docs/components/#navigation for more info on
 * Ionic pages and navigation.
 */

@Component({
  selector: 'page-possible-duplicates',
  templateUrl: 'possible-duplicates.html',
})
export class PossibleDuplicatesPage {
  cachedDuplicates = [];
  duplicates = [];

  constructor(
    public navCtrl: NavController,
    public navParams: NavParams,
    public alertCtrl: AlertController,
    public rest: RESTProvider,
    private sanitizer: DomSanitizer
  ) {
  }

  ionViewDidLoad() {
    this.rest.loadDuplicates()
      .subscribe(data => {
        this.cachedDuplicates = data;
        this.cachedDuplicates.forEach(row => {
          row.textDuration = GridPage.msToTime(row.duration)
        });

        this.cachedDuplicates.splice(0, 10).forEach(it => {
          this.duplicates.push(it)
        });
        this.duplicates.forEach(it => {
          it.movie.forEach(movie => {
            movie.icon = "assets/imgs/picture.svg";
            if (!debugMode) {
              setTimeout(() => {
                this.rest.loadMovieImage(movie.id)
                  .subscribe(image => {
                    this.duplicates.find(value => value.duration == movie.duration).movie
                      .find(value => value.id == movie.id).icon = this.sanitizer.bypassSecurityTrustUrl(image[0])
                  });
              }, 1);
            }
          })
        })
      })
  }

  deleteMovie(event, item) {
    let alert = this.alertCtrl.create({
      title: "Do you really want to delete this file?",
      subTitle: "<B>" + item.title + "</B><br/> will be deleted from <b>hardrive</b>. This operation cannot be reversed.",
      buttons: [{
        text: 'Cancel',
        role: 'cancel',
        handler: data => {
        }
      }, {
        text: 'Delete',
        handler: data => {
          this.rest.deleteMovie(item.id)
            .subscribe(it => {
              let group = this.duplicates.find(value => value.duration == item.duration);
              group.movie.splice(group.movie.indexOf(group.movie.find(value => value.id == item.id)), 1);
              this.duplicates.splice(this.duplicates.indexOf(this.duplicates.find(value => value.duration == group.duration)), 1, group);
              if (group.movie.length == 1) {
                this.duplicates.splice(this.duplicates.indexOf(this.duplicates.find(value => value.duration == item.duration)), 1)
              }
            })
        }
      }]
    });
    alert.present();
  }

  doInfinite(event) {
    this.cachedDuplicates.splice(0, 10).forEach(it => {
      this.duplicates.push(it)
      it.movie.forEach(movie => {
        movie.icon = "assets/imgs/picture.svg";
        if (!debugMode) {
          setTimeout(() => {
            this.rest.loadMovieImage(movie.id)
              .subscribe(image => {
                this.duplicates.find(value => value.duration == movie.duration).movie
                  .find(value => value.id == movie.id).icon = this.sanitizer.bypassSecurityTrustUrl(image[0])
              });
          }, 1);
        }
      })
    });
    event.complete()
  }

  itemTapped(event, item) {
    this.rest.playMovie(item.id)
      .subscribe()
  }
}
