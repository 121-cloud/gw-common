package otocloud.gw.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.Scanner;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import otocloud.common.MultipleFutures;

public class Configuration extends JsonObject {
	private static final Logger log = LoggerFactory.getLogger(Configuration.class.getName());
	
	private boolean isvalid = true;
	private static final Configuration _instance = new Configuration();
	private Map<String, Handler<JsonObject>> globalListeners = new Hashtable<String, Handler<JsonObject>>();
	private Map<String, Handler<JsonObject>> adapterListeners = new Hashtable<String, Handler<JsonObject>>();
	
	public static final String GATEWAY_GLOBAL_CONFIG_TAG = "globalconfig";
	public static final String CONFIG_KEY_TAG = "key";
	public static final String CONFIG_VALUE_TAG = "value";
	
	private Configuration() {
		JsonObject config = loadConfig();
		if (config != null) {
			this.mergeIn(config);
		}
		else {
			isvalid = false;
		}			
	}
	
	public static Configuration instance() {		
		return _instance;
	}
	
	public void addGatewayGlobalConfigChangeHandler(String adapterid, Handler<JsonObject> handler) {
		globalListeners.put(adapterid, handler);
	}
	
	public void addAapterConfigChangeHandler(String adapterid, Handler<JsonObject> handler) {
		adapterListeners.put(adapterid, handler);		
	}
	
	public int getAccoutID() {
		return this.getInteger(GatewaySchema.ACCOUNT_ID_TAG);
	}
	
	public Configuration setGatewayGlobalConfig(JsonObject conf) {
		this.put(GATEWAY_GLOBAL_CONFIG_TAG, conf);
		MultipleFutures notifytasks = new MultipleFutures();
		globalListeners.forEach((key,handler) -> {
			notifytasks.add(fut -> {
				handler.handle(conf);
				fut.complete();
			});
		});
		notifytasks.start();
		return this;
	}
	
	public JsonObject getGatewayGlobalConfig() {
		return this.getJsonObject(GATEWAY_GLOBAL_CONFIG_TAG);
	}
	
	public Configuration setAdapterConfig(String adapterid, JsonObject conf) {
		this.put(adapterid, conf);
		if (adapterListeners.containsKey(adapterid)) {
			Future<Void> fut = Future.future();
			fut.setHandler(ar -> {
				adapterListeners.get(adapterid).handle(conf);
			});
			fut.complete();
		}
		return this;
	}
	
	public JsonObject getAdapterConfig(String adapterid) {
		return this.getJsonObject(adapterid);
	}
	
	public void removeAdapterConfig(String adapterid) {
		this.adapterListeners.remove(adapterid);
		this.globalListeners.remove(adapterid);
		this.remove(adapterid);
	}
	
	public boolean isValid() {
		return isvalid;
	}
	
	private JsonObject loadConfig()
    {
        JsonObject conf = null;        
        
        try (Scanner scanner = new Scanner(new File(GatewaySchema.CONFIG_FILE)).useDelimiter("\\A"))
        {
            String sconf = scanner.next();
            try 
            {
            	conf = new JsonObject(sconf);
            } 
            catch (DecodeException e) 
            {
            	log.error("Configuration file " + GatewaySchema.CONFIG_FILE + " does not contain a valid JSON object");
            	return null;
            }
            finally
            {
            	scanner.close();
            }
        } 
        catch (FileNotFoundException e) 
        {
        	log.info("gateway.config.json file not found!");        	
        	return null;
        }
        
        return conf;
    }
}
