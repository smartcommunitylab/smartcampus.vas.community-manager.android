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
import android.widget.Toast;
import eu.trentorise.smartcampus.ac.AACException;
import eu.trentorise.smartcampus.ac.SCAccessProvider;
import eu.trentorise.smartcampus.android.common.GlobalConfig;
import eu.trentorise.smartcampus.cm.model.CMConstants;
import eu.trentorise.smartcampus.cm.model.PictureProfile;
import eu.trentorise.smartcampus.network.JsonUtils;
import eu.trentorise.smartcampus.network.RemoteConnector;
import eu.trentorise.smartcampus.network.RemoteConnector.CLIENT_TYPE;
import eu.trentorise.smartcampus.protocolcarrier.ProtocolCarrier;
import eu.trentorise.smartcampus.protocolcarrier.common.Constants.Method;
import eu.trentorise.smartcampus.protocolcarrier.custom.FileRequestParam;
import eu.trentorise.smartcampus.protocolcarrier.custom.MessageRequest;
import eu.trentorise.smartcampus.protocolcarrier.custom.MessageResponse;
import eu.trentorise.smartcampus.protocolcarrier.custom.RequestParam;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.ConnectionException;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.ProtocolException;
import eu.trentorise.smartcampus.social.model.Communities;
import eu.trentorise.smartcampus.social.model.Community;
import eu.trentorise.smartcampus.social.model.Entities;
import eu.trentorise.smartcampus.social.model.Entity;
import eu.trentorise.smartcampus.social.model.Group;
import eu.trentorise.smartcampus.social.model.Groups;
import eu.trentorise.smartcampus.social.model.ShareVisibility;
import eu.trentorise.smartcampus.social.model.User;
import eu.trentorise.smartcampus.socialservice.SocialService;
import eu.trentorise.smartcampus.socialservice.SocialServiceException;
import eu.trentorise.smartcampus.storage.DataException;

public class CMHelper {

	private static CMHelper instance = null;

	private static SCAccessProvider accessProvider = null;

	private Context mContext;

//	private static RemoteStorage remoteStorage = null;
	private ProtocolCarrier mProtocolCarrier = null;

//	private BasicProfileService basicProfileService = null;
	private SocialService socialService = null;
	
	private static PictureProfile profile;
	private static List<Community> communities = null;
	private static List<Group> savedGroups;

	private static Map<String, PictureProfile> knownUsers = new HashMap<String, PictureProfile>();

	private Community scCommunity = null;
	
//	private static Map<String, String> types = new HashMap<String, String>();
	
	public static void init(Context mContext) throws ProtocolException {
		instance = new CMHelper(mContext);
	}

	public static boolean isInitialized() {
		return instance != null;
	}
	
	public static String getAuthToken() throws AACException {
		return accessProvider.readToken(instance.mContext);
	}

	public static void setProfile(PictureProfile profile) {
		CMHelper.profile = profile;
		CMHelper.communities = Collections.singletonList(instance.scCommunity);
	}

	public static PictureProfile getProfile() {
		return profile;
	}
	
	public static List<Community> getCommunities() {
		return communities;
	}

	private static CMHelper getInstance() throws DataException {
		if (instance == null)
			throw new DataException("DTHelper is not initialized");
		return instance;
	}

	public static SCAccessProvider getAccessProvider() {
		if (accessProvider == null) accessProvider = SCAccessProvider.getInstance(instance.mContext);
		return accessProvider;
	}

	protected CMHelper(Context mContext) throws ProtocolException {
		super();
		this.mContext = mContext;
		this.mProtocolCarrier = new ProtocolCarrier(mContext, Constants.APP_TOKEN);
		if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.FROYO) {
			RemoteConnector.setClientType(CLIENT_TYPE.CLIENT_WILDCARD);
		}
		String url = GlobalConfig.getAppUrl(mContext);
		if (!url.endsWith("/")) url += "/";
//		basicProfileService = new BasicProfileService(url+"aac");
		socialService = new SocialService(url+"core.social");
	}

	public static void destroy() throws DataException {
	}

	public static PictureProfile retrieveProfile() throws ProtocolException, DataException, ConnectionException, eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException, AACException, SecurityException, SocialServiceException {
		MessageRequest request = new MessageRequest(GlobalConfig.getAppUrl(getInstance().mContext), Constants.SERVICE_PROFILE + "/current");
		request.setMethod(Method.GET);
		MessageResponse response = getInstance().mProtocolCarrier.invokeSync(request, Constants.APP_TOKEN, getAuthToken());
		PictureProfile pp = JsonUtils.toObject(response.getBody(), PictureProfile.class);
		checkSCCommunity();
		setGroups(readGroups());
		return pp;
	}

	public static boolean addToCommunity(String communityId) throws SecurityException, SocialServiceException, DataException, AACException {
		return getInstance().socialService.addUserToCommunity(getAuthToken(), communityId); 
	}

	public static boolean removeFromCommunity(String communityId)throws SecurityException, SocialServiceException, DataException, AACException {
		return getInstance().socialService.removeUserFromCommunity(getAuthToken(), communityId); 
	}
	public static List<PictureProfile> getPeople(String search) throws ProtocolException, DataException, ConnectionException, eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException, AACException {
		MessageRequest request = new MessageRequest(GlobalConfig.getAppUrl(getInstance().mContext), Constants.SERVICE_PROFILE);
		try {
			request.setQuery("filter="+URLEncoder.encode(search,"UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new DataException(e);
		}
		request.setMethod(Method.GET);
		MessageResponse response = getInstance().mProtocolCarrier.invokeSync(request, Constants.APP_TOKEN, getAuthToken());
		return JsonUtils.toObjectList(response.getBody(), PictureProfile.class);
	}

	private static List<Group> readGroups() throws SecurityException, SocialServiceException, DataException, AACException {
		Groups groups = getInstance().socialService.getUserGroups(getAuthToken());
		if (groups == null || groups.getContent() == null) return new ArrayList<Group>();
		return groups.getContent();
	}	

	public static Group saveGroup(Group group) throws SecurityException, SocialServiceException, DataException, AACException  {
		if (group.getSocialId() != null) {
			if (getInstance().socialService.updateUserGroup(getAuthToken(), group)) {
				group = getInstance().socialService.getUserGroup(group.getSocialId(), getAuthToken());
			} 
		} else {
			group = getInstance().socialService.createUserGroup(getAuthToken(), group.getName());
		}
		setGroups(readGroups());
		return group;
	}

	public static void deleteGroup(Group group) throws SocialServiceException, DataException, AACException {
		getInstance().socialService.deleteUserGroup(getAuthToken(), group.getSocialId());
		setGroups(readGroups());
	}

	public static Collection<Community> fetchCommunities() throws SecurityException, SocialServiceException, DataException, AACException  {
		Communities comms = getInstance().socialService.getCommunities(getAuthToken());
		if (comms == null || comms.getContent() == null) return Collections.emptyList();
		return comms.getContent();
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

	public static void uploadPictureProfile(PictureProfile profile, byte[] content)
			throws ConnectionException, ProtocolException, SecurityException,
			DataException, eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException, AACException {

		MessageRequest request = new MessageRequest(GlobalConfig.getAppUrl(getInstance().mContext), Constants.FILE_SERVICE + "/");
		request.setMethod(Method.POST);
		FileRequestParam param = new FileRequestParam();
		param.setContent(content);
		param.setParamName("file");
		param.setFilename("filename");
		param.setContentType("image/jpg");
		request.setRequestParams(Collections.<RequestParam> singletonList(param));
		MessageResponse response = getInstance().mProtocolCarrier.invokeSync(request, Constants.APP_TOKEN, getAuthToken());
		CMHelper.profile = JsonUtils.toObject(response.getBody(), PictureProfile.class);
	}

	public static byte[] downloadFile(long fid) throws ConnectionException,
			ProtocolException, SecurityException, DataException, eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException, AACException {
		MessageRequest request = new MessageRequest(GlobalConfig.getAppUrl(getInstance().mContext),
				Constants.FILE_SERVICE + "/" + fid);
		request.setMethod(Method.GET);
		// set requestFile true to get the byte array of file requested
		request.setRequestFile(true);
		MessageResponse response = getInstance().mProtocolCarrier.invokeSync(request, Constants.APP_TOKEN, getAuthToken());
		return response.getFileContent();
	}

	public static boolean assignToGroups(User user, Collection<Group> groups) throws SecurityException, SocialServiceException, DataException, AACException {
		List<String> users = Collections.singletonList(user.getSocialId());
		if (groups == null) groups = Collections.emptyList();
		for (Group g : savedGroups) {
			if (g.getSocialId().equals(CMConstants.MY_PEOPLE_GROUP_ID)) continue;
			if (groups.contains(g)) getInstance().socialService.addUsersToGroup(g.getSocialId(), users, getAuthToken());
			else getInstance().socialService.removeUsersFromGroup(g.getSocialId(), users, getAuthToken());
		}
		if (groups.isEmpty()) {
			getInstance().socialService.removeUsersFromGroup(CMConstants.MY_PEOPLE_GROUP_ID, users, getAuthToken());
		}
		
		setGroups(readGroups());
		return true;
	}

	public static List<Group> getGroups() {
		return savedGroups;
	}

	private static void setGroups(List<Group> groups) throws DataException, AACException {
		savedGroups = new ArrayList<Group>();
		knownUsers.clear();
		List<String> users = new ArrayList<String>();
		for (Group g : groups) {
			if (g.getSocialId().equals(CMConstants.MY_PEOPLE_GROUP_ID)) {
				continue;
			} 
			if (g.getUsers() != null) {
				for (User mp : g.getUsers()) {
					users.add(mp.getId());
				}
			}
			savedGroups.add(g);
		}
		List<PictureProfile> list = readPictureProfiles(users);
		if (list != null) {
			for (PictureProfile pp : list) {
				knownUsers.put(pp.getSocialId(), pp);
			}
		}
	}

	/**
	 * @param users
	 * @return
	 * @throws AACException 
	 * @throws DataException 
	 */
	private static List<PictureProfile> readPictureProfiles(List<String> users) throws AACException, DataException {
		try {
			MessageRequest request = new MessageRequest(GlobalConfig.getAppUrl(getInstance().mContext), Constants.SERVICE_PROFILE);
			request.setMethod(Method.GET);
			String query = "";
			for (String u : users) {
				query += "&ids="+u;
			}
			request.setQuery(query);
			MessageResponse response = getInstance().mProtocolCarrier.invokeSync(request, Constants.APP_TOKEN, getAuthToken());
			List<PictureProfile> ppList = JsonUtils.toObjectList(response.getBody(), PictureProfile.class);
			return ppList;
		} catch (eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException e) {
			throw new AACException(e);
		} catch (Exception e) {
			throw new DataException(e);
		}
	}

	public static List<Entity> readSharedObjects(ShareVisibility shareVisibility, int position, int size, String type) throws SecurityException, SocialServiceException, DataException, AACException {
		Entities entities = getInstance().socialService.getEntitiesSharedWithUser(getAuthToken(), shareVisibility, position, size, CMConstants.getTypeIdByType(type));
		if (entities == null || entities.getContent() == null) return Collections.emptyList();
		else {
			checkTypes(entities);
			checkUsers(entities);
		}
		return entities.getContent();
	}

	/**
	 * @param entities
	 */
	private static void checkUsers(Entities entities) {
		for (Iterator<Entity> iterator = entities.getContent().iterator(); iterator.hasNext();) {
			Entity e = iterator.next();
			if (!knownUsers.containsKey(e.getUser().getSocialId())) iterator.remove();
		}
	}

	public static List<Entity> readMyObjects(int position, int size, String type) throws SecurityException, SocialServiceException, DataException, AACException  {
		Entities entities = getInstance().socialService.getUserEntities(getAuthToken(), position, size, CMConstants.getTypeIdByType(type));
		if (entities == null || entities.getContent() == null) return Collections.emptyList();
		else checkTypes(entities);
		return entities.getContent();
	}

	/**
	 * @param entities
	 * @throws AACException 
	 * @throws DataException 
	 * @throws SocialServiceException 
	 * @throws SecurityException 
	 */
	private static void checkTypes(Entities entities) throws SecurityException, SocialServiceException, DataException, AACException {
		for (Iterator<Entity> iterator = entities.getContent().iterator(); iterator.hasNext();) {
			Entity e = iterator.next();
//			if (!types.containsKey(e.getEntityType())) {
//				EntityType et = getInstance().socialService.getEntityTypeById(getAuthToken(), e.getEntityType());
//				if (et != null) {
//					types.put(et.getId(), et.getName());
//					types.put(et.getName(), et.getId());
//				}
//			}
			if (CMConstants.getTypeByTypeId(e.getEntityType()) == null) {
				iterator.remove();
			}
		}
	}

	public static void share(Entity share, ShareVisibility vis) throws SecurityException, SocialServiceException, DataException, AACException {
		getInstance().socialService.shareUserEntity(getAuthToken(), share.getEntityId(), vis);
	}

	/**
	 * check the presence of only the SC community in the user communities' list and add it if not present.
	 * @param p 
	 * @throws DataException
	 * @throws AACException 
	 * @throws SocialServiceException 
	 * @throws ConnectionException
	 * @throws ProtocolException
	 * @throws SecurityException
	 */
	private static void checkSCCommunity() throws DataException, SecurityException, SocialServiceException, AACException  {
		if (getCommunities() != null && getCommunities().size() > 0) {
			getInstance().scCommunity = getCommunities().get(0);
		}
		else {
			Collection<Community> list = fetchCommunities();
			if (list != null && ! list.isEmpty()) getInstance().scCommunity = list.iterator().next();
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

	public static ShareVisibility getEntitySharing(String entityId) throws SecurityException, SocialServiceException, DataException, AACException {
		Entity entity = getInstance().socialService.getUserEntity(getAuthToken(), entityId);
		if (entity == null) return null;
		return entity.getVisibility();
	}

	public static Set<String> getUserGroups(User mp) {
		Set<String> res = new HashSet<String>();
		if (getGroups() != null) {
			for (Group g : getGroups()) {
				if (g.getUsers() != null) {
					for (User u : g.getUsers()) {
						if (u.getSocialId().equals(mp.getSocialId())) res.add(g.getSocialId());
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
		return knownUsers.containsKey(user_mp.getSocialId());
	}

	/**
	 * @param entityType
	 * @return
	 */
	public static String getEntityTypeName(String entityType) {
//		return types.get(entityType);
		return CMConstants.getTypeByTypeId(entityType);
	}

	/**
	 * @return
	 */
	public static List<PictureProfile> getKnownUsers() {
		if (knownUsers == null || knownUsers.isEmpty()) return null;
		return new ArrayList<PictureProfile>(knownUsers.values());
	}
}
