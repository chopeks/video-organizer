import {BrowserModule} from '@angular/platform-browser';
import {ErrorHandler, NgModule} from '@angular/core';
import {IonicApp, IonicErrorHandler, IonicModule} from 'ionic-angular';

import {MyApp} from './app.component';

import {StatusBar} from '@ionic-native/status-bar';
import {SplashScreen} from '@ionic-native/splash-screen';
import {RESTProvider} from '../providers/rest/rest';
import {HttpClientModule} from "@angular/common/http";
import {GridPage} from "../pages/grid/grid";
import {ActorsPage} from "../pages/actors/actors";
import {ActorDetailsPage} from "../pages/actor-details/actor-details";
import {SettingsPage} from "../pages/settings/settings";
import {CategoriesPage} from "../pages/categories/categories";
import {CategoryDetailsPage} from "../pages/category-details/category-details";
import {SelectorDialog} from "../pages/grid/selector-dialog/selector-dialog";
import {MoviePopover} from "../pages/grid/movie-popover/movie-popover";

@NgModule({
  declarations: [
    MyApp,
    GridPage,
    SelectorDialog,
    MoviePopover,
    ActorsPage,
    ActorDetailsPage,
    CategoriesPage,
    CategoryDetailsPage,
    SettingsPage
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    IonicModule.forRoot(MyApp),
  ],
  bootstrap: [IonicApp],
  entryComponents: [
    MyApp,
    GridPage,
    SelectorDialog,
    MoviePopover,
    ActorsPage,
    ActorDetailsPage,
    CategoriesPage,
    CategoryDetailsPage,
    SettingsPage
  ],
  providers: [
    StatusBar,
    SplashScreen,
    {provide: ErrorHandler, useClass: IonicErrorHandler},
    RESTProvider
  ]
})
export class AppModule {
}
