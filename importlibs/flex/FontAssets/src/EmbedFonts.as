package
{
	import flash.display.Sprite;
	import flash.text.Font;
	import flash.text.StyleSheet;
	import flash.text.TextField;
	import flash.text.TextFormat;
	import flash.text.StaticText;
	
	/**
	 * @author Frost.Yen
	 * @E-mail 871979853@qq.com
	 * @create 2015-6-26 下午3:23:38
	 *
	 */
	[SWF(width="800",height="600")]
	public class EmbedFonts extends Sprite
	{
		/**
		 *  source 指定要嵌入的字体文件路径。还可以用 systemFont指定一个系统中安装的字体。这样的话就可以不需要 source参数了。
		 fontName 这个实际上就是 fontFamily 的别名。
		 embedAsCFF 如果不提供这个参数，默认就是true。所以，如果系统你嵌入的字体用于TextField，一定要将其设置为false。
		 unicodeRange 要嵌入的文本的范围。见下表：
		 
		 嵌入字体范围：
		 大写字符 U+0020,U+0041-U+005A
		 小写字符 U+0020,U+0061-U+007A
		 数字 U+0030-U+0039,U+002E
		 标点符号 U+0020-U+002F,U+003A-U+0040,U+005B-U+0060,U+007B-U+007E
		 基本拉丁字符 U+0020-U+002F, U+0030-U+0039, U+003A-U+0040, U+0041-U+005A, U+005B-U+0060, U+0061-U+007A, U+007B-U+007E
		 中文字符 U+3000-303F,U+3105-312C,U+31A0-31BF,U+4E00-9FAF,U+FF01-FF60,U+F900-FAFF,U+201C-201D,U+2018-2019,U+2014,U+2026,U+FFE5,U+00B7
		 
		 详细的可以找到你本机的 FlexSDK/frameworks/flash-unicode-table.xml。还可以参考Setting character ranges。
		 */        
		/**
		[Embed(source="songs.TTF",fontName="SongTi",embedAsCFF="false",mimeType="application/x-font",
			unicodeRange="U+0020-U+002F, U+0030-U+0039, U+003A-U+0040, U+0041-U+005A, U+005B-U+0060, U+0061-U+007A, U+007B-U+007E,U+0020-U+002F,U+003A-U+0040,U+005B-U+0060,U+007B-U+007E,U+0030-U+0039,U+002E,U+0020,U+0061-U+007A,U+0020,U+0041-U+005A,U+3000-303F,U+3105-312C,U+31A0-31BF,U+4E00-9FAF,U+FF01-FF60,U+F900-FAFF,U+201C-201D,U+2018-2019,U+2014,U+2026,U+FFE5,U+00B7")]
			 
		[Embed(mimeType="application/x-font",  embedAsCFF="false", 
			source="jingdian.TTF",fontName="myfont", advancedAntiAliasing="true")]

		[Embed(mimeType="application/x-font", source="black.TTF",fontName="myfont",embedAsCFF="false",
			unicodeRange="U+0020-U+002F, U+0030-U+0039, U+003A-U+0040, U+0041-U+005A, U+005B-U+0060, U+0061-U+007A, U+007B-U+007E,U+0020-U+002F,U+003A-U+0040,U+005B-U+0060,U+007B-U+007E,U+0030-U+0039,U+002E,U+0020,U+0061-U+007A,U+0020,U+0041-U+005A,U+3000-303F,U+3105-312C,U+31A0-31BF,U+4E00-9FAF,U+FF01-FF60,U+F900-FAFF,U+201C-201D,U+2018-2019,U+2014,U+2026,U+FFE5,U+00B7")]
		**/	
		//NotoSansHans-Regular.otf SourceHanSerifCN-Heavy-4.otf
		//[Embed(mimeType="application/x-font", source="song.TTF",fontName="myfont2",embedAsCFF="false")]
		[Embed(mimeType="application/x-font", source="jingdian.TTF",fontName="宋体",embedAsCFF="false", fontWeight="bold",advancedAntiAliasing="true")]
			//unicodeRange="U+0020-U+002F, U+0030-U+0039, U+003A-U+0040, U+0041-U+005A, U+005B-U+0060, U+0061-U+007A, U+007B-U+007E,U+0020-U+002F,U+003A-U+0040,U+005B-U+0060,U+007B-U+007E,U+0030-U+0039,U+002E,U+0020,U+0061-U+007A,U+0020,U+0041-U+005A,U+3000-303F,U+3105-312C,U+31A0-31BF,U+4E00-9FAF,U+FF01-FF60,U+F900-FAFF,U+201C-201D,U+2018-2019,U+2014,U+2026,U+FFE5,U+00B7")]
		private var _itfont1:Class;
		[Embed(mimeType="application/x-font", source="songb.TTF",fontName="粗宋体",embedAsCFF="false", advancedAntiAliasing="true")]
		//unicodeRange="U+0020-U+002F, U+0030-U+0039, U+003A-U+0040, U+0041-U+005A, U+005B-U+0060, U+0061-U+007A, U+007B-U+007E,U+0020-U+002F,U+003A-U+0040,U+005B-U+0060,U+007B-U+007E,U+0030-U+0039,U+002E,U+0020,U+0061-U+007A,U+0020,U+0041-U+005A,U+3000-303F,U+3105-312C,U+31A0-31BF,U+4E00-9FAF,U+FF01-FF60,U+F900-FAFF,U+201C-201D,U+2018-2019,U+2014,U+2026,U+FFE5,U+00B7")]
		private var _itfont2:Class;
		
//		
//		[Embed(mimeType="application/x-font", source="vista.ttf", fontWeight="bold",fontName="_myfont5", embedAsCFF="false", advancedAntiAliasing="true")]
//		private var _itfont:Class;; 
		
		public function EmbedFonts()
		{
			var fontArr:Array= Font.enumerateFonts(false);
			for each(var font:Font in fontArr){
				trace(font.fontName, font.fontType);
				//Font.registerFont(font);//注册字体，如果在Embed字体的类中使用它，则不需要注册字体也可，上面的for each可以测试得知
			}
			
			test2();
		}
		
		private function test():void
		{
			var field:TextField = new TextField();
			var fx:Number=0;
			var fy:Number=0;
			
			field.text = "中国ABCDEFGHIJKLMNOPQRSTUVWXYZ\n";
			field.appendText("今天很困难，明天更困难，后天很美好。但是绝大多数人死在了明天晚上，如果你不努力的话。\n");
			
			var fmt:TextFormat = new TextFormat();
			fmt.font = "宋体";
			var oldFontName = fmt.font;
			
			//trace("tt=",field.text,field.height+";"+field.textHeight);
			
			fmt.size = 24;
			fmt.color = 0x000000;
			//fmt.bold = true;
			//fmt.letterSpacing=0.2;
						
			if(fmt.font=="宋体"){
				field.embedFonts = false;
			}
			
			field.setTextFormat(fmt);
			
			//trace(";fx=",y+";fy="+fy,";tt=",field.text);
			//trace("tt=",field.text,";font=",fmt.font,";fx=",fx+"; xx="+x);
			trace("font=",fmt.font,";embedFonts=",field.embedFonts, ";txt=",field.text);
			field.x = 10;
			field.y = 10;
			
			//field.width = (w *defaultScale *72)/25.4;
			field.width = 800;
			//fillRectEx(fx, fy, (w) *defaultScale, 5);
			addChild(field);
			
			print(560,480);
		}
		
		private function test2():void
		{
			var text:TextField = new TextField();
			text.width = 800;
			text.height = 600;
			text.selectable = false;
			text.wordWrap = true;
			
			var textFormat:TextFormat = new TextFormat();
			textFormat.font = "宋体";
			textFormat.size = 24;
			textFormat.color = 0x000000;
			//textFormat.bold = true; 
			//textFormat.letterSpacing="1";
			text.embedFonts = true;
			//text.setTextFormat(textFormat);
			
			text.text="0123456789";
			text.appendText("0123456789\n");
			text.appendText("abcdefghijklmnopqrstuvwxyz\n");
			text.appendText("ABCDEFGHIJKLMNOPQRSTUVWXYZ\n");
			text.appendText("今天很困难，明天更困难，后天很美好。但是绝大多数人死在了明天晚上，如果你不努力的话。\n");
			text.appendText("水的清澈，並非因為它不含雜質，而是在於懂得沉澱；心的通透，不是因為沒有雜念，而是在於明白取捨。\n");
			text.appendText("/ * + - @!#$%^&*()~><‘；、(*^__^*)O(∩_∩)O嗯!<(￣ˇ￣)/[]~(￣▽￣)~*\n");
			text.appendText("The following “Detail Sum(RMB) ” is only a part of “Sum Insured(RMB) ”, there is no\n");
			
			text.setTextFormat(textFormat);
			
			addChild(text);
			
			text = new TextField();
			text.width = 800;
			text.height = 600;
			text.y = 300;
			text.selectable = false;
			text.wordWrap = true;
			text.embedFonts = true;
			
			var textFormat2:TextFormat = new TextFormat();
			textFormat2.font = "粗宋体";
			textFormat2.size = 24;
			textFormat2.color = 0x000000;
			textFormat2.letterSpacing="-0.5";
			//textFormat2.bold = true;

			text.text="0123456789";
			text.appendText("0123456789\n");
			text.appendText("abcdefghijklmnopqrstuvwxyz\n");
			text.appendText("ABCDEFGHIJKLMNOPQRSTUVWXYZ\n");
			text.appendText("今天很困难，明天更困难，后天很美好。但是绝大多数人死在了明天晚上，如果你不努力的话。\n");
			text.appendText("水的清澈，並非因為它不含雜質，而是在於懂得沉澱；心的通透，不是因為沒有雜念，而是在於明白取捨。\n");
			text.appendText("/ * + - @!#$%^&*()~><‘；、(*^__^*)O(∩_∩)O嗯!<(￣ˇ￣)/[]~(￣▽￣)~*\n");
			text.appendText("The following “Detail Sum(RMB) ” is only a part of “Sum Insured(RMB) ”, there is no");
			
			text.setTextFormat(textFormat2);
			
			addChild(text);
			//print(820,1000);
			//trace(toOxString("ˇ▽"));//
		}
		
		import flash.printing.PrintJob;
		import flash.printing.PrintJobOptions;
		import flash.geom.Rectangle;
		public function print(w:int,h:int):void
		{
			var printJob = new PrintJob();
			
			if (printJob.start())
			{
				//for each (var po:Sprite in printObj)
				{
					var printJobOptions:PrintJobOptions = new PrintJobOptions();
					printJobOptions.printAsBitmap = false;
					
					//var options:PrintJobOptions = new PrintJobOptions();
					//options.printAsBitmap = false;
					printJob.addPage(this,new Rectangle(0, 0, w, h),printJobOptions);
				}
				
				//Alert.show("宽："+printJob.paperWidth+";宽："+printJob.paperHeight);
				printJob.send();
			}
			
		}
		/**
		 * 返回16进制unicode码 
		 * @param str 字符
		 * @return 
		 * 
		 */        
		private function toOxString(str:String):String
		{
			var result:String="";
			var len:int=str.length;  
			for (var i:int=0; i < len; i++)  
			{  
				result += "U+" + (str.charCodeAt(i)).toString(16);
				if(i < len - 1)
				{
					result += ",";
				}
			}
			return result;
		}
	}
}