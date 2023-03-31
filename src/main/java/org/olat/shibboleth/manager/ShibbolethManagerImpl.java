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
package org.olat.shibboleth.manager;

import java.util.ArrayList;
import java.util.List;

import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.manager.AuthenticationDAO;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.User;
import org.olat.core.util.StringHelper;
import org.olat.login.auth.AuthenticationProviderSPI;
import org.olat.login.validation.AllOkValidationResult;
import org.olat.login.validation.ValidationResult;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.provider.auto.AutoAccessManager;
import org.olat.shibboleth.ShibbolethDispatcher;
import org.olat.shibboleth.ShibbolethManager;
import org.olat.shibboleth.ShibbolethModule;
import org.olat.shibboleth.ShibbolethOrganisationStrategy;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * Initial date: 19.07.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class ShibbolethManagerImpl implements ShibbolethManager, AuthenticationProviderSPI {

	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private AccessControlModule acModule;
	@Autowired
	private UserManager userManager;
	@Autowired
	private AuthenticationDAO authenticationDao;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private AutoAccessManager autoAccessManager;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private ShibbolethModule shibbolethModule;

	@Override
	public List<String> getProviderNames() {
		return List.of(ShibbolethDispatcher.PROVIDER_SHIB);
	}

	@Override
	public boolean canAddAuthenticationUsername(String provider) {
		return canChangeAuthenticationUsername(provider);
	}

	@Override
	public boolean canChangeAuthenticationUsername(String provider) {
		return shibbolethModule.isEnabled() && ShibbolethDispatcher.PROVIDER_SHIB.equals(provider);
	}

	@Override
	public boolean changeAuthenticationUsername(Authentication authentication, String newUsername) {
		authentication.setAuthusername(newUsername);
		authentication = authenticationDao.updateAuthentication(authentication);
		return authentication != null;
	}

	@Override
	public ValidationResult validateAuthenticationUsername(String name, String provider, Identity identity) {
		return new AllOkValidationResult();
	}

	@Override
	public Identity createUser(String username, String shibbolethUniqueID, String language, ShibbolethAttributes shibbolethAttributes) {
		if (shibbolethAttributes == null) return null;

		List<Organisation> organisations = getOrganisations(shibbolethAttributes);
		Organisation organisation = organisations == null  || organisations.isEmpty() ? null : organisations.get(0);
		Identity identity = createUserAndPersist(username, shibbolethUniqueID, language, organisation, shibbolethAttributes);
		if(organisations != null && organisations.size() > 1) {
			for(int i=1; i<organisations.size(); i++) {
				organisationService.addMember(organisations.get(i), identity, OrganisationRoles.user);
			}
		}
		addToAuthorsGroup(identity, organisations, shibbolethAttributes);
		createAndBookAdvanceOrders(identity, shibbolethAttributes);
		return identity;
	}

	private Identity createUserAndPersist(String username, String shibbolethUniqueID, String language, Organisation organisation, ShibbolethAttributes shibbolethAttributes) {
		User user = userManager.createUser(null, null, null);
		user = shibbolethAttributes.syncUser(user);
		user.getPreferences().setLanguage(language);
		String identityName = securityModule.isIdentityNameAutoGenerated() ? null : username;
		return securityManager.createAndPersistIdentityAndUserWithOrganisation(identityName, username, null, user,
				ShibbolethDispatcher.PROVIDER_SHIB, BaseSecurity.DEFAULT_ISSUER, shibbolethUniqueID, null, organisation, null);
	}
	
	protected List<Organisation> getOrganisations(ShibbolethAttributes shibbolethAttributes) {
		ShibbolethOrganisationStrategy strategy = shibbolethModule.getOrganisationStrategy();
		if(strategy == null) {
			return null;
		}
		
		List<Organisation> organisations = new ArrayList<>();
		String defOrganisation = shibbolethModule.getDefaultOrganisation();
		if((strategy == ShibbolethOrganisationStrategy.def || strategy == ShibbolethOrganisationStrategy.both)
				&& StringHelper.containsNonWhitespace(defOrganisation)) {
			List<Organisation> defOrganisations = organisationService.findOrganisationByIdentifier(defOrganisation);
			organisations.addAll(defOrganisations);
		}

		String organisationAttr = shibbolethModule.getShibbolethOrganisation();
		if((strategy == ShibbolethOrganisationStrategy.shib || strategy == ShibbolethOrganisationStrategy.both)
				&& StringHelper.containsNonWhitespace(organisationAttr)) {
			String organisationValue = shibbolethAttributes.getValueForAttributeName(organisationAttr);
			if(StringHelper.containsNonWhitespace(organisationValue)) {
				List<Organisation> defOrganisations = organisationService.findOrganisationByIdentifier(organisationValue);
				organisations.addAll(defOrganisations);
			}
		}
		return organisations;
	}

	/**
	 * The method checks the configuration and add the roles to the specified organisatio
	 * or if no organisation is configured, to the default one.
	 * 
	 * @param identity The identity
	 * @param organisation The organization if any extra defined
	 * @param shibbolethAttributes The shibboleth attributes of the user
	 */
	private void addToAuthorsGroup(Identity identity, List<Organisation> organisations, ShibbolethAttributes shibbolethAttributes) {
		if (shibbolethAttributes.isAuthor() && isNotInAuthorsGroup(identity)) {
			if(organisations == null || organisations.isEmpty()) {
				// Choose the default organization
				organisationService.addMember(identity, OrganisationRoles.author);
			} else {
				for(Organisation organisation:organisations) {
					organisationService.addMember(organisation, identity, OrganisationRoles.author);
				}
			}
		}
	}

	private boolean isNotInAuthorsGroup(Identity identity) {
		return !organisationService.hasRole(identity, OrganisationRoles.author);
	}

	private void createAndBookAdvanceOrders(Identity identity, ShibbolethAttributes shibbolethAttributes) {
		if (acModule.isAutoEnabled()) {
			createAdvanceOrder(identity, shibbolethAttributes);
			autoAccessManager.grantAccessToCourse(identity);
		}
	}

	private void createAdvanceOrder(Identity identity, ShibbolethAttributes shibbolethAttributes) {
		ShibbolethAdvanceOrderInput input = getShibbolethAdvanceOrderInput();
		input.setIdentity(identity);
		String rawValues = shibbolethAttributes.getAcRawValues();
		input.setRawValues(rawValues);
		autoAccessManager.createAdvanceOrders(input);
	}

	@Override
	public void syncUser(Identity identity, ShibbolethAttributes shibbolethAttributes) {
		if (identity == null || shibbolethAttributes == null) {
			return;
		}

		User user = identity.getUser();
		List<Organisation> organisations = getOrganisations(shibbolethAttributes);
		syncAndPersistUser(identity, user, shibbolethAttributes);
		syncOrganisations(identity, organisations);
		addToAuthorsGroup(identity, organisations, shibbolethAttributes);
		createAndBookAdvanceOrders(identity, shibbolethAttributes);
	}

	private void syncAndPersistUser(Identity identity, User user, ShibbolethAttributes shibbolethAttributes) {
		if (shibbolethAttributes.hasDifference(user)) {
			User syncedUser = shibbolethAttributes.syncUser(user);
			userManager.updateUser(identity, syncedUser);
		}
	}

	private void syncOrganisations(Identity identity, List<Organisation> organisations) {
		if (organisations != null && !organisations.isEmpty()) {
			for(Organisation organisation:organisations) {
				organisationService.addMember(organisation, identity, OrganisationRoles.user);
			}
		}
	}

	/**
	 * Because the static method of the CoreSpringFactory can not be mocked.
	 */
	protected ShibbolethAdvanceOrderInput getShibbolethAdvanceOrderInput() {
		return CoreSpringFactory.getImpl(ShibbolethAdvanceOrderInput.class);
	}

}
