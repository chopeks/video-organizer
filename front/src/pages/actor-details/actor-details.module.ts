import { NgModule } from '@angular/core';
import { IonicPageModule } from 'ionic-angular';
import { ActorDetailsPage } from './actor-details';

@NgModule({
  declarations: [
    ActorDetailsPage,
  ],
  imports: [
    IonicPageModule.forChild(ActorDetailsPage),
  ],
})
export class ActorDetailsPageModule {}
