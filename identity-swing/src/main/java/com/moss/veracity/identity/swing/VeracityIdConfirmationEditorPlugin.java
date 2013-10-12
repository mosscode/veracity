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
package com.moss.veracity.identity.swing;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JTextField;

import com.moss.identity.IdProofCheckRecipe;
import com.moss.identity.IdProofRecipie;
import com.moss.identity.standard.DelegatedIdProofCheckRecipe;
import com.moss.identity.standard.PasswordProofRecipie;
import com.moss.identity.tools.swing.proofcheckrecipie.IdConfirmationConfigPanelPlugin;
import com.moss.identity.veracity.VeracityId;
import com.moss.swing.event.DocumentAdapter;

public class VeracityIdConfirmationEditorPlugin implements IdConfirmationConfigPanelPlugin {
	private final boolean allowEmptyPasswords;
	
	private VeracityIdConfirmationEditorPluginView view = new VeracityIdConfirmationEditorPluginView();
	
	private final List<String> errors = new LinkedList<String>();

	private JTextField hostsField;
	
	private String host;
	
	private String user;
	
	private String password;
	
	private VeracityId id;
	
	public VeracityIdConfirmationEditorPlugin(final boolean allowEmptyPasswords, final boolean allowHostsEditing, String ... hosts) {
		view.errorsField().setVisible(false);
		
		view.hostField().setEditable(allowHostsEditing);
		
		if(hosts!=null){
			DefaultComboBoxModel m = new DefaultComboBoxModel();
			for(String next : hosts){
				m.addElement(next);
			}
			view.hostField().setModel(m);
			view.hostField().setSelectedIndex(0);
			host = hosts[0];
		}
		
		view.hostField().addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				Object selection = view.hostField().getSelectedItem();
				if(selection!=null){
					host = selection.toString();
				}else{
					host = null;
				}
			}
		});
		
		
		this.allowEmptyPasswords = allowEmptyPasswords;
		
		view.nameField().getDocument().addDocumentListener(new DocumentAdapter(){
			@Override
			public void updateHappened() {
				user = view.nameField().getText();
				validate();
			}
		});
		hostsField = (JTextField) view.hostField().getEditor().getEditorComponent();
		hostsField.getDocument().addDocumentListener(new DocumentAdapter(){
			@Override
			public void updateHappened() {
				host = hostsField.getText();
				validate();
			}
		});
		
		validate();
	}
	
	
	private void validate(){
		errors.clear();
		{
			
			boolean hostIsGood = true;
			if(host==null || host.toString().trim().length()==0){
				errors.add("You must select a host");
				hostIsGood = false;
			}
			boolean userIsGood = true;
			if(user==null || user.trim().length()==0){
				errors.add("You must supply an ID");
				userIsGood = false;
			}
			
			if(hostIsGood && userIsGood){
				
				try {
					id = new VeracityId(user.trim() + "@" + host.toString().trim());
				} catch (Exception e) {
					e.printStackTrace();
					id = null;
					
					String message = e.getMessage()==null?e.getClass().getSimpleName():e.getMessage();
					errors.add(message);
				}
				
			}
		}

		StringBuilder text = new StringBuilder();
		for(String next : errors){
			text.append(next);
			text.append('\n');
		}
		view.errorsField().setText(text.toString());
	}
	
	public void showErrors() {
		view.errorsField().setVisible(true);
	}
	
	public boolean hasErrors() {
		return this.errors.size()>0;
	}
	
	public Component view() {
		return view;
	}
	
	public void setValue(IdProofCheckRecipe value) {
		VeracityId id = (VeracityId) value.id();
		
		int atPos = id.toString().indexOf('@');
		
		view.nameField().setText(id.toString().substring(0, atPos));
		view.hostField().setSelectedItem(id.toString().substring(atPos+1));
	}
	
	public String typeName() {
		return "Veracity Service";
	}
	
	public boolean canHandle(IdProofRecipie r) {
		return r instanceof PasswordProofRecipie && r.id() instanceof VeracityId;
	}
	
	public DelegatedIdProofCheckRecipe getValue() {
		return new DelegatedIdProofCheckRecipe(id);
	}
	
	public IdProofRecipie proofRecipie() {
		return null;
	}
}
