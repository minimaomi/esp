package com.report
{
	import flash.media.Video;
	import flash.utils.ByteArray;
	
	import mx.collections.ArrayList;
	import mx.controls.Alert;
	public class DataParse
	{
		private var reportDraw:ReportDraws;
		private var m_cols:uint = 0;	 //列数
		private var m_rowCur:uint = 0; 
		private var m_colCur:uint = 0;  //解析当前行时，处于第几列
		private var m_lastLeft:uint = 0;//最后一列左边线位置
		private var m_lastRight:uint = 0;//最后一列右边线位置
		private var rowpos_y:uint=0;
		private var rowpos_ry:uint=0;	//行累计
		private var m_posCols:Array; //每列的左边界.

		public function DataParse(rd:ReportDraws)
		{
			this.reportDraw = rd;
			m_posCols = new Array();
		}
	 
		public function dataparse(data:String,leftMargin:int=0,toptMargin:int=0,paperWidth=0,paperHeight=0):void{
			
			//reportDraw.graphics.beginFill(int(0xffffff),1);
			//reportDraw.graphics.drawRect(0,0,paperWidth*0.5,paperHeight*0.5);
			//reportDraw.graphics.endFill();
			reportDraw.setMargin(leftMargin,toptMargin);
			reportDraw.drawRect(0,0,paperWidth,paperHeight);//先画背景，避免横向打印走偏
			 
			var dataList:Array = data.split("\r\n");
			var index:int = data.indexOf("\r\n");
			if(index < 0){
				dataList = data.split("\n");
			}
			 
			for(var i:int=0;i<dataList.length;i++){
				var dataStr:String = dataList[i];
				if(dataStr.indexOf("8,[")==0){
					var wrap:Boolean = isWrap(dataStr);
					if(wrap){
						dataList[i] = dataStr+dataList[i+1];
						dataList.splice(i+1,1);
						i--;
					}
				}
			} 
		 
			// 获取列数，序读取5行数			
			m_colCur = 0;			
			var tmpRow:uint = 0;
			var tmpOffset:uint = 0;			
			
			for(var i:int=0;i<dataList.length;i++){
				var rowstr = dataList[i];
				var index:int = int(rowstr.charAt(0));
				
				if(index==6){
					var sub:String = rowstr.substring(2,rowstr.length);
					if(sub == null)return;
					var sList:Array = sub.split(",");
					tmpOffset = (uint)(sList[2]);
					if (tmpOffset==0) continue;
					
					if (sList[1]==sList[3]){						
						if (tmpOffset>m_lastRight){
							m_lastRight = tmpOffset;
							if(m_posCols.length==0){
								m_posCols.push((uint)(sList[0]));
							}
							m_posCols.push(m_lastRight);
						}
					}
				}else if(index==8){		
					var sub:String = rowstr.substring(2,rowstr.length);
					if(sub == null)return;
					var sList:Array = new Array();
					charsparse(sub,sList);
					var y:String=sList[2];
					
					// for col
					if (rowpos_y<int(y)){
						m_colCur = 1;
						rowpos_y=int(y)
					}else{
						m_colCur++;
					}
					//for row
					if (rowpos_ry<int(y)){
						rowpos_ry = int(y);
						m_rowCur++;
					}
					
					m_cols = (m_cols<m_colCur)?m_colCur:m_cols;
					if (m_cols>m_colCur && m_rowCur>10){
						//reportDraw.setLastCol(m_cols,m_lastLeft, m_lastRight);
						break;
					}
				}				
			}
			
			//解析数据
			rowpos_y = 0;
			m_colCur = 0;
			for(var i:int=0;i<dataList.length;i++){
				var dataStr = dataList[i];
				rowparse(dataStr);
			}			 
		}
		
		 private function isWrap(sub:String):Boolean{			 
			 var chars:String = sub.substring(2,sub.length);
			 var last:int = chars.lastIndexOf(",");
			 var data3:String = chars.substring(last+1,chars.length);
			 var isnum:Boolean =  isNaN(Number(data3));
			 if(isnum){
				 return true;
			 }else{
				 var ch:String = chars.charAt(last-1);

				 if(ch!="]"){
					 return true;
				 }
			 }
			 
			 return false;
		 }
		 
		 private function charsparse(chars:String, list:Array):void{		   
			 var dataList:Array = chars.split(",");
			 var data3 = dataList[dataList.length-1];
			 var data1,data2;
			 var len:int = chars.length;
			 var left:int,right:int;
			 for (var i:int = len-1; i >=0; i--)  {
				 var ch:String = chars.charAt(i);
				 if(ch=="]"){
					 right = i;
				 }
				 if(ch=="["){
					 left =i;
					 break;
				 }
			 }
			 data2 = chars.substring(left+1,right);
			 data1 =  chars.substring(1,left-2);
			 list.push(data1);
			 list.push(data2);
			 list.push(data3);
		 }
		 
		 
		private function rowparse(rowstr:String):void{
			var index:int = int(rowstr.charAt(0));
			var sub:String = rowstr.substring(2,rowstr.length);
			if(sub == null)return;
			var dataList:Array = sub.split(",");
			 
			switch(index){
				case 1://SET_COLOR;
					var color:uint = uint(dataList[0]);
					reportDraw.setColor(color);
					break;
				case 2://SET_FONT
					var fontName:String = dataList[0];
					var fsize:String= dataList[1];
					var isBold:String = dataList[2];
					var isItalic:String = dataList[3];
					reportDraw.setFont(fontName,fsize,isBold,isItalic);
					break;
				case 3://SET_STROKE
					 
					reportDraw.setStroke(dataList);
					break;
				case 4://DRAW_IMAGE
					var imageString:String = dataList[0];
					var x:String = dataList[1];
					var y:String = dataList[2];
					var w:String = dataList[3];
					var h:String = dataList[4];
			 		reportDraw.drawImage(imageString,x,y,w,h);
					break;
				case 5://DRAW_STRING
					var x:String = dataList[0];
					var y:String = dataList[1];
					var str:String = dataList[2];
			 		reportDraw.drawString(x,y,str);
					break;
				case 6://DRAW_LINE
					var x1:String = dataList[0];
					var y1:String = dataList[1];
					var x2:String = dataList[2];
					var y2:String = dataList[3];
		 			reportDraw.drawLine(x1,y1,x2,y2);
					break;
				case 7://FILL_RECT
					var x:String = dataList[0];
					var y:String = dataList[1];
					var w:String = dataList[2];
					var h:String = dataList[3];
					reportDraw.fillRect(x,y,w,h);
					break;
				case 8://DRAW_CHARS	 
					dataList = new Array();
					
					charsparse(sub,dataList);
					var y:String=dataList[2];
					var sList:Array = dataList[1].split(",");
					var right:int = (int)(sList[sList.length-1]);
					var left:int = (int)(sList[0]);
					var width:int = right - left;
					if (width<1) width = 35;
					if (rowpos_y!=int(y)){
						if ( rowpos_y>int(y)){ //单元格中字符串换行问题
							m_colCur++;
						}else if(rowpos_y==0 || m_colCur+1==m_cols){
							m_colCur = 0;
						}else{
							if (right<m_posCols[m_colCur]){ //通过字符串位置判断title中的列少于数据中的列)
								m_colCur = 0;
							}
						}
						rowpos_y=int(y)
					}else if(rowpos_y==int(y)){
						m_colCur++;
					}
					var dright:int = m_posCols[m_colCur+1]-right;
					var dleft:int = left - m_posCols[m_colCur];
					//trace("; yy=", y,"; col=",m_colCur, "; left= ", dleft, "; right= ", dright, "; ",sub);
					//trace("; yy=", y,"; col=",m_colCur, "; w= ", width,  "; ",sub);
					var val = replaceCommaOfString(dataList[0]);
					if (dright>0 && dright <13 && dleft>2){
						reportDraw.drawChars( val,left,(int)(dataList[2]),width,m_colCur, true); 
					}else{
						//trace("m_colCur=", m_colCur, "; posy=",rowpos_y);
			 			reportDraw.drawChars( val,left,(int)(dataList[2]),width,m_colCur, false); 
					}
					break;
				//default:
			}
		}
		
		private function replaceCommaOfString(data:String):String {  
			var dataList:Array = data.split(",");
			var sRet:String = "";
			for (var i:int = 0; i <dataList.length; i++)  {
				if(dataList[i]=="comma"){
					sRet +=  ",";
				}else{
					sRet += dataList[i].replace("（","(").replace("）",")");
				}
			}
			
			while (sRet.indexOf("“")>=0){
				if (isChinese(sRet)){
					break;
				}else{
					sRet = sRet.replace("“","\"").replace("”","\"");
				}
			}
			
			return sRet;
		}  
		
		private function isChinese(newStr: String):Boolean{
			for(var i:int=0;i<newStr.length;i++){
				var char:String = newStr.substr(i,1); 
				if (char=="“" ||char=="”" ) continue;
				
				if(/[^\x00-\xff]/g.test(char)){ //中文加10 
					return true; 
				}else{ 
					; 
				} 
			} 
			return false;
		}
		
		private function getCols(rowstr:String):void{
			var index:int = int(rowstr.charAt(0));
			var sub:String = rowstr.substring(2,rowstr.length);
			if(sub == null)return;
			var dataList:Array = sub.split(",");
						
			switch(index){				
				case 8://DRAW_CHARS	 
					dataList = new Array();
					charsparse(sub,dataList);
					
					parseCol( dataList[0],dataList[1],dataList[2]); 
					break;
				//default:
			}
		}
		
		public function parseCol(cx1:String,cx2:String,y):void{
			if(cx1==null||cx1=="")return;
			if(cx2==null||cx2=="")return;

			var fx:Number=0;
			var fy:Number=0;
			
			var cdotIndex = cx1.indexOf(",");
			var xdotIndex = cx2.indexOf(",");
			if(cdotIndex>=0&&xdotIndex>=0){
				var charsub2:String = cx1.substring(cdotIndex+1,cx1.length);
				var xsub2:String = cx2.substring(xdotIndex+1,cx2.length);
				
				parseCol(charsub2,xsub2,y);
				rowpos_y=int(y);	
			}else{				
				if (rowpos_y==int(y)){
					m_colCur++;
				}else{
					m_colCur=1;
				}
				m_cols = (m_cols<m_colCur)?m_colCur:m_cols;
			}			
		}
		
	}
}