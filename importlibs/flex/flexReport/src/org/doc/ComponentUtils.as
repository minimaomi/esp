
package org.doc
{
	import flash.display.Bitmap;
	import mx.core.UIComponent;
	import flash.geom.Matrix;
	import flash.display.BitmapData;
	import mx.core.Container;
	import flash.display.DisplayObject;
	import mx.core.IFlexDisplayObject;
	
	public class ComponentUtils
	{
		public function ComponentUtils():void
		{
			
		}
		
		public static function captureComponent(source:UIComponent, width:Number):Bitmap
		{
			var scale:Number = width / source.width;
			var matrix:Matrix = new Matrix();
			var bd:BitmapData = new BitmapData( width, source.height * scale, false );
			
			matrix.scale( scale, scale );
			bd.draw( source, matrix );
					
			var result:Bitmap = new Bitmap(bd);
			
			return result;			
		}

		public static function duplicateComponent(source:UIComponent):UIComponent
		{
			var container:Container = new Container();
			var clone:IFlexDisplayObject = container.createComponentFromDescriptor(source.descriptor, true);
			
			return clone as UIComponent;
		}
	}
}