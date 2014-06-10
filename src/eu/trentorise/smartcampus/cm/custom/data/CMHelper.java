/*******************************************************************************
 * Copyright 2012-2013 Trento RISE
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either   express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package eu.trentorise.smartcampus.cm.custom.data;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import eu.trentorise.smartcampus.ac.AACException;
import eu.trentorise.smartcampus.ac.SCAccessProvider;
import eu.trentorise.smartcampus.android.common.GlobalConfig;
import eu.trentorise.smartcampus.cm.helper.ImageCacheProvider;
import eu.trentorise.smartcampus.cm.model.CMConstants;
import eu.trentorise.smartcampus.cm.model.PictureProfile;
import eu.trentorise.smartcampus.network.JsonUtils;
import eu.trentorise.smartcampus.network.RemoteConnector;
import eu.trentorise.smartcampus.network.RemoteConnector.CLIENT_TYPE;
import eu.trentorise.smartcampus.profileservice.model.BasicProfile;
import eu.trentorise.smartcampus.protocolcarrier.ProtocolCarrier;
import eu.trentorise.smartcampus.protocolcarrier.common.Constants.Method;
import eu.trentorise.smartcampus.protocolcarrier.custom.FileRequestParam;
import eu.trentorise.smartcampus.protocolcarrier.custom.MessageRequest;
import eu.trentorise.smartcampus.protocolcarrier.custom.MessageResponse;
import eu.trentorise.smartcampus.protocolcarrier.custom.RequestParam;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.ConnectionException;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.ProtocolException;
import eu.trentorise.smartcampus.socialservice.SocialService;
import eu.trentorise.smartcampus.socialservice.SocialServiceException;
import eu.trentorise.smartcampus.socialservice.beans.Community;
import eu.trentorise.smartcampus.socialservice.beans.Entity;
import eu.trentorise.smartcampus.socialservice.beans.EntityType;
import eu.trentorise.smartcampus.socialservice.beans.Group;
import eu.trentorise.smartcampus.socialservice.beans.Limit;
import eu.trentorise.smartcampus.socialservice.beans.Visibility;
import eu.trentorise.smartcampus.storage.DataException;

public class CMHelper {

	private static CMHelper instance = null;

	private static SCAccessProvider accessProvider = null;

	private static Context mContext;

	// private static RemoteStorage remoteStorage = null;
	private ProtocolCarrier mProtocolCarrier = null;

	// private BasicProfileService basicProfileService = null;
	private SocialService socialService = null;

	private static PictureProfile profile;
	private static List<Group> savedGroups;

	private static Map<String, PictureProfile> knownUsers = new HashMap<String, PictureProfile>();

	private static Map<String, String> scEntityType = new HashMap<String, String>();

	private Community scCommunity = null;

	private static String APP_FIST_LAUNCH = "cmfist_launch";

	// private static Map<String, String> types = new HashMap<String, String>();

	public static void init(Context mContext) throws ProtocolException {
		instance = new CMHelper(mContext);
	}

	public static String getTypeByTypeId(String id) {
		return scEntityType.get(id);
	}

	public static boolean isInitialized() {
		return instance != null;
	}

	public static String getAuthToken() throws AACException {
		return getAccessProvider().readToken(mContext);
	}

	private static CMHelper getInstance() throws DataException {
		if (instance == null)
			throw new DataException("CMHelper is not initialized");
		return instance;
	}

	public static SCAccessProvider getAccessProvider() {
		if (accessProvider == null)
			accessProvider = SCAccessProvider.getInstance(mContext);
		return accessProvider;
	}

	protected CMHelper(Context mContext) throws ProtocolException {
		super();
		CMHelper.mContext = mContext;
		this.mProtocolCarrier = new ProtocolCarrier(mContext,
				Constants.APP_TOKEN);
		if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.FROYO) {
			RemoteConnector.setClientType(CLIENT_TYPE.CLIENT_WILDCARD);
		}
		String url = GlobalConfig.getAppUrl(mContext);
		if (!url.endsWith("/"))
			url += "/";
		// basicProfileService = new BasicProfileService(url+"aac");
		socialService = new SocialService(url + "core.social-dev");

		// init entity types
		// initTypes();

	}

	public static Map<String, String> getEntityTypes() {
		return scEntityType;
	}

	public static void initTypes() {
		try {
			String url = GlobalConfig.getAppUrl(mContext);
			if (!url.endsWith("/"))
				url += "/";
			// basicProfileService = new BasicProfileService(url+"aac");
			SocialService socialService = new SocialService(url
					+ "core.social-dev");
			if (socialService != null) {
				for (String typeLabel : CMConstants.types) {
					EntityType type = new EntityType(typeLabel, null);
					try {
						type = socialService.createEntityType(getAuthToken(),
								type);
						scEntityType.put(type.getId(), type.getName());
						Log.i("CmHelper",
								"Loaded entity type " + type.getName());
					} catch (SecurityException e) {
						Log.e("CmHelper",
								"Security exception getting entity type "
										+ typeLabel);
					} catch (SocialServiceException e) {
						Log.e("CmHelper",
								"General exception getting entity type "
										+ typeLabel);
					} catch (AACException e) {
						Log.e("CmHelper",
								"Authentication exception getting entity type "
										+ typeLabel);
					}

				}
			} else {
				Log.w("CMHelper", "socialService null retrieving entity types");
			}
		} catch (ProtocolException e1) {
			Log.e("CMHelper", "exception getting url app");
		}
	}

	public static void destroy() throws DataException {
	}

	public static PictureProfile getProfile() {
		return profile;
	}

	public static PictureProfile ensureProfile()
			throws SecurityException,
			ProtocolException,
			DataException,
			ConnectionException,
			eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException,
			AACException, SocialServiceException {
		return retrieveProfile(false);
	}

	public static boolean profileExists() {
		SharedPreferences appSharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		if (appSharedPrefs == null
				|| appSharedPrefs.getString("profile", null) == null) {
			return false;
		}
		return true;

	}

	@SuppressWarnings("unchecked")
	private static PictureProfile retrieveProfile(boolean forceLoad)
			throws ProtocolException,
			DataException,
			ConnectionException,
			eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException,
			AACException, SecurityException, SocialServiceException {

		SharedPreferences appSharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		String profileJson = null;
		if (forceLoad
				|| appSharedPrefs == null
				|| (profileJson = appSharedPrefs.getString("profile", null)) == null) {
			MessageRequest request = new MessageRequest(
					GlobalConfig.getAppUrl(mContext), Constants.SERVICE_PROFILE
							+ "/current");
			request.setMethod(Method.GET);
			MessageResponse response = getInstance().mProtocolCarrier
					.invokeSync(request, Constants.APP_TOKEN, getAuthToken());
			CMHelper.profile = JsonUtils.toObject(response.getBody(),
					PictureProfile.class);
			checkSCCommunity();
			saveProfileToCache(CMHelper.profile);
			setGroups(readGroups());
		} else if (profileJson != null) {
			CMHelper.profile = JsonUtils.toObject(profileJson,
					PictureProfile.class);
			CMHelper.savedGroups = JsonUtils.toObjectList(
					appSharedPrefs.getString("savedGroups", "[]"), Group.class);
			CMHelper.knownUsers = new HashMap<String, PictureProfile>();
			Map<String, Map<String, Object>> map = JsonUtils.toObject(
					appSharedPrefs.getString("knownUsers", "{}"), Map.class);
			for (String key : map.keySet()) {
				knownUsers.put(key,
						JsonUtils.convert(map.get(key), PictureProfile.class));
			}
		}
		return CMHelper.profile;
	}

	public static boolean syncData(Context c)
			throws SecurityException,
			ProtocolException,
			DataException,
			ConnectionException,
			eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException,
			AACException, SocialServiceException {
		PictureProfile pp = retrieveProfile(true);
		saveProfileToCache(pp);
		return true;
	}

	private static void saveProfileToCache(PictureProfile pp) {
		SharedPreferences appSharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		Editor prefsEditor = appSharedPrefs.edit();
		String json = JsonUtils.toJSON(pp);
		prefsEditor.putString("profile", json);
		prefsEditor.commit();
	}

	private static void saveGroupsToCache(List<Group> groups,
			Map<String, PictureProfile> knownUsers) {
		SharedPreferences appSharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		Editor prefsEditor = appSharedPrefs.edit();
		String json = JsonUtils.toJSON(groups);
		prefsEditor.putString("savedGroups", json);
		json = JsonUtils.toJSON(knownUsers);
		prefsEditor.putString("knownUsers", json);
		prefsEditor.commit();
	}

	public static boolean addToCommunity(String communityId)
			throws SecurityException, SocialServiceException, DataException,
			AACException {
		return getInstance().socialService.addUserToCommunity(getAuthToken(),
				communityId);
	}

	public static boolean removeFromCommunity(String communityId)
			throws SecurityException, SocialServiceException, DataException,
			AACException {
		return getInstance().socialService.removeUserFromCommunity(
				getAuthToken(), communityId);
	}

	public static List<PictureProfile> getPeople(String search)
			throws ProtocolException,
			DataException,
			ConnectionException,
			eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException,
			AACException {
		MessageRequest request = new MessageRequest(
				GlobalConfig.getAppUrl(mContext), Constants.SERVICE_PROFILE);
		try {
			request.setQuery("filter=" + URLEncoder.encode(search, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new DataException(e);
		}
		request.setMethod(Method.GET);
		MessageResponse response = getInstance().mProtocolCarrier.invokeSync(
				request, Constants.APP_TOKEN, getAuthToken());
		return JsonUtils.toObjectList(response.getBody(), PictureProfile.class);
	}

	private static List<Group> readGroups() throws SecurityException,
			SocialServiceException, DataException, AACException {
		return getInstance().socialService.getUserGroups(getAuthToken());
	}

	public static Group saveGroup(Group group) throws SecurityException,
			SocialServiceException, DataException, AACException {
		if (group.getId() != null) {
			if (getInstance().socialService.updateUserGroup(getAuthToken(),
					group) != null) {
				group = getInstance().socialService.getUserGroup(
						getAuthToken(), group.getId());
			}
		} else {
			group = getInstance().socialService.createUserGroup(getAuthToken(),
					group.getName());
		}
		setGroups(readGroups());
		return group;
	}

	public static void deleteGroup(Group group) throws SocialServiceException,
			DataException, AACException {
		getInstance().socialService.deleteUserGroup(getAuthToken(),
				group.getId());
		setGroups(readGroups());
	}

	public static Collection<Community> fetchCommunities()
			throws SecurityException, SocialServiceException, DataException,
			AACException {
		return getInstance().socialService.getCommunities(getAuthToken());
	}

	public static void endAppFailure(Activity activity, int id) {
		Toast.makeText(activity, activity.getResources().getString(id),
				Toast.LENGTH_LONG).show();
		activity.finish();
	}

	public static void showFailure(Activity activity, int id) {
		Toast.makeText(activity, activity.getResources().getString(id),
				Toast.LENGTH_LONG).show();
	}

	public static void uploadPictureProfile(PictureProfile profile,
			byte[] content)
			throws ConnectionException,
			ProtocolException,
			SecurityException,
			DataException,
			eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException,
			AACException, SocialServiceException {

		MessageRequest request = new MessageRequest(
				GlobalConfig.getAppUrl(mContext), Constants.FILE_SERVICE + "/");
		request.setMethod(Method.POST);
		FileRequestParam param = new FileRequestParam();
		param.setContent(content);
		param.setParamName("file");
		param.setFilename("filename");
		param.setContentType("image/jpg");
		request.setRequestParams(Collections
				.<RequestParam> singletonList(param));
		MessageResponse response = getInstance().mProtocolCarrier.invokeSync(
				request, Constants.APP_TOKEN, getAuthToken());
		CMHelper.profile = JsonUtils.toObject(response.getBody(),
				PictureProfile.class);
		saveProfileToCache(CMHelper.profile);
		ImageCacheProvider.store(profile.getUserId(), content);
	}

	public static byte[] downloadFile(long fid)
			throws ConnectionException,
			ProtocolException,
			SecurityException,
			DataException,
			eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException,
			AACException {
		MessageRequest request = new MessageRequest(
				GlobalConfig.getAppUrl(mContext), Constants.FILE_SERVICE + "/"
						+ fid);
		request.setMethod(Method.GET);
		// set requestFile true to get the byte array of file requested
		request.setRequestFile(true);
		MessageResponse response = getInstance().mProtocolCarrier.invokeSync(
				request, Constants.APP_TOKEN, getAuthToken());
		return response.getFileContent();
	}

	public static boolean assignToGroups(PictureProfile user,
			Collection<Group> groups) throws SecurityException,
			SocialServiceException, DataException, AACException {
		List<String> users = Collections.singletonList(user.getUserId());
		if (groups == null) {
			groups = Collections.emptyList();
		}

		for (Group g : savedGroups) {
			if (g.getId().equals(CMConstants.MY_PEOPLE_GROUP_ID))
				continue;
			if (groups.contains(g))
				getInstance().socialService.addUsersToGroup(getAuthToken(),
						g.getId(), users);
			else {
				getInstance().socialService.removeUsersFromGroup(
						getAuthToken(), g.getId(), users);
			}
		}

		setGroups(readGroups());
		return true;
	}

	public static List<Group> getGroups() {
		return savedGroups;
	}

	private static void setGroups(List<Group> groups) throws DataException,
			AACException {
		savedGroups = new ArrayList<Group>();
		knownUsers.clear();
		List<String> users = new ArrayList<String>();
		for (Group g : groups) {
			if (g.getId().equals(CMConstants.MY_PEOPLE_GROUP_ID)) {
				continue;
			}
			if (g.getMembers() != null) {
				for (String mp : g.getMembers()) {
					users.add(mp);
				}
			}
			savedGroups.add(g);
		}
		List<PictureProfile> list = readPictureProfiles(users);
		if (list != null) {
			for (PictureProfile pp : list) {
				if (!pp.getUserId().equals(profile.getUserId())) {
					knownUsers.put(pp.getUserId(), pp);
				}
			}
		}
		saveGroupsToCache(savedGroups, knownUsers);
	}

	/**
	 * @param users
	 * @return
	 * @throws AACException
	 * @throws DataException
	 */
	private static List<PictureProfile> readPictureProfiles(List<String> users)
			throws AACException, DataException {
		try {
			MessageRequest request = new MessageRequest(
					GlobalConfig.getAppUrl(mContext), Constants.SERVICE_PROFILE);
			request.setMethod(Method.GET);
			String query = "";
			for (String u : users) {
				query += "&ids=" + u;
			}
			request.setQuery(query);
			MessageResponse response = getInstance().mProtocolCarrier
					.invokeSync(request, Constants.APP_TOKEN, getAuthToken());
			List<PictureProfile> ppList = JsonUtils.toObjectList(
					response.getBody(), PictureProfile.class);
			return ppList;
		} catch (eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException e) {
			throw new AACException(e);
		} catch (Exception e) {
			throw new DataException(e);
		}
	}

	public static List<Entity> readSharedObjects(int position, int size,
			String type) throws SecurityException, SocialServiceException,
			DataException, AACException {
		Limit limit = new Limit();
		limit.setPage(position);
		limit.setPageSize(size);
		List<Entity> entities = getInstance().socialService
				.getEntitiesSharedWithUser(getAuthToken(), limit);

		checkTypes(entities);
		checkUsers(entities);

		return entities;
	}

	/**
	 * @param entities
	 */
	private static void checkUsers(Iterable<Entity> entities) {
		for (Iterator<Entity> iterator = entities.iterator(); iterator
				.hasNext();) {
			Entity e = iterator.next();
			if (!knownUsers.containsKey(e.getOwner()))
				iterator.remove();
		}
	}

	public static List<Entity> readMyObjects(int position, int size, String type)
			throws SecurityException, SocialServiceException, DataException,
			AACException {
		Limit limit = new Limit();
		limit.setPage(position);
		limit.setPageSize(size);
		List<Entity> entities = getInstance().socialService.getUserEntities(
				getAuthToken(), limit);
		checkTypes(entities);
		return entities;
	}

	/**
	 * @param entities
	 * @throws AACException
	 * @throws DataException
	 * @throws SocialServiceException
	 * @throws SecurityException
	 */
	private static void checkTypes(Iterable<Entity> entities)
			throws SecurityException, SocialServiceException, DataException,
			AACException {
		for (Iterator<Entity> iterator = entities.iterator(); iterator
				.hasNext();) {
			Entity e = iterator.next();
			if (getTypeByTypeId(e.getType()) == null) {
				iterator.remove();
			}
		}
	}

	public static void share(Entity share, String appId, String userToken)
			throws SecurityException,
			SocialServiceException,
			DataException,
			AACException,
			ProtocolException,
			ConnectionException,
			eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException {
		getInstance().socialService.updateUserEntityByUser(userToken, appId,
				share);
		// MessageRequest request = new MessageRequest(
		// GlobalConfig.getAppUrl(mContext), Constants.SERVICE_PROFILE);
		// request.setMethod(Method.POST);
		// request.setBody(JsonUtils.toJSON(share));
		// getInstance().mProtocolCarrier.invokeSync(request,
		// Constants.APP_TOKEN,
		// getAuthToken());
	}

	/**
	 * check the presence of only the SC community in the user communities' list
	 * and add it if not present.
	 * 
	 * @param p
	 * @throws DataException
	 * @throws AACException
	 * @throws SocialServiceException
	 * @throws ConnectionException
	 * @throws ProtocolException
	 * @throws SecurityException
	 */
	private static void checkSCCommunity() throws DataException,
			SecurityException, SocialServiceException, AACException {
		Collection<Community> list = fetchCommunities();
		if (list != null && !list.isEmpty()) {
			getInstance().scCommunity = list.iterator().next();
			addToCommunity(getInstance().scCommunity.getId());
		}
	}

	public static Community getSCCommunity() {
		try {
			return getInstance().scCommunity;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Visibility getEntitySharing(String entityId)
			throws SecurityException, SocialServiceException, DataException,
			AACException {
		Entity entity = getInstance().socialService.getUserEntity(
				getAuthToken(), entityId);
		if (entity == null) {
			return null;
		}
		return entity.getVisibility();
	}

	public static Set<String> getUserGroups(BasicProfile mp) {
		Set<String> res = new HashSet<String>();
		if (getGroups() != null) {

			for (Group g : getGroups()) {
				if (g.getMembers() != null) {
					for (String u : g.getMembers()) {
						if (u.equals(mp.getUserId()))
							res.add(g.getId());
					}
				}
			}
		}
		return res;
	}

	/**
	 * @param socialId
	 * @return
	 */
	public static PictureProfile getPictureProfile(String socialId) {
		return knownUsers.get(socialId);
	}

	/**
	 * @param user_mp
	 * @return
	 */
	public static boolean isKnown(PictureProfile user_mp) {
		return knownUsers.containsKey(user_mp.getUserId());
	}

	/**
	 * @param entityType
	 * @return
	 */
	public static String getEntityTypeName(String entityType) {
		// return types.get(entityType);
		return getTypeByTypeId(entityType);
	}

	/**
	 * @return
	 */
	public static List<PictureProfile> getKnownUsers() {
		if (knownUsers == null || knownUsers.isEmpty())
			return null;
		return new ArrayList<PictureProfile>(knownUsers.values());
	}

	public static boolean isFirstLaunch(Context ctx) {
		return PreferenceManager.getDefaultSharedPreferences(ctx).getBoolean(
				APP_FIST_LAUNCH, true);
	}

	public static void disableFirstLaunch(Context ctx) {
		PreferenceManager.getDefaultSharedPreferences(ctx).edit()
				.putBoolean(APP_FIST_LAUNCH, false).commit();
	}
}
