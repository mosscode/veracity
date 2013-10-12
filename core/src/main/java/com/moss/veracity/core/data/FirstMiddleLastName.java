/**
 * Copyright (C) 2013, Moss Computing Inc.
 *
 * This file is part of veracity.
 *
 * veracity is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * veracity is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with veracity; see the file COPYING.  If not, write to the
 * Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA.
 *
 * Linking this library statically or dynamically with other modules is
 * making a combined work based on this library.  Thus, the terms and
 * conditions of the GNU General Public License cover the whole
 * combination.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent
 * modules, and to copy and distribute the resulting executable under
 * terms of your choice, provided that you also meet, for each linked
 * independent module, the terms and conditions of the license of that
 * module.  An independent module is a module which is not derived from
 * or based on this library.  If you modify this library, you may extend
 * this exception to your version of the library, but you are not
 * obligated to do so.  If you do not wish to do so, delete this
 * exception statement from your version.
 */
package com.moss.veracity.core.data;

import com.moss.anthroponymy.StFirstMiddleLastName;
import com.moss.anthroponymy.StSalutation;

public final class FirstMiddleLastName {

	private String firstname;

	private String middleInitial;

	private String lastname;

	private StSalutation salutation;

	public FirstMiddleLastName() {}

	public FirstMiddleLastName(FirstMiddleLastName name){
		firstname = name.firstname;
		middleInitial = name.middleInitial;
		lastname = name.lastname;
	}

	public FirstMiddleLastName(String firstname, String middleInitial, String lastname) {
		this.firstname = firstname;
		this.middleInitial = middleInitial;
		this.lastname = lastname;
	}

	public FirstMiddleLastName(String firstname, String lastname){
		this.firstname = firstname;
		this.lastname = lastname;
	}

	public FirstMiddleLastName(StFirstMiddleLastName dto){
		this(dto.getFirstname(), dto.getMiddleInitial(), dto.getLastname());
	}

	public StFirstMiddleLastName toDto(){
		return new StFirstMiddleLastName(firstname, middleInitial, lastname);
	}

	public String getFirstname() {
		return firstname;
	}
	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}
	public String getLastname() {
		return lastname;
	}
	public void setLastname(String lastname) {
		this.lastname = lastname;
	}
	public String getMiddleInitial() {
		return middleInitial;
	}
	public void setMiddleInitial(String middleInitial) {
		this.middleInitial = middleInitial;
	}

	public String toString(){
		String fullname = "";
		if(firstname!=null) fullname += firstname;
		if(middleInitial!=null) fullname +=" " + middleInitial;
		if(lastname != null) fullname += " " + lastname;
		return fullname;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj==null || !(obj instanceof FirstMiddleLastName)) return false;
		FirstMiddleLastName other = (FirstMiddleLastName)obj;

		if(firstname!=null && !firstname.equals(other.firstname)) return false;
		if(firstname==null && other.firstname!=null) return false;

		if(lastname!=null && !lastname.equals(other.lastname)) return false;
		if(lastname==null && other.lastname!=null) return false;

		if(middleInitial!=null && !middleInitial.equals(other.middleInitial)) return false;
		if(middleInitial==null && other.middleInitial!=null) return false;

		return true;
	}

	public StSalutation getSalutation() {
		return salutation;
	}

	public void setSalutation(StSalutation salutation) {
		this.salutation = salutation;
	}
}