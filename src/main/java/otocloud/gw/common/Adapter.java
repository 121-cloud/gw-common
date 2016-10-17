package otocloud.gw.common;

import java.util.ArrayList;
import java.util.List;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.core.shareddata.LocalMap;
import otocloud.common.Command;
import otocloud.common.CommandResult;
import otocloud.common.MultipleFutures;

public abstract class Adapter extends AbstractVerticle {
	public static final String CONFIG_ERP_CONFIG = "erp_config";
	public static final String CONFIG_ERP_HOST = "host";
	public static final String CONFIG_ERP_PORT = "port";
	public static final String CONFIG_ERP_BUSI_CENTER_CODE = "busi_center_code";
	public static final String CONFIG_ERP_USER_CODE = "user_code";
	public static final String CONFIG_ERP_PASSWORD = "password";

	public static final String PROP_INVOCATION_INFO = "invocation_info";
	public static final String PROP_LOGIN_PARAMETERS = "login_parameters";
	public static final String PROP_SESSION = "session";
	public static final String PROP_USER_ID = "userId";
	public static final String PROP_USER_121_ID = "user_121_id";
	public static final String PROP_SECURITY_TOKEN = "security_token";
	public static final String PROP_PARAMS = "params";

	public static final String KEYOFMAP_SESSION_MAP = "otocloud.app.common.session.map";
	public static final String KEYOFMAP_SESSION_KEYS = "otocloud.app.common.session.keys";
	public static final String KEY_SESSION_KEYS = "keys";

	private static final Logger log = LoggerFactory.getLogger(Adapter.class.getName());

	private static final Configuration _configuration = Configuration.instance();

	public static final String STATE_ADDRESS = "state";
	public static final String STATE_TAG = "state";

	protected HttpClient httpClient;

	public abstract String getAppCode();

	public abstract String getERPBrand();

	public abstract String getERPVersion();

	public abstract void onGatewayGLobalConfigChanged(JsonObject conf);

	public abstract void onConfigChanged(JsonObject conf);

	public abstract AdapterState getState();

	public String getAppID() {
		return GatewaySchema.getAppID(getAppCode());
	}

	public final String getID() {
		return GatewaySchema.getAdapterID(getAppID());
	}

	public final String getAppAddressPrefix() {
		return GatewaySchema.getAppAddressPrefix(getAppID());
	}

	public final String getAddressPrefix() {
		return GatewaySchema.getAdapterAddressPrefix(getAppID());
	}

	public final String getStateQueryAddress() {
		return new StringBuilder().append(getAddressPrefix()).append(STATE_ADDRESS).toString();
	}

	public final int getAccountID() {
		return _configuration.getAccoutID();
	}

	public final JsonObject getGatewayGlobalConfig() {
		return _configuration.getGatewayGlobalConfig();
	}

	public final JsonObject getConfig() {
		return _configuration.getAdapterConfig(getID());
	}

	@Override
	public JsonObject config() {
		return getConfig();
	};

	@Override
	public void init(Vertx vertx, Context context) {
		super.init(vertx, context);
		_configuration.setAdapterConfig(getID(), context.config());
		httpClient = vertx.createHttpClient();
	}

	@Override
	public void start() throws Exception {
		super.start();
		_configuration.addGatewayGlobalConfigChangeHandler(getID(), this::gatewayGlobalConfigChangedHandle);
		_configuration.addAapterConfigChangeHandler(getID(), this::configChangedHandle);

		Command.consumer(vertx, getStateQueryAddress(), this::stateQueryHandle);
	}

	@Override
	public void stop() throws Exception {
		_configuration.removeAdapterConfig(getID());

		super.stop();
	}

	private void gatewayGlobalConfigChangedHandle(JsonObject conf) {
		log.info("Receive Gateway Global Config Changed Notice: " + conf.toString());
		onGatewayGLobalConfigChanged(conf);
	}

	private void configChangedHandle(JsonObject conf) {
		log.info("Receive Config Changed Notice: " + conf.toString());
		onConfigChanged(conf);
	}

	private void stateQueryHandle(Command cmd) {
		CommandResult result = cmd.createResultObject();
		if (cmd.isValid()) {
			result.addData(new JsonObject().put(STATE_TAG, this.getState()));
			result.succeed();
		} else {
			log.error("Illegal Command Format: " + cmd.toString());
		}
		cmd.reply(vertx, result);
	}

	/**
	 * TODO 细化到NC65适配器基类
	 * 
	 * @return
	 */
	protected <T> void invockEIS(String address, JsonObject params, JsonObject session,
			Handler<AsyncResult<T>> invockHandler) {
		JsonObject erpConfig = getERPConfig();
		String host = erpConfig.getString(CONFIG_ERP_HOST);
		int port = erpConfig.getInteger(CONFIG_ERP_PORT);
		// 身份认证
		final Integer userId = session == null ? null : session.getInteger(PROP_USER_ID);
		this.getSession(userId.toString(), getSessionAres -> {
			if (getSessionAres.succeeded()) {
				JsonObject ncSession = getSessionAres.result();
				JsonObject invocationInfo = new JsonObject();
				if (ncSession != null) {
					invocationInfo.put(PROP_SESSION, ncSession);
				} else {
					// TODO 临时方案:通过默认用户登录
					JsonObject loginParameters = createERPLoginParameters();
					invocationInfo.put(PROP_LOGIN_PARAMETERS, loginParameters);
				}
				JsonObject content = new JsonObject().put(PROP_INVOCATION_INFO, invocationInfo).put(PROP_PARAMS,
						params);
				Buffer contentBuffer = AdapterUtils.toBufferWithLength(content);
				@SuppressWarnings("unchecked")
				Handler<HttpClientResponse> responseHandler = (response) -> {
					response.bodyHandler(buf -> {
						String resultStr = "";
						try {
							resultStr = new String(buf.getBytes(), "UTF-8");
						} catch (Exception e) {
							log.error("Unknown charsetName UTF-8", e);
							invockHandler.handle(Future.failedFuture(e));
							return;
						}
						JsonObject result = null;
						try {
							result = new JsonObject(resultStr);
						} catch (DecodeException e) {
							log.error("Exception!", e);
							invockHandler.handle(Future.failedFuture(e));
							return;
						}
						// 身份认证-刷新token
						if (result.containsKey(PROP_SECURITY_TOKEN)) {
							final JsonObject result1 = result;
							String securityToken = result.getString(PROP_SECURITY_TOKEN);
							ncSession.put(PROP_SECURITY_TOKEN, securityToken);
							registerSession(userId.toString(), ncSession, registerAres -> {
								if (registerAres.succeeded()) {
									invockHandler.handle(Future.succeededFuture((T) (result1.getValue("datas"))));
								} else {
									invockHandler.handle(Future.failedFuture(registerAres.cause()));
								}
							});
						} else {
							invockHandler.handle(Future.succeededFuture((T) (result)));
						}
					});
					response.exceptionHandler(t -> {
						invockHandler.handle(Future.failedFuture(t));
					});
				};
				httpClient.post(port, host, address, responseHandler).end(contentBuffer);
			} else {
				invockHandler.handle(Future.failedFuture(getSessionAres.cause()));
			}
		});
	}

	protected void registerSession(String userId, JsonObject session, Handler<AsyncResult<Void>> registerHandler) {
		session.put(PROP_USER_121_ID, userId);
		if (vertx.isClustered()) {
			vertx.sharedData().<String, JsonObject> getClusterWideMap(KEYOFMAP_SESSION_MAP, getSessionMapAres -> {
				if (getSessionMapAres.succeeded()) {
					AsyncMap<String, JsonObject> sessionMap = getSessionMapAres.result();
					sessionMap.put(userId, session, putAres -> {
						if (putAres.succeeded()) {
							vertx.sharedData().<String, List<String>> getClusterWideMap(KEYOFMAP_SESSION_KEYS,
									getSessionKeysMapAres -> {
								if (getSessionKeysMapAres.succeeded()) {
									AsyncMap<String, List<String>> keysMap = getSessionKeysMapAres.result();
									keysMap.get(KEY_SESSION_KEYS, getKeysAres -> {
										if (getKeysAres.succeeded()) {
											List<String> keys = getKeysAres.result();
											if (keys != null) {
												keys.add(userId);
												registerHandler.handle(Future.succeededFuture());
											} else {
												keys = new ArrayList<String>();
												keys.add(userId);
												keysMap.put(KEY_SESSION_KEYS, keys, putKeysAres -> {
													if (putKeysAres.succeeded()) {
														registerHandler.handle(Future.succeededFuture());
													} else {
														registerHandler
																.handle(Future.failedFuture(putKeysAres.cause()));
													}
												});
											}
										} else {
											registerHandler.handle(Future.failedFuture(getKeysAres.cause()));
										}
									});
								} else {
									registerHandler.handle(Future.failedFuture(getSessionKeysMapAres.cause()));
								}
							});
						} else {
							registerHandler.handle(Future.failedFuture(putAres.cause()));
						}
					});
				} else {
					registerHandler.handle(Future.failedFuture(getSessionMapAres.cause()));
				}
			});
		} else {
			LocalMap<String, JsonObject> sessionMap = vertx.sharedData().getLocalMap(KEYOFMAP_SESSION_MAP);
			sessionMap.put(userId, session);
			registerHandler.handle(Future.succeededFuture());
		}
	}

	protected void getSession(String userId, Handler<AsyncResult<JsonObject>> getSessionHandler) {
		if (userId == null) {
			getSessionHandler.handle(Future.succeededFuture(null));
		}
		if (vertx.isClustered()) {
			vertx.sharedData().<String, JsonObject> getClusterWideMap(KEYOFMAP_SESSION_MAP, getSessionMapAres -> {
				if (getSessionMapAres.succeeded()) {
					AsyncMap<String, JsonObject> sessionMap = getSessionMapAres.result();
					sessionMap.get(userId, getSessionAres -> {
						if (getSessionAres.succeeded()) {
							getSessionHandler.handle(Future.succeededFuture(getSessionAres.result()));
						} else {
							getSessionHandler.handle(Future.failedFuture(getSessionAres.cause()));
						}
					});
				} else {
					getSessionHandler.handle(Future.failedFuture(getSessionMapAres.cause()));
				}
			});
		} else {
			LocalMap<String, JsonObject> sessionMap = vertx.sharedData().getLocalMap(KEYOFMAP_SESSION_MAP);
			JsonObject session = sessionMap.get(userId);
			getSessionHandler.handle(Future.succeededFuture(session));
		}
	}

	protected void unRegisterSession(String userId, Handler<AsyncResult<Void>> unRegisterSessionHandler) {
		if (userId == null) {
			unRegisterSessionHandler.handle(Future.succeededFuture());
		}
		if (vertx.isClustered()) {
			vertx.sharedData().<String, JsonObject> getClusterWideMap(KEYOFMAP_SESSION_MAP, getSessionMapAres -> {
				if (getSessionMapAres.succeeded()) {
					AsyncMap<String, JsonObject> sessionMap = getSessionMapAres.result();
					sessionMap.remove(userId, removeAres -> {
						if (removeAres.succeeded()) {
							vertx.sharedData().<String, List<String>> getClusterWideMap(KEYOFMAP_SESSION_KEYS,
									getSessionKeysMapAres -> {
								if (getSessionKeysMapAres.succeeded()) {
									AsyncMap<String, List<String>> keysMap = getSessionKeysMapAres.result();
									keysMap.get(KEY_SESSION_KEYS, getKeysAres -> {
										if (getKeysAres.succeeded()) {
											List<String> keys = getKeysAres.result();
											if (keys != null) {
												keys.remove(userId);
												keysMap.put(KEY_SESSION_KEYS, keys, rePutKeysAres -> {
													if (rePutKeysAres.succeeded()) {
														unRegisterSessionHandler.handle(Future.succeededFuture());
													} else {
														unRegisterSessionHandler
																.handle(Future.failedFuture(rePutKeysAres.cause()));
													}
												});
											} else {
												unRegisterSessionHandler
														.handle(Future.failedFuture("Unexpected session keys!"));
											}
										} else {
											unRegisterSessionHandler.handle(Future.failedFuture(getKeysAres.cause()));
										}
									});
								} else {
									unRegisterSessionHandler.handle(Future.failedFuture(getSessionKeysMapAres.cause()));
								}
							});
						} else {
							unRegisterSessionHandler.handle(Future.failedFuture(removeAres.cause()));
						}
					});
				} else {
					unRegisterSessionHandler.handle(Future.failedFuture(getSessionMapAres.cause()));
				}
			});
		} else {
			LocalMap<String, JsonObject> sessionMap = vertx.sharedData().getLocalMap(KEYOFMAP_SESSION_MAP);
			sessionMap.remove(userId);
			unRegisterSessionHandler.handle(Future.succeededFuture());
		}
	}

	protected void getAllSessions(Handler<AsyncResult<JsonArray>> getHandler) {
		if (vertx.isClustered()) {
			vertx.sharedData().<String, List<String>> getClusterWideMap(KEYOFMAP_SESSION_KEYS,
					getSessionKeysMapAres -> {
						if (getSessionKeysMapAres.succeeded()) {
							AsyncMap<String, List<String>> keysMap = getSessionKeysMapAres.result();
							keysMap.get(KEY_SESSION_KEYS, getKeysAres -> {
								if (getKeysAres.succeeded()) {
									List<String> keys = getKeysAres.result();
									if (keys == null || keys.size() == 0)
										getHandler.handle(Future.succeededFuture(new JsonArray()));
									else {
										vertx.sharedData().<String, JsonObject> getClusterWideMap(KEYOFMAP_SESSION_MAP,
												getSessionMapAres -> {
											if (getSessionMapAres.succeeded()) {
												AsyncMap<String, JsonObject> sessionMap = getSessionMapAres.result();
												// TODO multiFuture
												JsonArray sessions = new JsonArray();
												MultipleFutures getAllSessionFutures = new MultipleFutures();
												keys.forEach(key -> {
													getAllSessionFutures.add(getSessionFuture -> {
														sessionMap.get(key, getSessionAres -> {
															if (getSessionAres.succeeded()) {
																sessions.add(getSessionAres.result());
																getSessionFuture.complete();
															} else {
																getSessionFuture.fail(getSessionAres.cause());
															}
														});
													});
												});
												getAllSessionFutures.setHandler(getAllSessionAres -> {
													if (getAllSessionAres.succeeded()) {
														getHandler.handle(Future.succeededFuture(sessions));
													} else {
														getHandler
																.handle(Future.failedFuture(getAllSessionAres.cause()));
													}
												});
												getAllSessionFutures.start();
											} else {
												getHandler.handle(Future.failedFuture(getSessionMapAres.cause()));
											}
										});
									}
								} else {
									getHandler.handle(Future.failedFuture(getKeysAres.cause()));
								}
							});
						} else {
							getHandler.handle(Future.failedFuture(getSessionKeysMapAres.cause()));
						}
					});
		} else {
			LocalMap<String, JsonObject> sessionMap = vertx.sharedData().getLocalMap(KEYOFMAP_SESSION_MAP);
			JsonArray sessions = new JsonArray(new ArrayList<JsonObject>(sessionMap.values()));
			getHandler.handle(Future.succeededFuture(sessions));
		}
	}

	/**
	 * TODO 细化到NC65适配器基类
	 * 
	 * @return
	 */
	protected JsonObject createERPLoginParameters() {
		JsonObject loginParameters = new JsonObject();
		JsonObject erpConfig = getERPConfig();
		String busiCenterCode = erpConfig.getString(CONFIG_ERP_BUSI_CENTER_CODE);
		String userCode = erpConfig.getString(CONFIG_ERP_USER_CODE);
		String password = erpConfig.getString(CONFIG_ERP_PASSWORD);
		loginParameters.put(CONFIG_ERP_BUSI_CENTER_CODE, busiCenterCode);
		loginParameters.put(CONFIG_ERP_USER_CODE, userCode);
		loginParameters.put(CONFIG_ERP_PASSWORD, password);
		return loginParameters;
	}

	/**
	 * TODO 细化到NC65适配器基类
	 * 
	 * @return
	 */
	protected JsonObject getERPConfig() {
		JsonObject erpConfig = config() == null ? null : config().getJsonObject(CONFIG_ERP_CONFIG);
		if (erpConfig == null) {
			erpConfig = new JsonObject();
			JsonObject globalConfig = this.getGatewayGlobalConfig();
			if (globalConfig != null) {
				erpConfig.put(CONFIG_ERP_HOST, globalConfig.getString(GatewaySchema.GATEWAY_CONFIG_ERP_HOST_TAG));
				erpConfig.put(CONFIG_ERP_PORT, globalConfig.getInteger(GatewaySchema.GATEWAY_CONFIG_ERP_PORT_TAG));
				erpConfig.put(CONFIG_ERP_BUSI_CENTER_CODE,
						globalConfig.getString(GatewaySchema.GATEWAY_CONFIG_ERP_BUSI_CENTER_TAG));
				erpConfig.put(CONFIG_ERP_USER_CODE, globalConfig.getString(GatewaySchema.GATEWAY_CONFIG_ERP_ADMIN_TAG));
				erpConfig.put(CONFIG_ERP_PASSWORD,
						globalConfig.getString(GatewaySchema.GATEWAY_CONFIG_ERP_ADMIN_PSW_TAG));
			}
		}
		return erpConfig;
	}
}
