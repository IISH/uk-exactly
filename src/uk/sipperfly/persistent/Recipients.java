/*
 * GA Digital Files Transfer Tool
 * Author: Nouman Tayyab
 * Version: 1.0
 * Requires: JDK 1.7 or higher
 * Description: This tool transfers digital files to the Gates Archive.
 * Support: info@gatesarchive.com
 * Copyright 2013 Gates Archive
 */
package uk.sipperfly.persistent;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Recipient Entity
 *
 * @author noumantayyab
 */
@Entity
public class Recipients implements Serializable {

	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	private String email;

	/**
	 * Get ID
	 *
	 * @return
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Set ID
	 *
	 * @param id
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Get Email
	 *
	 * @return
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Set Email
	 *
	 * @param email
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public int hashCode() {
		int hash = 0;
		hash += (id != null ? id.hashCode() : 0);
		return hash;
	}

	@Override
	public boolean equals(Object object) {
		// TODO: Warning - this method won't work in the case the id fields are not set
		if (!(object instanceof Recipients)) {
			return false;
		}
		Recipients other = (Recipients) object;
		if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "my.persistent.Recipients[ id=" + id + " ]";
	}

}