package com.raqsoft.lib.hdfs.function;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionCodecFactory;

import com.raqsoft.common.RQException;
import com.raqsoft.dm.IFile;
import com.raqsoft.dm.RandomOutputStream;

public class HdfsFileImpl implements IFile {
	private FileSystem hdfs;
	private String fileName;
	private CompressionCodec codec;
	
	/*
	public static boolean isHDFSFile( String fName ) {
		return fName.startsWith( "hdfs://" );
	}*/
	
	public HdfsFileImpl() {}
	
	public HdfsFileImpl( String fileName ) {
		init( fileName );
	}
	
	public HdfsFileImpl(HdfsClient client, String uri)
	{
		hdfs = client.getFileSystem();
		initFile(uri);
	}
	
	public boolean deleteDir(){
		return true;
	}
	
	public void setFileName( String fileName ) {
		init( fileName );
	}
	
	private void init( String fileName ) {
		Configuration conf = new Configuration();
		try {
			hdfs = FileSystem.get(conf);
		}catch( Exception e ) {
			throw new RQException( e );
		}
		this.fileName = fileName;
        Path inputPath = new Path( fileName );
        //System.out.println( conf.get( "io.compression.codecs" ) );
        CompressionCodecFactory factory = new CompressionCodecFactory( conf );
        codec = factory.getCodec( inputPath );
	}
	
	private void initFile( String fileName ) {		
		this.fileName = fileName;
        Path inputPath = new Path( fileName );
        //System.out.println( conf.get( "io.compression.codecs" ) );
        CompressionCodecFactory factory = new CompressionCodecFactory( hdfs.getConf() );
        codec = factory.getCodec( inputPath );
	}
	
	public boolean exists() {
		try {
			return hdfs.exists( new Path( fileName ) );
		}
		catch( Throwable t ) {
			throw new RQException( t );
		}
	}
	
	/**
	 * 返回文件长度
	 * @return
	 */
	public long size() {
		try {
			if( !hdfs.exists( new Path( fileName ) ) ) return 0;
			return hdfs.getFileStatus( new Path( fileName ) ).getLen();
		}
		catch( Throwable t ) {
			throw new RQException( t );
		}
	}

	public long lastModified() {
		try {
			return hdfs.getFileStatus( new Path( fileName ) ).getModificationTime();
		}
		catch( Throwable t ) {
			throw new RQException( t );
		}
	}
	
	public boolean move( String newName, String opt ) {
		try {
			if( opt != null && opt.trim().length() > 0 ) {
				throw new Exception( "Not support the option " + opt );
			}
			return hdfs.rename( new Path( fileName ), new Path( newName ) );
		}
		catch( Throwable t ) {
			throw new RQException( t );
		}
	}
	
	public boolean delete() {
		try {
			return hdfs.delete( new Path( fileName ), true );
		}
		catch( Throwable t ) {
			throw new RQException( t );
		}
	}
	
	/**
	 * 打开文件输入流
	 * @return 返回org.apache.hadoop.fs.FSDataInputStream,此流对象有seek(long desired)方法定位指针位置 
	 */
	public InputStream getInputStream() {
   		try {
   			if( codec != null ) {
   				return codec.createInputStream( hdfs.open( new Path( fileName ) ) );
   			}
   			Path pt = new Path( fileName );
   			InputStream ism = hdfs.open( pt);
			return ism;
		}
		catch( Throwable t ) {
			throw new RQException( t );
		}
	}
	
	/**
	 * 打开文件输出流，如果文件不存在则创建并打开，存在则打开
	 * @param isAppend 是否以追加方式打开
	 * @return
	 */
	public OutputStream getOutputStream( boolean isAppend ) {
		try {
			Path p = new Path( fileName ).getParent();
			if( !hdfs.exists( p ) ) hdfs.mkdirs( p );
			OutputStream os = null;
			if( !isAppend ) os = hdfs.create( new Path( fileName ) );
			else os = hdfs.append( new Path( fileName ) );
			if( codec != null ) {
				os = codec.createOutputStream( os );
			}
			return os;
		}
		catch( Throwable t ) {
			throw new RQException( t );
		}
	}
	
	public String createTempFile( String prefix ) {
		try {
			Path path = new Path( fileName );
			Path p = path;
			String suffix = "";
			int pos = path.getName().lastIndexOf( "." );
			if( pos > 0 ) suffix = path.getName().substring( pos + 1 );
			if( suffix.length() > 0 ) {
				p = path.getParent();
			}
			while( true ) {
				long sj = System.currentTimeMillis() % 10000;
				String fn = p.toString() + "/" + prefix + sj;
				if( suffix.length() > 0 ) fn = fn + "." + suffix;
				if( !hdfs.exists( new Path( fn ) ) ) return fn;
			}
		}catch( Exception e ) {
			throw new RQException( e );
		}		
	}
	
	public static void main( String[] args ) {
		try{
			//HDFSFile file = new HDFSFile( "hdfs://192.168.0.204:9000/user/root/log.txt" );
			//System.out.println( file.exists() );
			//System.out.println( new HDFSFile("hdfs://192.168.0.204:9000/user/root/h.txt").createTempFile( "sjr" ) );
			//writeFile( "D:/workspace/hadooptest/in/5.txt", "hdfs://192.168.0.204:9000/user/root/test.gz" );
			readFile( "hdfs://192.168.0.76:9000/user/hive/lxw1235.txt" );
		}
		catch( Exception e ) {
			e.printStackTrace();
		}
	}
	
	private static void writeFile( String localFile, String fname ) throws Exception {
		BufferedReader br = null;
		PrintWriter pw = null;
		try {
			br = new BufferedReader( new InputStreamReader( new FileInputStream( localFile ) ) );
			HdfsFileImpl file = new HdfsFileImpl( fname );
			pw = new PrintWriter( new OutputStreamWriter( file.getOutputStream( false ) ) );
			String tmp = null;
			while( ( tmp = br.readLine() ) != null ) {
				pw.println( tmp );
			}
		}
		finally {
			br.close();
			pw.close();
		}
	}

	private static void readFile( String fname ) throws Exception {
		BufferedReader br = null;
		try {
			HdfsFileImpl file = new HdfsFileImpl( fname );
			br = new BufferedReader( new InputStreamReader( file.getInputStream() ) );
			String tmp = null;
			while( ( tmp = br.readLine() ) != null ) {
				System.out.println( tmp );
			}
		}
		finally {
			br.close();
		}
	}

	public RandomOutputStream getRandomOutputStream(boolean isAppend) {
		// TODO Auto-generated method stub
		throw new RQException( "不能随机写hdfs文件" );
		//return null;
	}
	
	//list all files
    public FileStatus[] listFiles(String dirName) {
        try {
        	String sPath = fileName + "/" + dirName;
        	Path f = new Path(sPath);
        	
			return hdfs.listStatus(f);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        return null;         
    }

   
}
