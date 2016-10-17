package otocloud.gw.common;

import java.util.Objects;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import otocloud.common.Command;
import otocloud.common.CommandResult;

public class GatewayAgent {
	private static final Logger log = LoggerFactory.getLogger(GatewayAgent.class.getName());
	
	private int accountid;	
	private Vertx vertx;
	
	public GatewayAgent(Vertx vertx, int accountid) {
		Objects.requireNonNull(vertx, "vertx");
		Objects.requireNonNull(accountid, "accountid");
		this.vertx = vertx;
		this.accountid = accountid;		
	}
	
	public void genInstallPackage(Handler<CommandResult> handler) {
		Command cmd = new Command(accountid, GatewaySchema.GATEWAYMANAGER_GENINSTALL_PACKAGE_ADDRESS);
		cmd.execute(vertx, handler);
	}
	
	public void getGatewayConfig(Handler<CommandResult> handler) {
		Command cmd = new Command(accountid, GatewaySchema.GATEWAYMANAGER_GATEWAY_CONFIG_ADDRESS, GatewaySchema.GATEWAYMANAGER_CONFIG_GET_ACTION);
		cmd.execute(vertx, handler);
	}
	
	public void setGatewayConfig(JsonObject config, Handler<CommandResult> handler) {
		Command cmd = new Command(accountid, GatewaySchema.GATEWAYMANAGER_GATEWAY_CONFIG_ADDRESS, GatewaySchema.GATEWAYMANAGER_CONFIG_SET_ACTION);
		cmd.addParam(GatewaySchema.CONFIG_TAG, config);
		cmd.execute(vertx, handler);
	}
	
	public void getAdapterConfig(String appid, Handler<CommandResult> handler) {
		Command cmd = new Command(accountid, GatewaySchema.GATEWAYMANAGER_ADAPTER_CONFIG_ADDRESS, GatewaySchema.GATEWAYMANAGER_CONFIG_GET_ACTION);
		cmd.addParam(GatewaySchema.APP_ID_TAG, appid);
		cmd.execute(vertx, handler);
	}
	
	public void setAdapterConfig(String appid, JsonObject config, Handler<CommandResult> handler) {
		Command cmd = new Command(accountid, GatewaySchema.GATEWAYMANAGER_ADAPTER_CONFIG_ADDRESS, GatewaySchema.GATEWAYMANAGER_CONFIG_SET_ACTION);
		cmd.setParams(config);
		cmd.addParam(GatewaySchema.APP_ID_TAG, appid);
		cmd.execute(vertx, handler);
	}
	
	public void ping(Handler<CommandResult> handler) {
		Command pingcmd = new Command(accountid, GatewaySchema.GATEWAY_PING_ADDRESS);		
		pingcmd.executeOnGateway(vertx, handler);				
	}
	
	public void queryState(Handler<CommandResult> handler) {
		Command querystatecmd = new Command(accountid, GatewaySchema.GATEWAYMANAGER_GATEWAY_QUERYSTATE_ADDRESS);		
		querystatecmd.execute(vertx, handler);		
	}
	
	public void queryAdapterState(String appid, Handler<CommandResult> handler) {
		Command cmd = new Command(accountid, GatewaySchema.GATEWAYMANAGER_ADAPTER_QUERYSTATE_ADDRESS);
		cmd.addParam(GatewaySchema.APP_ID_TAG, appid);
		cmd.execute(vertx, handler);
	}	
	
	public void deployAdapter(String appid, Handler<CommandResult> handler) {
		Command cmd = new Command(accountid, GatewaySchema.GATEWAYMANAGER_ADAPTER_DEPLOYMENT_ADDRESS, 
									GatewaySchema.DEPLOY_ACTION);
		cmd.addParam(GatewaySchema.APP_ID_TAG, appid);
		
		cmd.execute(vertx, handler);
	}
	
	public void unDeployAdapter(String appid, Handler<CommandResult> handler) {
		Command cmd = new Command(accountid, GatewaySchema.GATEWAYMANAGER_ADAPTER_DEPLOYMENT_ADDRESS, 
									GatewaySchema.UNDEPLOY_ACTION);
		cmd.addParam(GatewaySchema.APP_ID_TAG, appid);
		
		cmd.execute(vertx, handler);
	}
	
	public void setGlobalClientConfig(JsonObject config, Handler<CommandResult> handler) {
		setClientConfig(Configuration.GATEWAY_GLOBAL_CONFIG_TAG, config, handler);
	}
	
	public void setAdapterClientConfig(String appid, JsonObject config, Handler<CommandResult> handler) {
		setClientConfig(GatewaySchema.getAdapterID(appid), config, handler);
	}
	
	private void setClientConfig(String key, JsonObject value, Handler<CommandResult> handler) {
		Command cmd = new Command(accountid, GatewaySchema.GATEWAY_CONFIG_ADDRESS);
		
		cmd.getParams().put(key, value);
		cmd.executeOnGateway(vertx, handler);			
	}
}
