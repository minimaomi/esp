set EXECJAVA="D:\app\java\jdk1.8.0_121\bin\java"
rem 输入参数为：路径 "自动、手动模式等参数"
%EXECJAVA% -Xms128m -Xmx1024m  -cp .;dirprm;dirprm/fastjson-1.2.7.jar;dirprm/oggplug.jar;dirprm/icu4j_3_4_5.jar;dirprm/dm.jar -Dlog4j.configuration=log4j.properties; com.raqsoft.lib.ogg.MergeFile %1 %2
