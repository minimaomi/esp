package com.report
{
	import flash.display.Bitmap;
	import flash.display.BitmapData;
	import flash.display.Loader;
	import flash.display.LoaderInfo;
	import flash.display.Shape;
	import flash.display.Sprite;
	import flash.events.Event;
	import flash.geom.Matrix;
	import flash.geom.Point;
	import flash.net.URLRequest;
	import flash.text.TextField;
	import flash.text.TextFormat;
	import flash.text.engine.RenderingMode;
	import flash.text.engine.TextLine;
	import flash.utils.ByteArray;
	
	import mx.utils.Base64Decoder;
	
	public class ReportDraws extends Sprite
	{		
		private var color:uint=0;
		private var m_lastOffset:int = -1;//最后一列偏移距
		private  var lineStrokes:Array;
		private var fontSize:Number=12;
		private var dash:String;
		private var fmt:TextFormat = new TextFormat();
		public static var defaultScale:Number = 1;
		private var embedFonts:Boolean=false;
		private var fontFamily:String= "宋体";
		private var loadedmages:Array  = null;
		private var leftMargin:int=0,toptMargin:int=0;
		private var m_colCur:int=0;
		private var m_lineHeight:int=0;		
		
		public function ReportDraws(embedFonts:Boolean=false,fontFamily:String="宋体")
		{
			this.embedFonts = embedFonts;		 
			this.fontFamily = fontFamily;
			m_lineHeight = 0;
		}
		
		public function setColor(color:uint):void{
			this.color = color+16777216;
		}
		
		public function setMargin(left:int,top:int):void{
			this.leftMargin = left;
			this.toptMargin = top;
		}
				
		public function setFont(fontName, fontSize,isBold:String, isItalic:String):void{
			 var fName:String=fontName;
			 var fSize:Number=Number(fontSize);;
			 var Bold:Boolean=false;
			 var Italic:Boolean=false;
			
			if(isBold.toLowerCase()=="true"){
				Bold = true;
			}
			else{
				Bold = false;
			}
			if(isItalic.toLowerCase()=="true"){
				Italic = true;
			}
			else{
				Italic = false;
			}
		
			fmt.font =  fName;
		
		    fmt.size = fSize;
			this.fontSize = fSize;
			fmt.italic = Italic;
			fmt.bold = Bold;
			
			var mycolor:uint = this.color;
			fmt.color = mycolor;
		}
		
		public function setStroke(stokes:Array):void{
			this.lineStrokes = stokes;//Number(stokes[0]);
		}
		
		public function setLodedImage(loadedImage:Array):void{
			loadedmages  = loadedImage;
		}
		
		public function drawImage(base64:String,x,y,w,h):void{
			if(loadedmages!=null)
			   loadedmages.push(1);
			
			var base64Des:Base64Decoder = new Base64Decoder();
			base64Des.decode(base64);
			var byteArr:ByteArray = base64Des.toByteArray();
			var loader:Loader = new Loader();
			loader.contentLoaderInfo.addEventListener(Event.COMPLETE,function(e:Event):void{
				//_img.source = e.currentTarget.content;
				var loaderInfo:LoaderInfo = e.target as LoaderInfo;
				var image:Bitmap =loaderInfo.content as Bitmap;
				//generateDocument(image,null);
				image.x = (int(x)+1+leftMargin)*defaultScale;
				image.y = (int(y)+1+toptMargin)*defaultScale;
				
				image.width = int(w-2)*defaultScale;
				image.height = int(h-2)*defaultScale;
			
				//var jpg:JPGEncoder = new JPGEncoder();
				addChild(image);
				if(loadedmages!=null&&loadedmages.length>0){
					loadedmages.pop();
				}
			});
			loader.loadBytes(byteArr);
		//	loader.load(new URLRequest("D:/worked/1.png"));
		}
		
		public function drawString(x,y,strings):void{	
			var txtField:TextField = new TextField();
			txtField.text = strings;
			
			fmt.size = fontSize*defaultScale
			
			if(embedFonts&&fmt.font!="Wingdings"){
				fmt.font = this.fontFamily;
				txtField.embedFonts = true;
			}
			txtField.setTextFormat(fmt);
			txtField.x = (leftMargin+int(x))*defaultScale;
			txtField.y = (toptMargin+int(y))*defaultScale-txtField.textHeight;
			
			this.addChild(txtField);
		}
		
	    public function drawRect(x,y,w,h):void{
			this.graphics.beginFill(int(0xffffff),1);
			this.graphics.drawRect(x,y,w*defaultScale,h*defaultScale);
			this.graphics.endFill();
			
		}
		public function drawLine(xs1,ys1,xs2,ys2):void{
			 var x1:int = int(xs1)+leftMargin;
			 var x2:int = int(xs2)+leftMargin;
			var y1:int = int(ys1)+toptMargin;
			var y2:int = int(ys2)+toptMargin;
			//双线问题
			if(lineStrokes.length==1 ){
				if (y1==y2 && y1>0){
					if (m_lineHeight+1 == y1){
						//trace("lineH=", m_lineHeight);
						this.graphics.lineStyle(Number(this.lineStrokes[0])*defaultScale,this.color,1);
						this.graphics.moveTo(int(x1)*defaultScale,int(m_lineHeight)*defaultScale+1);
						this.graphics.lineTo(int(x2)*defaultScale,int(m_lineHeight)*defaultScale+1);
						this.graphics.lineStyle(0,this.color,0);
					}else{
						this.graphics.lineStyle(Number(this.lineStrokes[0])*defaultScale,this.color,1);
						this.graphics.moveTo(int(x1)*defaultScale,int(y1)*defaultScale);
						this.graphics.lineTo(int(x2)*defaultScale,int(y2)*defaultScale);
						this.graphics.lineStyle(0,this.color,0);
					}
				}else{
				  this.graphics.lineStyle(Number(this.lineStrokes[0])*defaultScale,this.color,1);
			      this.graphics.moveTo(int(x1)*defaultScale,int(y1)*defaultScale);
				  this.graphics.lineTo(int(x2)*defaultScale,int(y2)*defaultScale);
				  this.graphics.lineStyle(0,this.color,0);
				}
			}
			else{
				drawDashLine(new Point(int(x1),int(y1)),new Point(int(x2),int(y2)),lineStrokes);
			}
			// 记录每行最后一列第一个高度) (可能有多个)
			if (y1==y2 && y1>0 ){
				if (m_colCur==0){
					m_lineHeight = y1;
				}
			}
		}
		
		private function drawDashLine(fromPoint:Point,toPoint:Point,dashs:Array):void{
			var max:Number = Point.distance(fromPoint, toPoint); 
			var dis:Number = 0; 
			while(dis < max)
			{
				for(var i:int=1;i<dashs.length;i++){
					var p1 = Point.interpolate(fromPoint, toPoint, (max-dis) / max);  
					var length:Number = Number(dashs[i]);
					dis += length;
					var p2 = Point.interpolate(fromPoint, toPoint, (max-dis) / max);  
					if(i%2==1){
						this.graphics.lineStyle(Number(this.lineStrokes[0])*defaultScale,this.color,1);
					    this.graphics.moveTo(p1.x*defaultScale,p1.y*defaultScale);
					    this.graphics.lineTo(p2.x*defaultScale,p2.y*defaultScale);
					    this.graphics.lineStyle(0,this.color,0);
					}
				}
			}
		}
		
		public function fillRect(xs,ys,w,h):void{
			 
			var x:int = int(xs)+leftMargin;
			var y:int = int(ys)+toptMargin;

			this.graphics.beginFill(this.color,1);
			this.graphics.drawRect(int(x)*defaultScale,int(y)*defaultScale,int(w)*defaultScale,int(h)*defaultScale);
			this.graphics.endFill();
		}
		
		private var rowpos_y:uint=0;
		public function drawChars2(cx1:String,cx2:String,y,col:int,bOffset:Boolean):void{			
			if(cx1==null||cx1=="")return;
			var field:TextField = new TextField();
			var fx:Number=0;
			var fy:Number=0;
			var nsub1:int = 0;
			//var fmt1:TextFormat = new TextFormat("宋体",50,uint(0x00ff00),true,true,true);
			//if (711==int(y)){
			//	var aa:int = 0;
			//}
			m_colCur = col;
			m_lastOffset = 5;
			var cdotIndex = cx1.indexOf(",");
			var xdotIndex = cx2.indexOf(",");
			if(cdotIndex>=0&&xdotIndex>=0){
				var charsub1:String = cx1.substring(0,cdotIndex).replace("（","(").replace("）",")");
				var charsub2:String = cx1.substring(cdotIndex+1,cx1.length);
				var xsub1:String = cx2.substring(0,xdotIndex);
			   var xsub2:String = cx2.substring(xdotIndex+1,cx2.length);
			   if(charsub1=="comma"){
				   field.text = ",";
			   }else{
			       field.text = charsub1;
			   }
			   
			   nsub1=int(xsub1);
			   if (!bOffset){
				   fx = (nsub1+leftMargin)*defaultScale;	
			   }else{
				   fx = (nsub1+leftMargin-m_lastOffset)*defaultScale;	
			   }

			   drawChars2(charsub2,xsub2,y,col,bOffset);
			}
			else{				
				if(cx1=="comma"){
					field.text =",";
				}else{
					field.text =cx1.replace("（","(").replace("）",")");
				}
				
				if (!bOffset){
					fx = (int(cx2)+leftMargin)*defaultScale;
				}else{
					fx = (int(cx2)+leftMargin-m_lastOffset)*defaultScale;
				}
			}
			//trace("tt=",field.text,field.height+";"+field.textHeight);
			fmt.size = fontSize*defaultScale
			fmt.color = this.color;
			if(embedFonts&&fmt.font!="Wingdings"&&field.text.indexOf("㎡")==-1
			           &&fmt.font.toLowerCase().indexOf("serif")==-1){
				fmt.font = this.fontFamily;
				field.embedFonts = true;
			}
			field.setTextFormat(fmt);
			fy = (int(y)+toptMargin)*defaultScale-field.textHeight ;
			
			//trace("tt=",field.text,";fx=",fx+";fx="+fy);
			field.x = fx;
			field.y = fy;
		    addChild(field);
			
		    if(embedFonts&&(fmt.font=="Wingdings"||fmt.font.toLowerCase().indexOf("serif")>-1)){
				 try{
					 var bd:BitmapData = new BitmapData(field.textWidth+1,field.textHeight+1, false);
					 var m:Matrix = new Matrix();  
					 bd.draw(field,m);
					 var result:Bitmap = new Bitmap(bd);
					  result.x = fx;
					  result.y =  fy ;
					  addChild(result);
				 }
				 catch( errorName:Error){
					 
				 }
			 }
		//  save bitmap for test
		//	var b:BitmapData = new BitmapData(field.width, field.height, true, null);
		//	b.draw(field);
		//	var m:Bitmap = new Bitmap(b);
		//	 m.x = field.x;
		//	 m.y = field.y;
		//	addChild(m);			
		}
		
		public function drawChars(cx1:String,x:int,y:int,w:int, col:int,bOffset:Boolean):void{
			
			if(cx1==null||cx1=="")return;			
			var field:TextField = new TextField();
			var fx:Number=0;
			var fy:Number=0;
			var nsub1:int = 0;
			
			m_colCur = col;
			m_lastOffset = 5;
			field.text = cx1;
				
			if (!bOffset){
				fx = (x+leftMargin)*defaultScale;
			}else{
				fx = (x+leftMargin-m_lastOffset)*defaultScale;
			}
			
			//trace("tt=",field.text,field.height+";"+field.textHeight);
			fmt.size = fontSize*defaultScale
			fmt.color = this.color;
			if(embedFonts&&fmt.font!="Wingdings"&&field.text.indexOf("㎡")==-1
				&&fmt.font.toLowerCase().indexOf("serif")==-1){
				fmt.font = this.fontFamily;
				field.embedFonts = true;
			}
			field.setTextFormat(fmt);
			fy = (y+toptMargin)*defaultScale-field.textHeight ;
			
			//trace("tt=",field.text,";fx=",fx+";fx="+fy);
			field.x = fx;
			field.y = fy;
			field.width = (w *defaultScale *72)/25.4;
			//field.width = (w+14) *defaultScale*1.35;
			addChild(field);
			
			//fillRect(field.x,field.y,field.width,2);
			
			if(embedFonts&&(fmt.font=="Wingdings"||fmt.font.toLowerCase().indexOf("serif")>-1)){
				try{
					var bd:BitmapData = new BitmapData(field.textWidth+1,field.textHeight+1, false);
					var m:Matrix = new Matrix();  
					bd.draw(field,m);
					var result:Bitmap = new Bitmap(bd);
					result.x = fx;
					result.y =  fy ;
					addChild(result);
				}
				catch( errorName:Error){
					
				}
			}
		}
	}
}