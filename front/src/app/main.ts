import {platformBrowserDynamic} from '@angular/platform-browser-dynamic';

import {AppModule} from './app.module';

export const debugMode = false;
export const appVersion = "0.9.4";

platformBrowserDynamic().bootstrapModule(AppModule);


