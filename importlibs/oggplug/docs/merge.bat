set EXECJAVA="D:\app\java\jdk1.8.0_121\bin\java"
rem �������Ϊ��·�� "�Զ����ֶ�ģʽ�Ȳ���"
%EXECJAVA% -Xms128m -Xmx1024m  -cp .;dirprm;dirprm/fastjson-1.2.7.jar;dirprm/oggplug.jar;dirprm/icu4j_3_4_5.jar;dirprm/dm.jar -Dlog4j.configuration=log4j.properties; com.raqsoft.lib.ogg.MergeFile %1 %2
