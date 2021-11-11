package org.doc
{
	import flash.display.Sprite; 
	import flash.display.Stage; 
	import flash.geom.Rectangle; 
	//import flash.printing. .PaperSize; 
	import flash.printing.PrintJob; 
	import flash.printing.PrintJobOrientation; 
	import mx.printing.FlexPrintJob;
	import flash.text.TextField; 

	public class PrintJobExample extends Sprite 
	{	 
		private var bg:Sprite; 
		private var txt:TextField; 
		private var pj:FlexPrintJob; 
		
		public function PrintJobExample():void 
		{ 
			var pj = new FlexPrintJob(); 
			initPrintJob(); 
			initContent(); 
			draw(); 
			printPage(); 
		} 
		
		private function printPage():void 
		{ 
			if (pj.start()) {
				pj.send(); 
			}
		} 
		
		private function initContent():void 
		{ 
			bg = new Sprite(); 
			bg.graphics.beginFill(0x00FF00); 
			bg.graphics.drawRect(0, 0, 100, 200); 
			bg.graphics.endFill(); 
			
			txt = new TextField(); 
			txt.border = true; 
			txt.text = "Hello World"; 
		} 
		
		private function initPrintJob():void 
		{ 
			 
		} 
		
		private function draw():void 
		{ 
			addChild(bg); 
			addChild(txt); 
			txt.x = 50; 
			txt.y = 50; 
		}
		
		
	}
}