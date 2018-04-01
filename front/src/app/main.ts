import {platformBrowserDynamic} from '@angular/platform-browser-dynamic';

import {AppModule} from './app.module';

export const debugMode = false;

platformBrowserDynamic().bootstrapModule(AppModule);


