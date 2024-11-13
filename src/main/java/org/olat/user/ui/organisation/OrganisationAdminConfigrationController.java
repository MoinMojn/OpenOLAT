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
package org.olat.user.ui.organisation;

import org.olat.basesecurity.OrganisationModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 9 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OrganisationAdminConfigrationController extends FormBasicController {
	
	private FormToggle enableEl;
	private FormLayoutContainer emailDomainCont;
	private FormToggle emailDomainEnableEl;
	
	@Autowired
	private OrganisationModule organisationModule;
	
	public OrganisationAdminConfigrationController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer organisationCont = FormLayoutContainer.createDefaultFormLayout("organisations", getTranslator());
		organisationCont.setFormTitle(translate("organisation.configuration"));
		organisationCont.setFormInfo(translate("organisation.configuration.help"));
		organisationCont.setFormInfoHelp("manual_admin/administration/Modules_Organisations/");
		organisationCont.setRootForm(mainForm);
		formLayout.add(organisationCont);
		
		enableEl = uifactory.addToggleButton("organisation.admin.enabled", "organisation.admin.enabled", translate("on"), translate("off"), organisationCont);
		enableEl.toggle(organisationModule.isEnabled());
		enableEl.addActionListener(FormEvent.ONCHANGE);
		
		emailDomainCont = FormLayoutContainer.createDefaultFormLayout("emailDomain", getTranslator());
		emailDomainCont.setFormTitle(translate("organisation.email.domains"));
		emailDomainCont.setFormInfo(translate("organisation.email.domains.help"));
		emailDomainCont.setFormInfoHelp("manual_admin/administration/Modules_Organisations/#e-mail_domain_mapping");
		emailDomainCont.setElementCssClass("o_block_top");
		emailDomainCont.setRootForm(mainForm);
		formLayout.add(emailDomainCont);
		
		emailDomainEnableEl = uifactory.addToggleButton("email.domain", "organisation.admin.email.domain.enabled", translate("on"), translate("off"), emailDomainCont);
		emailDomainEnableEl.toggle(organisationModule.isEmailDomainEnabled());
		emailDomainEnableEl.addActionListener(FormEvent.ONCHANGE);
	}

	private void updateUI() {
		emailDomainCont.setVisible(enableEl.isOn());
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(enableEl == source) {
			organisationModule.setEnabled(enableEl.isOn());
			updateUI();
			fireEvent(ureq, Event.CHANGED_EVENT);
		} else if(emailDomainEnableEl == source) {
			organisationModule.setEmailDomainEnabled(emailDomainEnableEl.isOn());
			fireEvent(ureq, Event.CHANGED_EVENT);
		}
		super.formInnerEvent(ureq, source, event);
	}
}
