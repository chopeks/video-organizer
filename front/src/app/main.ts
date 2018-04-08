import {platformBrowserDynamic} from '@angular/platform-browser-dynamic';

import {AppModule} from './app.module';

export const debugMode = false;
export const appVersion = "1.0.0";

platformBrowserDynamic().bootstrapModule(AppModule);


