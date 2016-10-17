package otocloud.gw.common;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.vertx.core.json.JsonObject;
import otocloud.common.Command;

public class GatewaySchema {
	public static final String CONFIG_FILE = System.getenv("OTOCLOUD_GATEWAY_HOME") + File.separator + "config"
			+ File.separator + "gateway.config.json";
	public static final String ADAPTER_INSTALL_PATH = System.getenv("OTOCLOUD_GATEWAY_HOME") + File.separator
			+ "adapters";
	public static final String CONFIG_TAG = "config";
	public static final String APP_ID_TAG = "appid";
	public static final String APP_ID_PREFIX = "otocloud-app-";
	public static final int SYSTEM_ACCOUNT_ID = -1;

	public static final String SERVER_URL_TAG = "serverurl";
	public static final String ACCOUNT_ID_TAG = "accountid";
	public static final String VERSION_TAG = "version";
	public static final String CURRENT_VERSION_TAG = "currentversion";
	public static final String GATEWAY_DOWNLOAD_URL_TAG = "gateway.download.url";
	public static final String GATEWAY_SOURCE_URL_TAG = "gateway.source.url";
	public static final String GATEWAY_TARGET_URL_TAG = "gateway.target.url";
	public static final String GATEWAY_SOURCE_PACKAGE_TAG = "gateway.source.package";
	public static final String GATEWAY_TARGET_PACKAGE_URL_TAG = "installpackagedownloadurl";
	public static final String ADAPTER_DOWNLOAD_URL_TAG = "adapter.download.url";

	public static final String UNKOWN_COMMAND = "unkown";
	public static final String GATEWAY_ADDRESS_TAG = "gatewayaddress";
	public static final String GATEWAY_START_ON_TAG = "startedon";
	public static final String GATEWAY_SHUTDOWN_ON_TAG = "shutteddownon";
	public static final String GATEWAY_STATE_TAG = "state"; // {online,offline,notinstall}
	public static final String GATEWAY_LOCALADDRESS_TAG = "localaddress";
	public static final String GATEWAY_REMOTEADDRESS_TAG = "remoteaddress";

	public static final String ADDRESS_TAG = "address";
	public static final String ACTION_TAG = "action";
	public static final String PARMS_TAG = "parms";
	public static final String COMMAND_TAG = "command";
	public static final String COMMAND_START_ON_TAG = "starton";
	public static final String RESULT_DATA_TAG = "data";
	public static final String RESULT_STATE_TAG = "state";
	public static final String RESULT_ERROR_TAG = "error";
	public static final String RESULT_REPLY_ON_TAG = "replyon";
	public static final String RAW_ILLEGAL_COMMAND_TAG = "rawcommand";
	public static final String RAW_ILLEGAL_COMMANDRESULT_TAG = "rawcommandresult";

	public static final String GATEWAY_ADDRESS_PREFIX = "otocloud-gw-";
	public static final String GATEWAY_PING_ADDRESS = GATEWAY_ADDRESS_PREFIX + "ping";
	public static final String GATEWAY_CONFIG_ADDRESS = GATEWAY_ADDRESS_PREFIX + "config";
	public static final String GATEWAY_ADAPTER_DEPLOYMENT_ADDRESS = GATEWAY_ADDRESS_PREFIX + "adapterdeployment";

	public static final String GATEWAYMANAGER_ADDRESS_PFEFIX = "otocloud-gwm-";
	public static final String GATEWAYMANAGER_REGISTRY_ADDRESS = GATEWAYMANAGER_ADDRESS_PFEFIX + "registry";
	public static final String GATEWAYMANAGER_GATEWAY_QUERYSTATE_ADDRESS = GATEWAYMANAGER_ADDRESS_PFEFIX
			+ "querygatewaystate";
	public static final String GATEWAYMANAGER_GENINSTALL_PACKAGE_ADDRESS = GATEWAYMANAGER_ADDRESS_PFEFIX
			+ "geninstallpackage";
	public static final String GATEWAYMANAGER_CONFIG_ADDRESS = GATEWAYMANAGER_ADDRESS_PFEFIX + "config";
	public static final String GATEWAYMANAGER_ADAPTER_DEPLOYMENT_ADDRESS = GATEWAYMANAGER_ADDRESS_PFEFIX
			+ "adapterdeployment";
	public static final String GATEWAYMANAGER_GATEWAY_CONFIG_ADDRESS = GATEWAYMANAGER_ADDRESS_PFEFIX
			+ "gatewayglobalconfig";
	public static final String GATEWAYMANAGER_ADAPTER_CONFIG_ADDRESS = GATEWAYMANAGER_ADDRESS_PFEFIX + "adapterconfig";
	public static final String GATEWAYMANAGER_ADAPTER_QUERYSTATE_ADDRESS = GATEWAYMANAGER_ADDRESS_PFEFIX
			+ "queryadapterstate";

	public static final String GATEWAYMANAGER_CONFIG_GET_ACTION = "GET";
	public static final String GATEWAYMANAGER_CONFIG_SET_ACTION = "SET";

	public static final String DEPLOYMENT_TARGET_TAG = "target";
	public static final String DEPLOYMENT_OPTIONS_TAG = "options";
	public static final String DEPLOY_ACTION = "deploy";
	public static final String UNDEPLOY_ACTION = "undeploy";
	public static final String DEPLOYMENT_ID_TAG = "deploymentid";

	public static final String ADAPTER_ID_TAG = "adapterid";
	public static final String ADAPTER_ERP_BRAND_TAG = "adaptererpbrand";
	public static final String ADAPTER_ERP_VERSION_TAG = "adaptererpversion";
	public static final String ADAPTER_VERSION_TAG = "adapterversion";
	public static final String ADAPTER_STATE_TAG = "state";
	public static final String ADAPTER_START_ON_TAG = "starton";
	public static final String ADAPTER_SHUTDOWN_ON_TAG = "shutdownon";
	public static final String ADAPTER_CLIENTSYNCED_TAG = "clientSynced";

	public static final String GATEWAY_CONFIG_ERP_BRAND_TAG = "erpbrand";
	public static final String GATEWAY_CONFIG_ERP_VERSION_TAG = "erpversion";
	public static final String GATEWAY_CONFIG_ERP_HOST_TAG = "host";
	public static final String GATEWAY_CONFIG_ERP_PORT_TAG = "port";
	public static final String GATEWAY_CONFIG_ERP_BUSI_CENTER_TAG = "busi_center_code";
	public static final String GATEWAY_CONFIG_ERP_ADMIN_TAG = "user_code";
	public static final String GATEWAY_CONFIG_ERP_ADMIN_PSW_TAG = "password";

	public static String genGatewayAddress(int accountid) {
		return GATEWAY_ADDRESS_PREFIX + String.valueOf(accountid);
	}

	public static final String getAppID(String appcode) {
		return new StringBuilder().append(APP_ID_PREFIX).append(appcode).toString();
	}

	public static final String getAppAddressPrefix(String appid) {
		return new StringBuilder().append(appid).append("-").toString();
	}

	public static final String getAdapterID(String appid) {
		return new StringBuilder().append(appid).append("-ad").toString();
	}

	public static final String getAdapterArtifactID(String adapterid, String erpbrand, String erpversion) {
		return new StringBuilder().append(adapterid.replace(".".charAt(0), "-".charAt(0))).append("-").append(erpbrand)
				.append(erpversion).toString();
	}

	public static final String getAdapterPackageName(String artifactid, String version) {
		return new StringBuilder().append(artifactid).append("-").append(version).append(".jar").toString();
	}

	public static final String getAdapterPackageName(String adapterid, String erpbrand, String erpversion,
			String version) {
		return getAdapterPackageName(getAdapterArtifactID(adapterid, erpbrand, erpversion), version);
	}

	public static final String getAdapterAddressPrefix(String appid) {
		return new StringBuilder().append(getAdapterID(appid)).append("-").toString();
	}

	public static Command genGatewayStartRegistryCommand(int accountid) {
		Command command = new Command(accountid, GATEWAYMANAGER_REGISTRY_ADDRESS);
		return command.setParams(genGatewayStartRegistryParams(accountid));
	}

	private static JsonObject genGatewayStartRegistryParams(int accountid) {
		JsonObject params = new JsonObject();

		params.put(GATEWAY_ADDRESS_TAG, genGatewayAddress(accountid));
		params.put(ACCOUNT_ID_TAG, accountid);
		params.put(GATEWAY_STATE_TAG, GatewayState.ONLINE);
		params.put(GATEWAY_START_ON_TAG, getNow());
		params.put(VERSION_TAG, Configuration.instance().getString(VERSION_TAG));

		return params;
	}

	public static Command genGatewayShutdownRegistryCommand(int accountid) {
		Command command = new Command(accountid, GATEWAYMANAGER_REGISTRY_ADDRESS);
		return command.setParams(genGatewayShutdownRegistryParams(accountid));
	}

	public static JsonObject genGatewayDefaultConfigSchema() {
		JsonObject config = new JsonObject();
		config.put(GATEWAY_CONFIG_ERP_BRAND_TAG, "");
		config.put(GATEWAY_CONFIG_ERP_VERSION_TAG, "");
		config.put(GATEWAY_CONFIG_ERP_HOST_TAG, "");
		config.put(GATEWAY_CONFIG_ERP_PORT_TAG, 8080);
		config.put(GATEWAY_CONFIG_ERP_BUSI_CENTER_TAG, "");
		config.put(GATEWAY_CONFIG_ERP_ADMIN_TAG, "");
		config.put(GATEWAY_CONFIG_ERP_ADMIN_PSW_TAG, "");

		return config;
	}

	private static JsonObject genGatewayShutdownRegistryParams(int accountid) {
		JsonObject parms = new JsonObject();

		parms.put(ACCOUNT_ID_TAG, accountid);
		parms.put(GATEWAY_STATE_TAG, GatewayState.OFFLINE);
		parms.put(GATEWAY_SHUTDOWN_ON_TAG, getNow());

		return parms;
	}

	public static String getNow() {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS").format(new Date());
	}

}
