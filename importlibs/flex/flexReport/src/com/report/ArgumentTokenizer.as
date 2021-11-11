package com.report
{
	public class ArgumentTokenizer
	{
		private   static var LOG:Boolean = false;
		
		public static function log( o:Object ):void {
			if (LOG)
				trace(o);
		}
		
		/**
		 *   为指定字符串构造一个参数分割器。缺省分隔符为','。
		 *
		 *   @param s 指定的字符串
		 */
		//public function ArgumentTokenizer(  s:String ):void
		//{
		//	ArgumentTokenizer(s, ',', false, false, false);
		//}
		
		/**
		 *   为指定字符串构造一个参数分割器。
		 *
		 *   @param s 指定的字符串
		 *   @param delim 指定的分隔符
		 */
		//public function ArgumentTokenizer(  s:String,  delim:String ):void
		//
		//	ArgumentTokenizer(s, delim, false, false, false);
		//}
		
		//public function ArgumentTokenizer(  s:String,  ignoreParentheses:Boolean,
		//	 ignoreBrackets:Boolean,  ignoreBraces:Boolean ):void
		//{
		//	ArgumentTokenizer(s, ',', ignoreParentheses, ignoreBrackets, ignoreBraces);
		//}
		
		public function ArgumentTokenizer(  s:String,  delim:String,  ignoreParentheses:Boolean,
											ignoreBrackets:Boolean,  ignoreBraces:Boolean ):void
		{
			//str = s.trim();
			str = s;
			this.delim = delim;
			this.parentheses = !ignoreParentheses;
			this.brackets = !ignoreBrackets;
			this.braces = !ignoreBraces;
			len = (str == null||str.length==0) ? -1 : str.length;
		}
		
		
		
		/**
		 *   取下一个标记。
		 *
		 *   @return 若字符串为null，则返回null。若hasNext()或hasMoreTokens()为真，则返回
		 *	分割符分割的标记(非空串或空串)，否则返回null。
		 *	若不匹配的引号(单/双)则返回引号后的所有字符
		 *	若引号前转义符\，则此引号不起引号作用
		 *
		 */
		public function next():String
		{
			if(str == null || index > len)
				return null;
			var old:int = index;
			while (index <= len) {
				if ( index == len ) {
					index ++;
					if ( len > 1 && str.charAt(len-1) == delim )
						return count ? null : "";
					break;
				}
				var ch:String = str.charAt(index);
				if ( ch == '\\' ) {
					index += 2;
					continue;
				}
				
				if ( ch == '\"' || ch == '\'' ) {
					var tmp:int = scanQuotation(str, index);
					if (tmp < 0) {
						index = len + 1;
						return count ? null : str.substring(old);
					}
					index = tmp + 1;
					continue;
				}
				if (parentheses && ch == '(' ) {
					var tmp:int = scanParenthesis(str, index,'\\');
					if (tmp < 0) {
						index = len + 1;
						return count ? null : str.substring(old);
					}
					index = tmp + 1;
					continue;
				}
				if (brackets && ch == '[') {
					var tmp:int = scanBracket(str, index);
					if (tmp < 0) {
						index = len + 1;
						return count ? null : str.substring(old);
					}
					index = tmp + 1;
					continue;
				}
				if (braces && ch == '{') {
					var tmp:int =  scanBrace(str, index,'\\');
					if (tmp < 0) {
						index = len + 1;
						return count ? null : str.substring(old);
					}
					index = tmp + 1;
					continue;
				}
				index++;
				if (ch == delim)
					break;
			}
			return count ? null : str.substring( old, index-1 );
		}
		
		
		/**
		 *   取下一个标记。
		 *
		 *   @return 若字符串为null，则返回null。若hasMoreTokens()为真，则返回分割符分割
		 *   的标记(非空串或空串)，否则返回null。
		 */
		public function nextToken():String {
			return next();
		}
		public function  nextElement():Object {
			return next();
		}
		
		
		
		/**
		 *   计算指定字符串中所有未访问标记的个数。
		 *
		 *   @return 字符串中标记数
		 */
		public function countTokens():int
		{
			var j:int = index;
			count = true;
			var i:int;
			for(i = 0; index <= len; i++)
				next();
			
			index = j;
			count = false;
			return i;
		}
		
		/**
		 *   检查是否还有标记
		 *   @return 还有标记返回true，否则返回false
		 */
		public function hasNext():Boolean
		{
			return index <= len;
		}
		
		/**
		 *   检查是否还有标记
		 *   @return 还有标记返回true，否则返回false
		 */
		public function hasMoreTokens():Boolean
		{
			return hasNext();
		}
		
		public function hasMoreElements():Boolean {
			return hasNext();
		}
		
		/**
		 * 搜索下一个匹配的花括号，但引号内的花括号被跳过
		 * @param str 需要搜索括号的原串
		 * @param start 起始位置,即左花括号在原串中的位置
		 * @param escapeChar 转义字符
		 * @return 若找到,则返回匹配的右花括号在原串中的位置,否则返回-1
		 */
		public static function scanBrace( str:String,  start:int,  escapeChar:String):int {
			if (str.charAt(start) != '{')
				return -1;
			
			var len:int = str.length;
			for (var i:int = start + 1; i < len; )  {
				var ch:String = str.charAt(i);
				switch (ch)  {
					case '{':
						i = scanBrace(str, i, escapeChar);
						if (i < 0) return -1;
						i ++;
						break;
					case '\"':
					case '\'':
						var q:int = scanQuotation(str, i, escapeChar);
						if (q < 0) {
							i++;
						} else {
							i = q + 1;
						}
						break;
					case '}':
						return i;
					default:
						if (ch == escapeChar) i ++;
						i ++;
						break;
				}
			}
			return -1;
		}
		
		
		/**
		 * 搜索下一个匹配的中括号,缺省转义字符为\，且引号内的中括号被跳过
		 * @param str 需要搜索括号的原串
		 * @param start 起始位置,即左中括号在原串中的位置
		 * @return 若找到,则返回匹配的右中括号在原串中的位置,否则返回-1
		 */
		public static function scanBracket(str:String,start:int,escapeChar:String = '\\'):int {
			if (str.charAt(start) != '[')
				return -1;
			
			var len:int = str.length;
			for (var i:int = start + 1; i < len; )  {
				var ch:String = str.charAt(i);
				switch (ch)  {
					case '[':
						i = scanBracket(str, i, escapeChar);
						if (i < 0) return -1;
						i ++;
						break;
					case '\"':
					case '\'':
						var q:int = scanQuotation(str, i, escapeChar);
						if (q < 0) {
							i++;
						} else {
							i = q + 1;
						}
						break;
					case ']':
						return i;
					default:
						if (ch == escapeChar) i ++;
						i ++;
						break;
				}
			}
			return -1;
		}
		
		
		
		/**
		 * 搜索下一个匹配的圆括号，但引号内的圆括号被跳过
		 * @param str 需要搜索括号的原串
		 * @param start 起始位置,即左圆括号(在原串中的位置
		 * @param escapeChar 转义字符
		 * @return 若找到,则返回匹配的右圆括号在原串中的位置,否则返回-1
		 */
		public static function scanParenthesis( str:String,  start:int,  escapeChar:String):int {
			if (str.charAt(start) != '(')
				return -1;
			
			var len:int = str.length;
			for (var i:int = start + 1; i < len; )  {
				var ch:String = str.charAt(i);
				switch (ch)  {
					case '(':
						i = scanParenthesis(str, i, escapeChar);
						if (i < 0) return -1;
						i ++;
						break;
					case '\"':
					case '\'':
						var q:int = scanQuotation(str, i, escapeChar);
						if (q < 0) {
							i++;
						} else {
							i = q + 1;
						}
						break;
					case ')':
						return i;
					default:
						if (ch == escapeChar) i ++;
						i ++;
						break;
				}
			}
			return -1;
		}
		
		/**
		 * 搜索下一个匹配的引号'或"
		 * @param str 需要搜索引号的原串
		 * @param start 起始位置,即头一引号在原串中的位置
		 * @param escapeChar 转义字符
		 * @return 若找到,则返回匹配的引号在原串中的位置,否则返回-1
		 */
		public static function scanQuotation(str:String,start:int,escapeChar:String = '\\'):int {
			var quote:String = str.charAt(start);
			if (quote != '\"' && quote != '\'') return -1;
			var idx:int = start + 1, len = str.length;
			while (idx < len) {
				var ch:String = str.charAt(idx);
				if (ch == escapeChar)
					idx += 2;
				else if ( ch == quote )
					return idx;
				else
					idx ++;
			}
			return -1;
		}
		
		private var delim:String = ',';
		private var index:int;
		private var str:String;
		private var len:int;
		private var count:Boolean;
		private var parentheses:Boolean = false;
		private var brackets:Boolean = false;
		private var braces:Boolean = false;
	}
}