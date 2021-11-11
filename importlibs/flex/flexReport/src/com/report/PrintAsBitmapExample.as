package com.report
{
	import flash.display.Sprite;
	import flash.display.Loader;
	import flash.display.Bitmap;
	import flash.display.BitmapData;    
	import flash.printing.PrintJob;
	import flash.printing.PrintJobOptions;
	import flash.events.Event;
	import flash.events.IOErrorEvent;
	import flash.net.URLRequest;
	import flash.geom.Matrix;
	
	public class PrintAsBitmapExample extends Sprite
	{
		private var frame:Sprite = new Sprite();
		private var url:String = "image.jpg";
		private var loader:Loader = new Loader();
		
		public function PrintAsBitmapExample() {			
			var request:URLRequest = new URLRequest(url);
			
			loader.load(request);
			loader.contentLoaderInfo.addEventListener(Event.COMPLETE, completeHandler);
			loader.contentLoaderInfo.addEventListener(IOErrorEvent.IO_ERROR, ioErrorHandler);
		}
		
		public  function completeHandler(event:Event):void {			
			var picture:Bitmap = Bitmap(loader.content);
			var bitmap:BitmapData = picture.bitmapData;
			
			var myBitmap:BitmapData = new BitmapData(loader.width, loader.height, false);			
			var matrix:Matrix = new Matrix();
			
			matrix.scale((200 / bitmap.width), (200 / bitmap.height));
			
			frame.graphics.lineStyle(10);
			frame.graphics.beginBitmapFill(bitmap, matrix, true);
			frame.graphics.drawRect(0, 0, 200, 200);
			frame.graphics.endFill();
			
			addChild(frame);
			
			printPage();    
		}
		
		private function ioErrorHandler(event:IOErrorEvent):void {
			trace("Unable to load the image: " + url);
		}
		
		
  		public  function printPage ():void {
			var myPrintJob:PrintJob = new PrintJob();
			var options:PrintJobOptions = new PrintJobOptions();
			options.printAsBitmap = true;
			
			myPrintJob.start();
			try {
				myPrintJob.addPage(frame, null, options);
			}
			catch(e:Error) {
				trace ("Had problem adding the page to print job: " + e);
			}
			
			try {
				myPrintJob.send();
			}
			catch (e:Error) {
				trace ("Had problem printing: " + e);    
			}
		}
		
	}
}