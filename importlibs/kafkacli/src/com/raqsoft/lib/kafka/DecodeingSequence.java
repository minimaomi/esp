package com.raqsoft.lib.kafka;

import java.util.Map;  
import org.apache.kafka.common.serialization.Deserializer;

import com.raqsoft.common.Logger;
import com.raqsoft.dm.Sequence;  
  
public class DecodeingSequence implements Deserializer<Object> {  
  
    @Override  
    public void configure(Map<String, ?> configs, boolean isKey) {  
    }  
  
    @Override  
    public Object deserialize(String topic, byte[] data) {  
    	Sequence seq = null;
    	try {
    		seq = new Sequence();
			seq.fillRecord(data);
		} catch (Exception e) {
			Logger.error(e.getStackTrace());
		}
        return seq;  
    }  
  
    @Override  
    public void close() {  
          
    }  
}  