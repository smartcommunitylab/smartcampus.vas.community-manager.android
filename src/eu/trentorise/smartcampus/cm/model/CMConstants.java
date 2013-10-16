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
package eu.trentorise.smartcampus.cm.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import eu.trentorise.smartcampus.cm.R;

public class CMConstants {

	public static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("dd/MMM/yyyy '-' HH:mm", Locale.US);
	
	public static final String ENTTIY_TYPE_POI = "POI";
	public static final String ENTTIY_TYPE_EVENT = "Event";
	public static final String ENTTIY_TYPE_PORTFOLIO = "Portfolio";
	public static final String ENTTIY_TYPE_EXPERIENCE = "Experience";
	public static final String ENTTIY_TYPE_PHOTO_VIDEO = "Photo/Video";
	public static final String ENTTIY_TYPE_JOURNEY = "Journey";
	public static final String ENTTIY_TYPE_STORY = "Story";

	private static Map<String, String> entityTypeMap = new HashMap<String, String>();
	private static Map<String, String> entityTypeIdMap = new HashMap<String, String>();
	static {
		entityTypeMap.put(ENTTIY_TYPE_POI, "location");
		entityTypeMap.put(ENTTIY_TYPE_EVENT, "event");
		entityTypeMap.put(ENTTIY_TYPE_PORTFOLIO, "portfolio");
		entityTypeMap.put(ENTTIY_TYPE_EXPERIENCE, "experience");
		entityTypeMap.put(ENTTIY_TYPE_PHOTO_VIDEO, "computer file");
		entityTypeMap.put(ENTTIY_TYPE_JOURNEY, "journey");
		entityTypeMap.put(ENTTIY_TYPE_STORY, "narrative");

		entityTypeIdMap.put("44", "location");
		entityTypeIdMap.put("39", "event");
		entityTypeIdMap.put("45", "portfolio");
		entityTypeIdMap.put("40", "experience");
		entityTypeIdMap.put("41", "computer file");
		entityTypeIdMap.put("42", "journey");
		entityTypeIdMap.put("46", "narrative");
		for (String k : new HashSet<String>(entityTypeIdMap.keySet())) {
			entityTypeIdMap.put(entityTypeIdMap.get(k), k);
		}
}

	public static String getLocalType(String remoteType) {
		for (Entry<String,String> e : entityTypeMap.entrySet()) {
			if (e.getValue().equals(remoteType)) {
				return e.getKey();
			}
		}
		return null;
	}

	public static String getRemoteType(String localType) {
		return entityTypeMap.get(localType);
	}

	public static List<String> getRemoteTypes(List<String> localTypes) {
		List<String> list = new ArrayList<String>();
		if (localTypes != null)  for (String s : localTypes) if (entityTypeMap.containsKey(s)) list.add(entityTypeMap.get(s));
		return list;
	}
	
	public static List<String> getAllRemoteTypes() {
		return new ArrayList<String>(entityTypeMap.values());
	}
	
	public static List<String> getLocalTypes(List<String> remoteTypes) {
		List<String> list = new ArrayList<String>();
		if (remoteTypes != null)  {
			for (Entry<String,String> s : entityTypeMap.entrySet()) {
				if (remoteTypes.contains(s.getValue())) {
					list.add(entityTypeMap.get(s));
				}
			}
		}
		return list;
	}
	
	public static final String MY_PEOPLE_GROUP_NAME = "My People";
	public static final String MY_PEOPLE_GROUP_ID = "-999";

	public static class ObjectFilterDescriptor {
		
		public int drawable;
		public int drawable_selected;
		public int object_drawable;
		public int contentDescription;
		public String type;
		public ObjectFilterDescriptor(int drawable_selected,int drawable, 
				int object_drawable, int contentDescription, String type) {
			super();
			this.drawable = drawable;
			this.drawable_selected = drawable_selected;
			this.object_drawable = object_drawable;
			this.contentDescription = contentDescription;
			this.type = type;
		}
		
	}
	
	public static ObjectFilterDescriptor[] FILTER_DESCRIPTORS = new ObjectFilterDescriptor[]{
		new ObjectFilterDescriptor(R.drawable.ic_f_portfolio, R.drawable.ic_f_portfolio2, R.drawable.ic_a_portfolio, R.string.shared_content_filters_portfolio, getRemoteType(ENTTIY_TYPE_PORTFOLIO)),
		new ObjectFilterDescriptor(R.drawable.ic_f_buster, R.drawable.ic_f_buster2, R.drawable.ic_a_buster, R.string.shared_content_filters_experience, getRemoteType(ENTTIY_TYPE_EXPERIENCE)),
		new ObjectFilterDescriptor(R.drawable.ic_f_planner, R.drawable.ic_f_planner2, R.drawable.ic_a_planner, R.string.shared_content_filters_journey, getRemoteType(ENTTIY_TYPE_JOURNEY)),
		new ObjectFilterDescriptor(R.drawable.ic_f_poi, R.drawable.ic_f_poi2, R.drawable.ic_a_poi, R.string.shared_content_filters_poi, getRemoteType(ENTTIY_TYPE_POI)),
		new ObjectFilterDescriptor(R.drawable.ic_f_event, R.drawable.ic_f_event2, R.drawable.ic_a_event, R.string.shared_content_filters_event, getRemoteType(ENTTIY_TYPE_EVENT)),
		new ObjectFilterDescriptor(R.drawable.ic_f_story, R.drawable.ic_f_story2, R.drawable.ic_a_story, R.string.shared_content_filters_story, getRemoteType(ENTTIY_TYPE_STORY)),
	};
	
	private static Map<String, ObjectFilterDescriptor> objectFilterDescriptorMap = new HashMap<String, CMConstants.ObjectFilterDescriptor>();
	static {
		for (ObjectFilterDescriptor descr: FILTER_DESCRIPTORS) {
			objectFilterDescriptorMap.put(descr.type, descr);
		}
	}
	public static ObjectFilterDescriptor getObjectDescriptor(String type) {
		return objectFilterDescriptorMap.get(type);
	}
	
	public static String getTypeByTypeId(String typeId) {
		return entityTypeIdMap.get(typeId);
	}
	public static String getTypeIdByType(String type) {
		return entityTypeIdMap.get(type);
	}
}
