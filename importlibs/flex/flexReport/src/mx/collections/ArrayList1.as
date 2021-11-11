package mx.collections
{
	public class ArrayList1
	{
		private var array:Array = new Array();
		public function ArrayList1()
		{
		}
		public function getItemAt(i:int):Object{
			return array[i];
		}
		public function setItemAt(obj:Object,i:int):void{
			array[i] = obj;
		}
		public function addItem(obj:Object):void{
			array.push(obj);
		}
		public function addItemAt(obj:Object,i:int):void{
			
			 if(i<0){
				 return;
			 }else if(i>=array.length){
				 array.push(obj);
			 }
			 else if(i>=0&&i<array.length){
				 var tmp:Array = new Array();
				 for(var r:int=0;r<i;r++){
					 tmp.push(array[r]);
				 }
				 tmp.push(obj);
				 for(var r:int=i;r<array.length;r++){
					 
					 tmp.push(array[r]);
				 }
				 array = tmp;
			 }
		}
		public   function  get length():int{
			return array.length;
		}
		public function toString():String{
			return array.toString();
		}
		public function removeAll():void{
			array.splice(0,array.length);
			array = null;
			array = new Array();
		}
		public function getItemIndex(obj:Object):int{
			
			for(var r:int=0;r<array.length;r++){
				if(array[r]==obj){
					return r;
				}
			}
			return -1;
		}
	}
	
	
}