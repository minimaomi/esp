package com.raqsoft.lib.hdfs.function;

import java.io.IOException;
import java.net.URI;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.compress.CompressionCodecFactory;
import org.apache.hadoop.mapred.JobConf;
import com.raqsoft.dm.Context;
import com.raqsoft.dm.FileObject;
import com.raqsoft.dm.IResource;

public class HdfsClient implements IResource
{
	private FileSystem m_fileSys = null;
	
	public HdfsClient(Context ctx, String[] xmlfiles, String uri)
	{
		try {
			m_fileSys = HdfsConfigure(xmlfiles, uri);
			ctx.addResource(this);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Path getPath()
	{
		return null;
	}
	
	public FileSystem getFileSystem()
	{
		return m_fileSys;
	}
	
	public CompressionCodecFactory getCodecFactory()
	{
		return null;
	}

	public FileSystem HdfsConfigure(String[] xmlfiles, String uri) throws IOException {
		JobConf conf = new JobConf();
		if (xmlfiles!=null){
			for (int i=0; i<xmlfiles.length; i++){
				FileObject fo = new FileObject(xmlfiles[i]);
				if (fo.isExists()){
					conf.addResource(fo.getInputStream());
				}else{
					System.out.println("file: "+ xmlfiles[i] + " not existed");
				}
			}
		}else if(uri!=null){			
			conf.set("fs.default.name",uri); 
		}
		ClassLoader classLoader = HdfsClient.this.getClass().getClassLoader();
		conf.setClassLoader(classLoader);
		conf.set("fs.hdfs.impl",org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());  
		
		FileSystem fs = null;
		if (uri == null){
			fs = FileSystem.get(conf);
		}else{			
			fs = FileSystem.get(URI.create(uri), conf);
		}
		
		return fs;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		try {
			if (m_fileSys!=null){				
				m_fileSys.close();				
				m_fileSys = null;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
