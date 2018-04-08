import {platformBrowserDynamic} from '@angular/platform-browser-dynamic';

import {AppModule} from './app.module';

export const debugMode = false;
export const appVersion = "0.9.5";

platformBrowserDynamic().bootstrapModule(AppModule);


