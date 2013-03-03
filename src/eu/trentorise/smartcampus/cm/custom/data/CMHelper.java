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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import eu.trentorise.smartcampus.ac.SCAccessProvider;
import eu.trentorise.smartcampus.ac.authenticator.AMSCAccessProvider;
import eu.trentorise.smartcampus.android.common.GlobalConfig;
import eu.trentorise.smartcampus.android.common.Utils;
import eu.trentorise.smartcampus.android.common.sharing.ShareEntityObject;
import eu.trentorise.smartcampus.android.common.tagging.SemanticSuggestion;
import eu.trentorise.smartcampus.android.common.tagging.SuggestionHelper;
import eu.trentorise.smartcampus.cm.model.CMConstants;
import eu.trentorise.smartcampus.cm.model.Community;
import eu.trentorise.smartcampus.cm.model.Group;
import eu.trentorise.smartcampus.cm.model.GroupAssignment;
import eu.trentorise.smartcampus.cm.model.MinimalProfile;
import eu.trentorise.smartcampus.cm.model.Profile;
import eu.trentorise.smartcampus.cm.model.ProfileSearchFilter;
import eu.trentorise.smartcampus.cm.model.ShareOperation;
import eu.trentorise.smartcampus.cm.model.ShareVisibility;
import eu.trentorise.smartcampus.cm.model.SharedContent;
import eu.trentorise.smartcampus.cm.model.SocialContainer;
import eu.trentorise.smartcampus.cm.model.StoreProfile;
import eu.trentorise.smartcampus.cm.model.Topic;
import eu.trentorise.smartcampus.protocolcarrier.ProtocolCarrier;
import eu.trentorise.smartcampus.protocolcarrier.common.Constants.Method;
import eu.trentorise.smartcampus.protocolcarrier.custom.FileRequestParam;
import eu.trentorise.smartcampus.protocolcarrier.custom.MessageRequest;
import eu.trentorise.smartcampus.protocolcarrier.custom.MessageResponse;
import eu.trentorise.smartcampus.protocolcarrier.custom.RequestParam;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.ConnectionException;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.ProtocolException;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;
import eu.trentorise.smartcampus.storage.DataException;
import eu.trentorise.smartcampus.storage.remote.RemoteStorage;

public class CMHelper {

	private static CMHelper instance = null;

	private static SCAccessProvider accessProvider = new AMSCAccessProvider();

	private Context mContext;

	private static RemoteStorage remoteStorage = null;

	private ProtocolCarrier mProtocolCarrier = null;

	private static Profile profile;
	private static List<Group> savedGroups;
	private static Map<Long, MinimalProfile> knownUsers = new HashMap<Long, MinimalProfile>();

	private Community scCommunity = null;
	
	public static void init(Context mContext) {
		instance = new CMHelper(mContext);
	}

	public static boolean isInitialized() {
		return instance != null;
	}
	
	public static String getAuthToken() {
		return accessProvider.readToken(instance.mContext, null);
	}

	public static void setProfile(Profile profile) {
		CMHelper.profile = profile;
		profile.setCommunities(Collections.singletonList(instance.scCommunity));
	}

	public static Profile getProfile() {
		return profile;
	}

	private static CMHelper getInstance() throws DataException {
		if (instance == null)
			throw new DataException("DTHelper is not initialized");
		return instance;
	}

	public static SCAccessProvider getAccessProvider() {
		return accessProvider;
	}

	protected CMHelper(Context mContext) {
		super();
		this.mContext = mContext;
		this.mProtocolCarrier = new ProtocolCarrier(mContext,
				Constants.APP_TOKEN);
	}


	public static void destroy() throws DataException {
	}

	private static RemoteStorage getRemote(Context mContext, String token) throws ProtocolException, DataException {
		if (remoteStorage == null) {
			remoteStorage = new RemoteStorage(mContext, Constants.APP_TOKEN);
		}
		remoteStorage.setConfig(token, GlobalConfig.getAppUrl(getInstance().mContext), Constants.SERVICE);
		return remoteStorage;
	}

	public static Profile retrieveProfile() throws ConnectionException,
			ProtocolException, SecurityException, DataException {
		MessageRequest request = new MessageRequest(GlobalConfig.getAppUrl(getInstance().mContext),
				Constants.SERVICE
						+ "/eu.trentorise.smartcampus.cm.model.Profile/current");
		request.setMethod(Method.GET);
		MessageResponse response = getInstance().mProtocolCarrier.invokeSync(
				request, Constants.APP_TOKEN, getAuthToken());
		String body = response.getBody();
		if (body == null || body.trim().length() == 0) {
			setGroups(new ArrayList<Group>());
			return null;
		}
		setGroups(readGroups());
		// check that the default SC community is in the user communities and add it otherwise.
		Profile p = Utils.convertJSONToObject(body, Profile.class);
		checkSCCommunity(p);
		return p;
	}

	public static Profile storeProfile(Profile profile)
			throws ConnectionException, ProtocolException, SecurityException,
			DataException {
		MessageRequest request = new MessageRequest(GlobalConfig.getAppUrl(getInstance().mContext),
				Constants.SERVICE
						+ "/eu.trentorise.smartcampus.cm.model.StoreProfile");
		request.setMethod(Method.POST);
		String json = Utils.convertToJSON(Utils.convertObjectToData(
				StoreProfile.class, profile));
		request.setBody(json);

		MessageResponse response = getInstance().mProtocolCarrier.invokeSync(
				request, Constants.APP_TOKEN, getAuthToken());
		String body = response.getBody();
		if (body == null || body.trim().length() == 0) {
			return null;
		}
		setGroups(readGroups());
		profile = Utils.convertJSONToObject(body, Profile.class);
		setProfile(profile);
		return profile;
	}

	public static boolean addToCommunity(String communityId)
			throws ConnectionException, ProtocolException, SecurityException,
			DataException {
		MessageRequest request = new MessageRequest(GlobalConfig.getAppUrl(getInstance().mContext),
				Constants.SERVICE + "/addtocommunity/" + communityId);
		request.setMethod(Method.PUT);
		MessageResponse response = getInstance().mProtocolCarrier.invokeSync(
				request, Constants.APP_TOKEN, getAuthToken());
		String body = response.getBody();
		if (body == null || body.trim().length() == 0) {
			return false;
		}
		return Boolean.parseBoolean(body);
	}

	public static boolean removeFromCommunity(String communityId)
			throws ConnectionException, ProtocolException, SecurityException,
			DataException {
		MessageRequest request = new MessageRequest(GlobalConfig.getAppUrl(getInstance().mContext),
				Constants.SERVICE + "/removefromcommunity/" + communityId);
		request.setMethod(Method.PUT);
		MessageResponse response = getInstance().mProtocolCarrier.invokeSync(
				request, Constants.APP_TOKEN, getAuthToken());
		String body = response.getBody();
		if (body == null || body.trim().length() == 0) {
			return false;
		}
		return Boolean.parseBoolean(body);
	}

	public static List<MinimalProfile> getPeople(String search)
			throws ConnectionException, ProtocolException, SecurityException,
			DataException {
		Collection<MinimalProfile> coll = null;
		if (search != null && search.trim().length() != 0) {
			ProfileSearchFilter filter = new ProfileSearchFilter();
			filter.setFullname(search.toLowerCase());
			coll = getRemote(getInstance().mContext, getAuthToken())
					.searchObjects(filter, MinimalProfile.class);
		} else {
			coll = getRemote(getInstance().mContext, getAuthToken())
					.getObjects(MinimalProfile.class);
		}
		if (coll != null)
			return new ArrayList<MinimalProfile>(coll);
		return Collections.emptyList();
	}

	public static List<Group> readGroups() throws DataException,
			ConnectionException, ProtocolException, SecurityException {
		Collection<Group> groups = null;
		groups = getRemote(instance.mContext, getAuthToken()).getObjects(
				Group.class);

		if (groups != null) {
			for (Group g : groups) {
				if (g.getUsers() != null) {
					for (MinimalProfile mp : g.getUsers()) {
						mp.setKnown(true);
					}
				}
			}
		}
		return groups == null ? new ArrayList<Group>() : new ArrayList<Group>(
				groups);
	}

	public static Group saveGroup(Group group) throws DataException,
			ConnectionException, ProtocolException, SecurityException {
		Group newGroup = group;
		if (group.getId() != null) {
			getRemote(instance.mContext, getAuthToken()).update(group, false);
		} else {
			newGroup = getRemote(instance.mContext, getAuthToken()).create(
					group);
		}
		setGroups(readGroups());
		return newGroup;
	}

	public static void deleteGroup(Group group) throws DataException,
			ConnectionException, ProtocolException, SecurityException {
		getRemote(instance.mContext, getAuthToken()).delete(group.getId(),
				Group.class);
		setGroups(readGroups());
	}

	public static Collection<Community> getCommunities() throws DataException,
			ConnectionException, ProtocolException, SecurityException {
		Collection<Community> collection = null;
		collection = getRemote(instance.mContext, getAuthToken()).getObjects(
				Community.class);

		return collection;
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

	public static MinimalProfile removeFromKnown(MinimalProfile user)
			throws ConnectionException, ProtocolException, SecurityException,
			DataException {
		MessageRequest request = new MessageRequest(GlobalConfig.getAppUrl(getInstance().mContext),
				Constants.SERVICE + "/defaultgroup/" + user.getSocialId());
		request.setMethod(Method.DELETE);
		getInstance().mProtocolCarrier.invokeSync(request, Constants.APP_TOKEN,
				getAuthToken());
		setGroups(readGroups());
		return user;
	}

	public static void uploadPictureProfile(Profile profile, byte[] content)
			throws ConnectionException, ProtocolException, SecurityException,
			DataException {
		if (profile.getPictureUrl() != null) {
			String fid = profile.getPictureUrl().substring(
					profile.getPictureUrl().lastIndexOf('/') + 1);
			try {
				replaceFile(Long.parseLong(fid), content);
			} catch (NumberFormatException e) {
				Log.e("CMHelper", "error parsing resource url");
				throw new DataException();
			}
		} else {
			String resourceUrl = updloadFile(content);
			profile.setPictureUrl(resourceUrl);
			storeProfile(profile);
		}
	}

	public static String updloadFile(byte[] content)
			throws ConnectionException, ProtocolException, SecurityException,
			DataException {
		MessageRequest request = new MessageRequest(GlobalConfig.getAppUrl(getInstance().mContext),
				Constants.FILE_SERVICE + "/");
		request.setMethod(Method.POST);
		FileRequestParam param = new FileRequestParam();
		param.setContent(content);
		request.setRequestParams(Collections.<RequestParam>singletonList(param));
		MessageResponse response = getInstance().mProtocolCarrier.invokeSync(
				request, Constants.APP_TOKEN, getAuthToken());

		String resourceUrl = GlobalConfig.getAppUrl(getInstance().mContext) + "/" + Constants.FILE_SERVICE
				+ "/" + Long.parseLong(response.getBody());

		return resourceUrl;

	}

	public static boolean replaceFile(long fid, byte[] content)
			throws ConnectionException, ProtocolException, SecurityException,
			DataException {
		MessageRequest request = new MessageRequest(GlobalConfig.getAppUrl(getInstance().mContext),
				Constants.FILE_SERVICE + "/" + fid);
		request.setMethod(Method.POST);

		// set requestFile true to get the byte array of file requested
		FileRequestParam param = new FileRequestParam();
		param.setContent(content);
		request.setRequestParams(Collections.<RequestParam>singletonList(param));
		MessageResponse response = getInstance().mProtocolCarrier.invokeSync(
				request, Constants.APP_TOKEN, getAuthToken());

		return Boolean.parseBoolean(response.getBody());

	}

	public static byte[] downloadFile(long fid) throws ConnectionException,
			ProtocolException, SecurityException, DataException {
		MessageRequest request = new MessageRequest(GlobalConfig.getAppUrl(getInstance().mContext),
				Constants.SERVICE + "/file/" + fid);
		request.setMethod(Method.GET);

		// set requestFile true to get the byte array of file requested
		request.setRequestFile(true);
		MessageResponse response = getInstance().mProtocolCarrier.invokeSync(
				request, Constants.APP_TOKEN, getAuthToken());

		return response.getFileContent();

	}

	public static boolean assignToGroups(MinimalProfile user,
			Collection<Group> groups) throws ConnectionException,
			ProtocolException, SecurityException, DataException {
		GroupAssignment ga = new GroupAssignment();
		ga.setUserId(user.getSocialId());
		ga.setGroupIds(new ArrayList<Long>());
		if (groups != null) {
			for (Group g : groups) {
				if (g.getSocialId() == CMConstants.MY_PEOPLE_GROUP_ID)
					continue;
				ga.getGroupIds().add(g.getSocialId());
			}
		}

		MessageRequest request = new MessageRequest(GlobalConfig.getAppUrl(getInstance().mContext),
				Constants.SERVICE + "/assigntogroup");
		request.setMethod(Method.PUT);
		request.setBody(Utils.convertToJSON(ga));
		MessageResponse response = getInstance().mProtocolCarrier.invokeSync(
				request, Constants.APP_TOKEN, getAuthToken());
		String body = response.getBody();
		setGroups(readGroups());
		if (body == null || body.trim().length() == 0) {
			return false;
		}
		return Boolean.parseBoolean(body);

	}

	public static List<Group> getGroups() {
		return savedGroups;
	}

	private static void setGroups(List<Group> groups) {
		savedGroups = groups;
		for (Group g : groups) {
			if (g.getSocialId() == CMConstants.MY_PEOPLE_GROUP_ID) {
				if (g.getUsers() != null) {
					for (MinimalProfile mp : g.getUsers()) {
						knownUsers.put(mp.getSocialId(), mp);
					}
				}
				break;
			}
		}
	}

	public static MinimalProfile getKnownUser(Long id) {
		return knownUsers.get(id);
	}

	public static List<Topic> getTopics() throws DataException,
			ConnectionException, ProtocolException, SecurityException {
		Collection<Topic> coll = getRemote(instance.mContext, getAuthToken())
				.getObjects(Topic.class);
		if (coll != null)
			return new ArrayList<Topic>(coll);
		return Collections.emptyList();
	}

	public static Topic saveTopic(Topic topic) throws DataException,
			ConnectionException, ProtocolException, SecurityException {
		List<String> old = topic.getContentTypes();
		Topic result = null;
		if (topic.getId() != null) {
			result = topic;
			getRemote(instance.mContext, getAuthToken()).update(topic, false);
		} else {
			result = getRemote(instance.mContext, getAuthToken()).create(topic);
		}
		result.setContentTypes(old);
		return result;
	}

	public static boolean removeTopic(String id) throws DataException,
			ConnectionException, ProtocolException, SecurityException {
		getRemote(instance.mContext, getAuthToken()).delete(id, Topic.class);
		return true;
	}

	public static List<SemanticSuggestion> getSuggestions(CharSequence suggest)
			throws ConnectionException, ProtocolException, SecurityException,
			DataException {
		return SuggestionHelper.getSuggestions(suggest, getInstance().mContext,
				GlobalConfig.getAppUrl(getInstance().mContext), getAuthToken(), Constants.APP_TOKEN);
	}

	public static List<SharedContent> readSharedObjects(
			ShareVisibility shareVisibility, int position, int size, String type)
			throws ConnectionException, ProtocolException, SecurityException,
			DataException {
		MessageRequest request = new MessageRequest(GlobalConfig.getAppUrl(getInstance().mContext),
				Constants.SERVICE + "/sharedcontent");
		request.setMethod(Method.POST);
		request.setBody(Utils.convertToJSON(shareVisibility));
		request.setQuery("position=" + position + "&size=" + size + "&type="
				+ (type == null ? "" : type));
		MessageResponse response = getInstance().mProtocolCarrier.invokeSync(
				request, Constants.APP_TOKEN, getAuthToken());
		String body = response.getBody();
		if (body == null || body.trim().length() == 0) {
			return Collections.emptyList();
		}
		return Utils.convertJSONToObjects(body, SharedContent.class);
	}

	public static List<SharedContent> readMyObjects(int position, int size,
			String type) throws ConnectionException, ProtocolException,
			SecurityException, DataException {
		MessageRequest request = new MessageRequest(GlobalConfig.getAppUrl(getInstance().mContext),
				Constants.SERVICE + "/content");
		request.setMethod(Method.GET);
		request.setQuery("position=" + position + "&size=" + size + "&type="
				+ (type == null ? "" : type));
		MessageResponse response = getInstance().mProtocolCarrier.invokeSync(
				request, Constants.APP_TOKEN, getAuthToken());
		String body = response.getBody();
		if (body == null || body.trim().length() == 0) {
			return Collections.emptyList();
		}
		return Utils.convertJSONToObjects(body, SharedContent.class);
	}

	public static void share(ShareEntityObject share, SocialContainer socialContainer) throws ConnectionException, ProtocolException, SecurityException, DataException {
		ShareOperation op = new ShareOperation();
		op.setEntityId(share.getEntityId());
		op.setVisibility(new ShareVisibility());
		op.getVisibility().setAllUsers(socialContainer.isAllUsers());
		if (socialContainer.getGroups() != null) {
			List<Long> groups = new ArrayList<Long>();
			for (Group g : socialContainer.getGroups()) {
				if (g.getName().equals(CMConstants.MY_PEOPLE_GROUP_NAME)) {
					op.getVisibility().setAllKnownUsers(true);
				} else {
					groups.add(g.getSocialId());
				}
			}
			op.getVisibility().setGroupIds(groups);
		}
		if (socialContainer.getCommunities() != null) {
			List<Long> communityIds = new ArrayList<Long>();
			for (Community c : socialContainer.getCommunities()) {
				communityIds.add(c.getSocialId());
			}
			op.getVisibility().setCommunityIds(communityIds);
		}
		if (socialContainer.getUsers() != null) {
			List<Long> userIds = new ArrayList<Long>();
			for (MinimalProfile c : socialContainer.getUsers()) {
				userIds.add(c.getSocialId());
			}
			op.getVisibility().setUserIds(userIds);
		}
		
		MessageRequest request = new MessageRequest(GlobalConfig.getAppUrl(getInstance().mContext), Constants.SERVICE + "/share");
		request.setMethod(Method.POST);
		request.setBody(Utils.convertToJSON(op));
		getInstance().mProtocolCarrier.invokeSync(request, Constants.APP_TOKEN, getAuthToken());
	}

	/**
	 * check the presence of only the SC community in the user communities' list and add it if not present.
	 * @param p 
	 * @throws DataException
	 * @throws ConnectionException
	 * @throws ProtocolException
	 * @throws SecurityException
	 */
	private static void checkSCCommunity(Profile p) throws DataException, ConnectionException, ProtocolException, SecurityException {
		if (p.getCommunities() != null && p.getCommunities().size() > 0) {
			getInstance().scCommunity = p.getCommunities().get(0);
		}
		else {
			Collection<Community> list = getCommunities();
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

	public static ShareVisibility getEntitySharing(Long entityId) throws ConnectionException, ProtocolException, SecurityException, DataException {
		MessageRequest request = new MessageRequest(GlobalConfig.getAppUrl(getInstance().mContext), Constants.SERVICE + "/assignments/"+entityId);
		request.setMethod(Method.GET);
		MessageResponse response = getInstance().mProtocolCarrier.invokeSync(request, Constants.APP_TOKEN, getAuthToken());
		String body = response.getBody();
		if (body == null || body.trim().length() == 0) {
			return null;
		}
		return Utils.convertJSONToObject(body, ShareVisibility.class);
	}

	public static Set<Long> getUserGroups(MinimalProfile mp) {
		Set<Long> res = new HashSet<Long>();
		if (getGroups() != null) {
			for (Group g : getGroups()) {
				if (g.getUsers() != null && g.getUsers().contains(mp)) res.add(g.getSocialId());
			}
		}
		return res;
	}
}
