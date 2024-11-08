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
package org.olat.admin.user;

import java.util.ArrayList;
import java.util.List;

import org.olat.basesecurity.OrganisationEmailDomain;
import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 26 avr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SelectOrganisationController extends FormBasicController {
	
	private SingleSelection organisationEl;
	
	private final List<Organisation> organisations;
	private final Identity editedIdentity;
	
	@Autowired
	private OrganisationModule organisationModule;
	@Autowired
	private OrganisationService organisationService;

	public SelectOrganisationController(UserRequest ureq, WindowControl wControl, List<Organisation> organisations, Identity editedIdentity) {
		super(ureq, wControl);
		this.organisations = new ArrayList<>(organisations);
		this.editedIdentity = editedIdentity;
		
		initForm(ureq);
		updateEmailDomainUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		List<String> theKeys = new ArrayList<>();
		List<String> theValues = new ArrayList<>();
		
		for(Organisation organisation:organisations) {
			theKeys.add(organisation.getKey().toString());
			theValues.add(organisation.getDisplayName());
		}
		organisationEl = uifactory.addDropdownSingleselect("select.organisation", formLayout,
				theKeys.toArray(new String[theKeys.size()]), theValues.toArray(new String[theValues.size()]));
		if (organisationEl.getKeys().length > 0) {
			organisationEl.select(organisationEl.getKey(0), true);
			if (editedIdentity != null && organisationModule.isEmailDomainEnabled()) {
				organisationEl.addActionListener(FormEvent.ONCHANGE);
			}
		}
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	public Organisation getSelectedOrganisation() {
		Organisation organisation = null;
		if(organisationEl.isOneSelected()) {
			String selectedKey = organisationEl.getSelectedKey();
			for(Organisation org:organisations) {
				if(org.getKey().toString().equals(selectedKey)) {
					organisation = org;
				}
			}
		}
		return organisation;
	}

	private void updateEmailDomainUI() {
		if (editedIdentity != null && organisationModule.isEmailDomainEnabled() && organisationEl.isOneSelected()) {
			List<OrganisationEmailDomain> emailDomains = organisationService.getEnabledEmailDomains(() -> Long.valueOf(organisationEl.getSelectedKey()));
			boolean emailDomainAllowed = organisationService.isEmailDomainAllowed(emailDomains, editedIdentity.getUser().getEmail());
			if (!emailDomainAllowed) {
				organisationEl.setWarningKey("error.email.domain.not.allowed");
			} else {
				organisationEl.clearWarning();
			}
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == organisationEl) {
			updateEmailDomainUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		organisationEl.clearError();
		if(!organisationEl.isOneSelected()) {
			organisationEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
