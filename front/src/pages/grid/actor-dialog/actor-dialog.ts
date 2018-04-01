import {Component, Renderer} from '@angular/core';
import {ViewController} from 'ionic-angular';

@Component({
  selector: 'actor-dialog',
  templateUrl: 'actor-dialog.html'
})
export class ActorDialog {

  text: string;

  constructor(public renderer: Renderer, public viewCtrl: ViewController) {
    this.renderer.setElementClass(viewCtrl.pageRef().nativeElement, 'custom-popup', true);

  }

}
