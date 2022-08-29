import { GraphicEntityModule } from './entity-module/GraphicEntityModule.js';
import { TooltipModule } from './tooltip-module/TooltipModule.js';
import { EndScreenModule } from './endscreen-module/EndScreenModule.js';

export const playerColors = [
  '#f7ef05', // yellow
  '#22a1e4' // curious blue
];

export const modules = [
	GraphicEntityModule,
	EndScreenModule,
	TooltipModule
];