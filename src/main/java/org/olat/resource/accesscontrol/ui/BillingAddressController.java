/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.resource.accesscontrol.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.util.StringHelper;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.BillingAddress;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 1 Nov 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class BillingAddressController extends FormBasicController implements Controller {

	private TextElement identifierEl;
	private TextElement nameLine1El;
	private TextElement nameLine2El;
	private TextElement addressLine1El;
	private TextElement addressLine2El;
	private TextElement addressLine3El;
	private TextElement addressLine4El;
	private TextElement poBoxEl;
	private TextElement regionEl;
	private TextElement zipEl;
	private TextElement cityEl;
	private TextElement countryEl;
	private FormToggle enabledEl;

	private BillingAddress billingAddress;
	private final Organisation organisation;
	private final Identity addressIdentity;
	
	@Autowired
	private ACService acService;
	
	public BillingAddressController(UserRequest ureq, WindowControl wControl, BillingAddress billingAddress, Organisation organisation, Identity addressIdentity) {
		super(ureq, wControl);
		this.billingAddress = billingAddress;
		this.organisation = organisation;
		this.addressIdentity = addressIdentity;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if (billingAddress != null && ureq.getUserSession().getRoles().isAdministrator()) {
			uifactory.addStaticTextElement("billing.address.id", billingAddress.getKey().toString(), formLayout);
		}
		
		String identifier = billingAddress != null? billingAddress.getIdentifier(): null;
		identifierEl = uifactory.addTextElement("billing.address.identifier", 255, identifier, formLayout);
		identifierEl.setMandatory(true);
		
		String nameLine1 = billingAddress != null? billingAddress.getNameLine1(): null;
		nameLine1El = uifactory.addTextElement("billing.address.name.line1", 255, nameLine1, formLayout);
		
		String nameLine2 = billingAddress != null? billingAddress.getNameLine2(): null;
		nameLine2El = uifactory.addTextElement("billing.address.name.line2", 255, nameLine2, formLayout);
		
		String addressLine1 = billingAddress != null? billingAddress.getAddressLine1(): null;
		addressLine1El = uifactory.addTextElement("billing.address.address.line1", 255, addressLine1, formLayout);
		
		String addressLine2 = billingAddress != null? billingAddress.getAddressLine2(): null;
		addressLine2El = uifactory.addTextElement("billing.address.address.line2", 255, addressLine2, formLayout);
		
		String addressLine3 = billingAddress != null? billingAddress.getAddressLine3(): null;
		addressLine3El = uifactory.addTextElement("billing.address.address.line3", 255, addressLine3, formLayout);
		
		String addressLine4 = billingAddress != null? billingAddress.getAddressLine4(): null;
		addressLine4El = uifactory.addTextElement("billing.address.address.line4", 255, addressLine4, formLayout);
		
		String poBox = billingAddress != null? billingAddress.getPoBox(): null;
		poBoxEl = uifactory.addTextElement("billing.address.pobox", 255, poBox, formLayout);
		
		String region = billingAddress != null? billingAddress.getRegion(): null;
		regionEl = uifactory.addTextElement("billing.address.region", 255, region, formLayout);
		
		String zip = billingAddress != null? billingAddress.getZip(): null;
		zipEl = uifactory.addTextElement("billing.address.zip", 255, zip, formLayout);
		
		String city = billingAddress != null? billingAddress.getCity(): null;
		cityEl = uifactory.addTextElement("billing.address.city", 255, city, formLayout);
		
		String country = billingAddress != null? billingAddress.getCountry(): null;
		countryEl = uifactory.addTextElement("billing.address.country", 255, country, formLayout);
		
		enabledEl = uifactory.addToggleButton("enabled", "billing.address.enabled", translate("on"), translate("off"), formLayout);
		enabledEl.setEnabled(billingAddress == null);
		if (billingAddress == null || billingAddress.isEnabled()) {
			enabledEl.toggleOn();
		}
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		identifierEl.clearError();
		if (!StringHelper.containsNonWhitespace(identifierEl.getValue())) {
			identifierEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		return allOk;
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		if (billingAddress == null) {
			billingAddress = acService.createBillingAddress(organisation, addressIdentity);
		}
		
		billingAddress.setIdentifier(identifierEl.getValue());
		billingAddress.setNameLine1(nameLine1El.getValue());
		billingAddress.setNameLine2(nameLine2El.getValue());
		billingAddress.setAddressLine1(addressLine1El.getValue());
		billingAddress.setAddressLine2(addressLine2El.getValue());
		billingAddress.setAddressLine3(addressLine3El.getValue());
		billingAddress.setAddressLine4(addressLine4El.getValue());
		billingAddress.setPoBox(poBoxEl.getValue());
		billingAddress.setRegion(regionEl.getValue());
		billingAddress.setZip(zipEl.getValue());
		billingAddress.setCity(cityEl.getValue());
		billingAddress.setCountry(countryEl.getValue());
		billingAddress.setEnabled(enabledEl.isOn());
		
		billingAddress = acService.updateBillingAddress(billingAddress);
		
		fireEvent(ureq, Event.CHANGED_EVENT);
	}

}
