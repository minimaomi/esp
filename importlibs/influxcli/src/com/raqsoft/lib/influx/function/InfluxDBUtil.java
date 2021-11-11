package com.raqsoft.lib.influx.function;


import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDB.ConsistencyLevel;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
import org.influxdb.dto.Pong;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;

/**
 * InfluxDB���ݿ����Ӳ�����
 * 
 * @author 
 */
public class InfluxDBUtil {
	// �û���
	private String username;
	// ����
	private String password;
	// ���ӵ�ַ
	private String openurl;
	// ���ݿ�
	private String database;

	// ��������
	private String retentionPolicy;
	private InfluxDB influxDB;

	public InfluxDBUtil(String openurl, String database,
			String retentionPolicy,String username, String password) {
		this.openurl = openurl;
		this.database = database;
		this.username = username;
		this.password = password;
		this.retentionPolicy = retentionPolicy == null || retentionPolicy.equals("") ? "autogen" : retentionPolicy;
		influxDbBuild();
	}
	  
	/**
	 * �������ݿ�
	 * 
	 * @param dbName
	 */
	@SuppressWarnings("deprecation")
	public void createDB(String dbName) {
		influxDB.createDatabase(dbName);
	}

	/**
	 * ɾ�����ݿ�
	 * 
	 * @param dbName
	 */
	@SuppressWarnings("deprecation")
	public void deleteDB(String dbName) {
		influxDB.deleteDatabase(dbName);
	}
	
	public String getDBName(){
		return database;
	}

	/**
	 * ���������Ƿ�����
	 * 
	 * @return true ����
	 */
	public boolean ping() {
		boolean isConnected = false;
		Pong pong;
		try {
			pong = influxDB.ping();
			isConnected = (pong != null);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return isConnected;
	}

	/**
	 * ����ʱ�����ݿ� �����������򴴽�
	 * 
	 * @return
	 */
	public InfluxDB influxDbBuild() {
		if (influxDB == null) {
			influxDB = InfluxDBFactory.connect(openurl, username, password);
		}
		try {
//			influxDB.enableGzip();
//			boolean b = influxDB.isGzipEnabled();
//			System.out.println(b);
			// if (!influxDB.databaseExists(database)) {
			// influxDB.createDatabase(database);
			// }
		} catch (Exception e) {
			// �����ݿ�������ö�̬������֧�ִ������ݿ�
			// e.printStackTrace();
		} finally {
			influxDB.setRetentionPolicy(retentionPolicy);
		}
		influxDB.setLogLevel(InfluxDB.LogLevel.NONE);
		return influxDB;
	}

	/**
	 * �����Զ��屣������
	 * 
	 * @param policyName
	 *            ������
	 * @param duration
	 *            ��������
	 * @param replication
	 *            ���渱������
	 * @param isDefault
	 *            �Ƿ���ΪĬ�ϱ�������
	 */
	public void createRetentionPolicy(String policyName, String duration, int replication, Boolean isDefault) {
		String sql = String.format("CREATE RETENTION POLICY \"%s\" ON \"%s\" DURATION %s REPLICATION %s ", policyName,
				database, duration, replication);
		if (isDefault) {
			sql = sql + " DEFAULT";
		}
		this.query(sql);
	}

	/**
	 * ����Ĭ�ϵı�������
	 * 
	 * @param ��������default������������30�죬���渱��������1
	 *            ��ΪĬ�ϱ�������
	 */
	public void createDefaultRetentionPolicy() {
		String command = String.format("CREATE RETENTION POLICY \"%s\" ON \"%s\" DURATION %s REPLICATION %s DEFAULT",
				"default", database, "30d", 1);
		this.query(command);
	}

	/**
	 * ��ѯ
	 * 
	 * @param command
	 *            ��ѯ���
	 * @return
	 */
	public QueryResult query(String command) {
		return influxDB.query(new Query(command, database));
	}

	/**
	 * ����
	 * 
	 * @param measurement
	 *            ��
	 * @param tags
	 *            ��ǩ
	 * @param fields
	 *            �ֶ�
	 */
	public void insert(String measurement, Map<String, String> tags, Map<String, Object> fields, long time,
			TimeUnit timeUnit) {
		Builder builder = Point.measurement(measurement);
		builder.tag(tags);
		builder.fields(fields);
		if (0 != time) {
			builder.time(time, timeUnit);
		}
		influxDB.write(database, retentionPolicy, builder.build());
	}

	/**
	 * ����д����
	 * 
	 * @param batchPoints
	 */
	public void batchInsert(BatchPoints batchPoints) {
		influxDB.write(batchPoints);
		// influxDB.enableGzip();
		// influxDB.enableBatch(2000,100,TimeUnit.MILLISECONDS);
		// influxDB.disableGzip();
		// influxDB.disableBatch();
	}

	/**
	 * ����д������
	 * 
	 * @param database
	 *            ���ݿ�
	 * @param retentionPolicy
	 *            �������
	 * @param consistency
	 *            һ����
	 * @param records
	 *            Ҫ��������ݣ�����BatchPoints.lineProtocol()�ɵõ�һ��record��
	 */
	public void batchInsert(final String database, final String retentionPolicy, final ConsistencyLevel consistency,
			final List<String> records) {
		influxDB.write(database, retentionPolicy, consistency, records);
	}

	/**
	 * ɾ��
	 * 
	 * @param command
	 *            ɾ�����
	 * @return ���ش�����Ϣ
	 */
	public String deleteMeasurementData(String command) {
		QueryResult result = influxDB.query(new Query(command, database));
		return result.getError();
	}

	/**
	 * �ر����ݿ�
	 */
	public void close() {
		influxDB.close();
	}

	/**
	 * ����Point
	 * 
	 * @param measurement
	 * @param time
	 * @param fields
	 * @return
	 */
	public Point pointBuilder(String measurement, long time, TimeUnit unit, Map<String, String> tags, Map<String, Object> fields) {
		Point point = Point.measurement(measurement).time(time, unit).tag(tags).fields(fields).build();
		return point;
	}

}
