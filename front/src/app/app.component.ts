import {Component, ViewChild} from '@angular/core';
import {Nav, Platform} from 'ionic-angular';
import {StatusBar} from '@ionic-native/status-bar';
import {SplashScreen} from '@ionic-native/splash-screen';

import {GridPage} from "../pages/grid/grid";
import {ActorsPage} from "../pages/actors/actors";
import {SettingsPage} from "../pages/settings/settings";
import {CategoriesPage} from "../pages/categories/categories";

@Component({
  templateUrl: 'app.html'
})
export class MyApp {
  @ViewChild(Nav) nav: Nav;

  rootPage: any = ActorsPage; // TODO

  pages: Array<{ title: string, component: any }>;

  constructor(public platform: Platform, public statusBar: StatusBar, public splashScreen: SplashScreen) {
    this.initializeApp();

    // used for an example of ngFor and navigation
    this.pages = [
      {title: 'Actors', component: ActorsPage},
      {title: 'Categories', component: CategoriesPage},
      {title: 'Movies', component: GridPage},
      {title: 'Settings', component: SettingsPage}
    ];

    window.scrollTo(0,1);
  }

  initializeApp() {
    this.platform.ready().then(() => {
      // Okay, so the platform is ready and our plugins are available.
      // Here you can do any higher level native things you might need.
      this.statusBar.styleBlackOpaque();
      this.splashScreen.hide();
    });
  }

  openPage(page) {
    // Reset the content nav to have just this page
    // we wouldn't want the back button to show in this scenario
    this.nav.setRoot(page.component);
  }
}
