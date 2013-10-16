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

import java.io.Serializable;

import eu.trentorise.smartcampus.social.model.User;

/**
 * 
 * Minimal information for a user visualization
 * 
 * @author Mirko Perillo
 * 
 */
public class PictureProfile extends User implements Serializable {
	private static final long serialVersionUID = 2720812823469859081L;
	
	private String pictureUrl;

	public PictureProfile() {
		super();
	}

	/**
	 * @param user
	 */
	public PictureProfile(User user) {
		setId(user.getId());
		setName(user.getName());
		setSocialId(user.getSocialId());
		setSurname(user.getSurname());
	}

	public String getPictureUrl() {
		return pictureUrl;
	}

	public void setPictureUrl(String pictureUrl) {
		this.pictureUrl = pictureUrl;
	}

	public String fullName() {
		return getName() + " "+getSurname();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
		result = prime * result
				+ ((getSocialId() == null) ? 0 : getSocialId().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		PictureProfile other = (PictureProfile) obj;
		if (getId() == null) {
			if (other.getId() != null)
				return false;
		} else if (!getId().equals(other.getId()))
			return false;
		if (getSocialId() == null) {
			if (other.getSocialId() != null)
				return false;
		} else if (!getSocialId().equals(other.getSocialId()))
			return false;
		return true;
	}
	
	
}
