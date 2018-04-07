import {platformBrowserDynamic} from '@angular/platform-browser-dynamic';

import {AppModule} from './app.module';

export const debugMode = true;

platformBrowserDynamic().bootstrapModule(AppModule);


