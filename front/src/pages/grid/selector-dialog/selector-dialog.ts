import {Component, ViewChild} from '@angular/core';
import {AlertController, NavParams, Searchbar, ViewController} from 'ionic-angular';
import {RESTProvider} from "../../../providers/rest/rest";

@Component({
  selector: 'selector-dialog',
  templateUrl: 'selector-dialog.html'
})
export class SelectorDialog {
  allItems: Array<{ id: any, name: string }> = [];
  items: Array<{ id: any, name: string }> = [];
  selected: Array<{ id: any, name: string }> = [];
  type: string

  @ViewChild('searchbar') searchbar: Searchbar;

  constructor(
    public navParams: NavParams,
    public viewCtrl: ViewController,
    public alertCtrl: AlertController,
    public rest: RESTProvider
  ) {

  }

  ionViewWillEnter() {
    this.allItems = this.navParams.get('data');
    this.items = this.allItems.slice();
    this.selected = this.navParams.get('selected');
    this.sortItems();
    this.type = this.navParams.get('type');
  }

  ionViewDifEnter() {
    this.searchbar.setFocus();
  }

  isItemSelected(item) {
    return this.selected.find(it => it.id == item.id) != null
  }

  changeState(event, item) {
    if (event.checked) {
      this.selected.push(item)
    } else {
      this.selected.splice(this.selected.findIndex(it => it.id == item.id), 1)
    }
    this.sortItems();
  }

  filterItems(event) {
    this.items = this.allItems.filter(it => {
      return it.name.toLowerCase().includes(event.target.value.toLowerCase())
    });
    this.sortItems();
  }

  sortItems() {
    this.items.sort((a, b) => {
      if (this.selected.find(it => b.id == it.id) != null)
        return 1;
      if (this.selected.find(it => a.id == it.id) != null)
        return 0;
      return a.name.localeCompare(b.name);
    })
  }

  save() {
    this.viewCtrl.dismiss({
      selected: this.selected
    });
  }

  addNewItem() {
    let type: string;
    if (this.type == 'actors') {
      type = "Actor"
    } else {
      type = "Category"
    }
    let alert = this.alertCtrl.create({
      title: "Add new " + type,
      inputs: [{
        name: 'name',
        placeholder: 'Name'
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
          let stream: any;
          if (this.type == 'actors') {
            stream = this.rest.saveArtist({
              id: -1,
              name: data.name
            })
          } else {
            stream = this.rest.saveCategory({
              id: -1,
              name: data.name
            })
          }
          stream.subscribe(it => {
            if (this.type == 'actors') {
              this.rest.loadArtists()
                .subscribe(it => {
                  this.allItems = it;
                  if (this.searchbar.hasValue()) {
                    this.items = this.allItems.filter(it => {
                      return it.name.toLowerCase().includes(this.searchbar.value.toLowerCase())
                    });
                  } else {
                    this.items = this.allItems.slice();
                  }
                  this.sortItems();
                })
            } else {
              this.rest.loadCategories()
                .subscribe(it => {
                  this.allItems = it;
                  if (this.searchbar.hasValue()) {
                    this.items = this.allItems.filter(it => {
                      return it.name.toLowerCase().includes(this.searchbar.value.toLowerCase())
                    });
                  } else {
                    this.items = this.allItems.slice();
                  }
                  this.sortItems();
                })
            }
          })
        }
      }]
    });
    alert.present();
  }
}
