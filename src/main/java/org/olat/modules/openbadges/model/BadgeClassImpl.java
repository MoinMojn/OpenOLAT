/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.openbadges.model;

import java.io.Serial;
import java.util.Date;

import org.olat.core.id.Persistable;
import org.olat.modules.openbadges.BadgeClass;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

/**
 * Initial date: 2023-05-30<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Entity(name="badgeclass")
@Table(name="o_badge_class")
public class BadgeClassImpl implements Persistable, BadgeClass {

	@Serial
	private static final long serialVersionUID = 4628879504742724536L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, unique = true, insertable = true, updatable = false)
	private Long key;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "creationdate", nullable = false, insertable = true, updatable = false)
	private Date creationDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "lastmodified", nullable = false, insertable = true, updatable = true)
	private Date lastModified;

	@Column(name = "b_uuid", nullable = false, insertable = true, updatable = false)
	private String uuid;

	@Column(name = "b_status", nullable = false, insertable = true, updatable = true)
	private BadgeClassStatus status;

	@Column(name = "b_version", nullable = false, insertable = true, updatable = true)
	private String version;

	@Column(name = "b_image", nullable = false, insertable = true, updatable = true)
	private String image;

	@Column(name = "b_name", nullable = false, insertable = true, updatable = true)
	private String name;

	@Column(name = "b_description", nullable = false, insertable = true, updatable = true)
	private String description;

	@Column(name = "b_criteria", nullable = false, insertable = true, updatable = true)
	private String criteria;

	@Column(name = "b_issuer", nullable = false, insertable = true, updatable = true)
	private String issuer;

	@Column(name = "b_tags", nullable = false, insertable = true, updatable = true)
	private String tags;

	public BadgeClassImpl() {
	}

	@Override
	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	@Override
	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	@Override
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	@Override
	public BadgeClassStatus getStatus() {
		return status;
	}

	@Override
	public void setStatus(BadgeClassStatus status) {
		this.status = status;
	}

	@Override
	public String getVersion() {
		return version;
	}

	@Override
	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public String getImage() {
		return image;
	}

	@Override
	public void setImage(String image) {
		this.image = image;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String getCriteria() {
		return criteria;
	}

	@Override
	public void setCriteria(String criteria) {
		this.criteria = criteria;
	}

	@Override
	public String getIssuer() {
		return issuer;
	}

	@Override
	public String getTags() {
		return tags;
	}

	@Override
	public void setTags(String tags) {
		this.tags = tags;
	}

	@Override
	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof BadgeClassImpl badgeClass) {
			return getKey() != null && getKey().equals(badgeClass.getKey());
		}
		return false;
	}
}
