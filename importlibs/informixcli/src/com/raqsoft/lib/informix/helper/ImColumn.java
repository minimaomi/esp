package com.raqsoft.lib.informix.helper;

public class ImColumn {
	public int	  nIndex;	//��������
	public String colName;	//����
	public String colType;	//������������
	public DATA_TYPE nType;	//�������ͱ�ʶ
	public short nColLength;//���ݳ��ȣ�datetime��
	public short nSize;		//���ݳ���(decimalǰ�沿�ֳ���)
	public short nStartSize;//���ݳ���(decimalǰ�沿�ֳ���)
	public short nEndSize;	//decimal���沿�ֳ���
	public Object value;	//���ֵ.(row, col��Ӧ��ֵ)
	
	// dataType: blob,clob, text, byte not support
	public enum DATA_TYPE{
		TYPE_BIGINT,
		TYPE_BIGSERIAL,
		TYPE_BOOLEAN,
		TYPE_CHAR,
		TYPE_VARCHAR,
		
		TYPE_LVARCHAR,		
		TYPE_INTEGER,
		TYPE_DECIMAL,	
		TYPE_LONG,
		TYPE_DOUBLE,
		
		TYPE_FLOAT,
		TYPE_INTERVAL, //time
		TYPE_INT8,
		TYPE_DATE,		
		TYPE_DATETIME,
		
		//TYPE_BINARY, //not support
		TYPE_MONEY,
		TYPE_NCHAR,
		TYPE_SERIAL,		
		TYPE_SMALLFLOAT,		
		TYPE_SMALLINT,		
	}
}
