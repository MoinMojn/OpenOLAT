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
package org.olat.modules.curriculum.ui;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementManagedFlag;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumElementTypeRef;
import org.olat.modules.curriculum.CurriculumLearningProgress;
import org.olat.modules.curriculum.CurriculumLectures;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementTypeRefImpl;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.model.TaxonomyLevelSearchParameters;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.modules.taxonomy.ui.component.TaxonomyLevelDepthComparator;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 16 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditCurriculumElementController extends FormBasicController {
	
	private static final String[] calendarsKeys = new String[] {
			CurriculumCalendars.enabled.name(), CurriculumCalendars.disabled.name()
		};
	
	private static final String[] calendarsTypedKeys = new String[] {
			CurriculumCalendars.enabled.name(), CurriculumCalendars.disabled.name(), CurriculumCalendars.inherited.name()
		};
	
	private static final String[] lecturesKeys = new String[] {
			CurriculumLectures.enabled.name(), CurriculumLectures.disabled.name()
		};
	
	private static final String[] lecturesTypedKeys = new String[] {
			CurriculumLectures.enabled.name(), CurriculumLectures.disabled.name(), CurriculumLectures.inherited.name()
		};
	
	private static final String[] learningProgressKeys = new String[] {
			CurriculumLearningProgress.enabled.name(), CurriculumLearningProgress.disabled.name()
		};
	
	private static final String[] learningProgressTypedKeys = new String[] {
			CurriculumLearningProgress.enabled.name(), CurriculumLearningProgress.disabled.name(), CurriculumLearningProgress.inherited.name()
		};


	private DateChooser endEl;
	private DateChooser beginEl;
	private TextElement identifierEl;
	private TextElement displayNameEl;
	private RichTextElement descriptionEl;

	private SingleSelection statusEl;
	private SingleSelection lecturesEnabledEl;
	private SingleSelection calendarsEnabledEl;
	private SingleSelection learningProgressEnabledEl;
	private SingleSelection curriculumElementTypeEl;
	
	private MultipleSelectionElement subjectsEl;
	
	private Curriculum curriculum;
	private CurriculumElement element;
	private CurriculumElement parentElement;
	private final CurriculumElementType preSelectedType;
	private final CurriculumSecurityCallback secCallback;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private TaxonomyService taxonomyService;
	
	/**
	 * Create a new curriculum element.
	 * 
	 * @param ureq The user request
	 * @param wControl The window control
	 */
	public EditCurriculumElementController(UserRequest ureq, WindowControl wControl,
			CurriculumElement parentElement, CurriculumElementType preSelectedType, Curriculum curriculum, CurriculumSecurityCallback secCallback) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(TaxonomyUIFactory.class, getLocale(), getTranslator()));
		this.curriculum = curriculum;
		this.parentElement = parentElement;
		this.secCallback = secCallback;
		this.preSelectedType = preSelectedType;
		initForm(ureq);
	}
	
	public EditCurriculumElementController(UserRequest ureq, WindowControl wControl,
			CurriculumElement element, CurriculumElement parentElement, Curriculum curriculum, CurriculumSecurityCallback secCallback) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(TaxonomyUIFactory.class, getLocale(), getTranslator()));
		this.curriculum = curriculum;
		this.element = element;
		this.parentElement = parentElement;
		this.preSelectedType = null;
		this.secCallback = secCallback;
		initForm(ureq);
		updateStatusWarning(ureq);
	}
	
	public CurriculumElement getCurriculumElement() {
		return element;
	}

	public CurriculumElement getParentElement() {
		return parentElement;
	}

	public void setParentElement(CurriculumElement parentElement) {
		this.parentElement = parentElement;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(element != null) {
			String key = element.getKey().toString();
			uifactory.addStaticTextElement("curriculum.element.key", key, formLayout);
			String externalId = element.getExternalId();
			uifactory.addStaticTextElement("curriculum.element.external.id", externalId, formLayout);
		}
		
		boolean canEdit = element == null || secCallback.canEditCurriculumElement(element);

		String displayName = element == null ? "" : element.getDisplayName();
		displayNameEl = uifactory.addTextElement("displayName", "curriculum.element.displayName", 255, displayName, formLayout);
		displayNameEl.setEnabled(!CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.displayName) && canEdit);
		displayNameEl.setMandatory(true);
		if(displayNameEl.isEnabled() && !StringHelper.containsNonWhitespace(displayName)) {
			displayNameEl.setFocus(true);
		}
		
		String identifier = element == null ? "" : element.getIdentifier();
		identifierEl = uifactory.addTextElement("identifier", "curriculum.element.identifier", 64, identifier, formLayout);
		identifierEl.setEnabled(!CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.identifier) && canEdit);
		identifierEl.setMandatory(true);
		
		// Status
		SelectionValues statusPK = new SelectionValues();
		for(CurriculumElementStatus status:CurriculumElementStatus.values()) {
			if(status == CurriculumElementStatus.inactive) continue;
			statusPK.add(SelectionValues.entry(status.name(), translate("status." + status.name())));
		}
		statusEl = uifactory.addRadiosHorizontal("status", "curriculum.element.status", formLayout, statusPK.keys(), statusPK.values());
		statusEl.setEnabled(!CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.status) && canEdit);
		statusEl.addActionListener(FormEvent.ONCHANGE);
		if(element == null || element.getElementStatus() == null) {
			statusEl.select(CurriculumElementStatus.active.name(), true);
		} else {
			statusEl.select(element.getElementStatus().name(), true);
		}
		
		// Element type
		List<CurriculumElementType> types = getTypes();
		SelectionValues typePK = new SelectionValues();
		for(CurriculumElementType type:types) {
			typePK.add(SelectionValues.entry(type.getKey().toString(), StringHelper.escapeHtml(type.getDisplayName())));
		}
		curriculumElementTypeEl = uifactory.addDropdownSingleselect("type", "curriculum.element.type", formLayout, typePK.keys(), typePK.values());
		curriculumElementTypeEl.setEnabled(!CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.type) && canEdit);
		curriculumElementTypeEl.addActionListener(FormEvent.ONCHANGE);
		curriculumElementTypeEl.setMandatory(true);
		boolean typeFound = false;
		CurriculumElementType elementType = element == null ? preSelectedType : element.getType();
		if(elementType != null) {
			String selectedTypeKey = elementType.getKey().toString();
			if(typePK.containsKey(selectedTypeKey)) {
				curriculumElementTypeEl.select(selectedTypeKey, true);
				typeFound = true;
			}
		}
		if(!typeFound) {
			for(CurriculumElementType type:types) {
				if(CurriculumService.DEFAULT_CURRICULUM_ELEMENT_TYPE.equals(type.getExternalId())) {
					curriculumElementTypeEl.select(type.getKey().toString(), true);
				}
			}
		}
		
		// Subjects
		TaxonomyLevelSearchParameters filter = new TaxonomyLevelSearchParameters();
		
		List<TaxonomyLevel> taxonomyLevels = taxonomyService.getTaxonomyLevels(null, filter);
		List<TaxonomyLevel> selectedSubjects = curriculumService.getTaxonomy(element);
		
		if (!taxonomyLevels.isEmpty()) {
			subjectsEl = uifactory.addCheckboxesDropdown("subjects", formLayout);
			Set<Taxonomy> taxonomies = taxonomyLevels.stream().map(TaxonomyLevel::getTaxonomy).collect(Collectors.toSet());
			Set<String> disabledEntries = new HashSet<>();
			
			String[] keys = new String[taxonomyLevels.size() + taxonomies.size()];
			String[] values = new String[taxonomyLevels.size() + taxonomies.size()];
			int index = 0;
			
			for (Taxonomy taxonomy : taxonomies) {
				keys[index] = "Taxonomy_" + taxonomy.getKey();
				values[index] = taxonomy.getDisplayName();
				disabledEntries.add("Taxonomy_" + taxonomy.getKey());
				
				index++;
				
				List<TaxonomyLevel> relatedLevels = taxonomyLevels.stream().filter(level -> level.getTaxonomy().equals(taxonomy)).collect(Collectors.toList());
				
				Collections.sort(relatedLevels, new TaxonomyLevelDepthComparator());
				
				for (TaxonomyLevel level : relatedLevels) {
					keys[index] = level.getKey().toString();
					values[index] = level.getMaterializedPathIdentifiersWithoutSlash();
					
					index++;
				}
			}
			
			subjectsEl.setKeysAndValues(keys, values);
			subjectsEl.setEnabled(disabledEntries, false);
			
			if (!selectedSubjects.isEmpty()) {
				for (TaxonomyLevel selectedLevel : selectedSubjects) {
					subjectsEl.select(selectedLevel.getKey().toString(), true);
				}
			}
		}
		
		calendarsEnabledEl = uifactory.addRadiosHorizontal("type.calendars.enabled", formLayout, new String[0], new String[0]);
		calendarsEnabledEl.setEnabled(!CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.calendars) && canEdit);
		CurriculumCalendars calendarsEnabled =  element == null ? CurriculumCalendars.inherited : element.getCalendars();
		updateCalendarsEnabled(calendarsEnabled, elementType);
		
		lecturesEnabledEl = uifactory.addRadiosHorizontal("type.lectures.enabled", formLayout, new String[0], new String[0]);
		lecturesEnabledEl.setEnabled(!CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.lectures) && canEdit);
		CurriculumLectures lecturesEnabled =  element == null ? CurriculumLectures.inherited : element.getLectures();
		updateLecturesEnabled(lecturesEnabled, elementType);
		
		learningProgressEnabledEl = uifactory.addRadiosHorizontal("type.learning.progress.enabled", formLayout, new String[0], new String[0]);
		learningProgressEnabledEl.setEnabled(!CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.learningProgress) && canEdit);
		CurriculumLearningProgress learningProgressEnabled =  element == null ? CurriculumLearningProgress.inherited : element.getLearningProgress();
		updateLearningProgressEnabled(learningProgressEnabled, elementType);
		
		List<TaxonomyLevel> levels = curriculumService.getTaxonomy(element);
		if(!levels.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			for(TaxonomyLevel level:levels) {
				if(sb.length() > 0) sb.append(", ");
				sb.append(StringHelper.escapeHtml(TaxonomyUIFactory.translateDisplayName(getTranslator(), level)));
			}
			uifactory.addStaticTextElement("curriculum.element.taxonomy", sb.toString(), formLayout);
		}
		
		Date begin = element == null ? null : element.getBeginDate();
		beginEl = uifactory.addDateChooser("start", "curriculum.element.begin", begin, formLayout);
		beginEl.setEnabled(!CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.dates) && canEdit);
		beginEl.addActionListener(FormEvent.ONCHANGE);

		Date end = element == null ? null : element.getEndDate();
		endEl = uifactory.addDateChooser("end", "curriculum.element.end", end, formLayout);
		endEl.setEnabled(!CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.dates) && canEdit);
		endEl.addActionListener(FormEvent.ONCHANGE);
		endEl.setDefaultValue(beginEl);
		
		String description = element == null ? "" : element.getDescription();
		descriptionEl = uifactory.addRichTextElementForStringDataCompact("curriculum.description", "curriculum.description", description, 10, 60, null,
				formLayout, ureq.getUserSession(), getWindowControl());
		descriptionEl.setEnabled(!CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.description) && canEdit);

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		if(canEdit) {
			uifactory.addFormSubmitButton("save", buttonsCont);
		}
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	private void updateCalendarsEnabled(CurriculumCalendars preferedEnabled, CurriculumElementType selectedType) {
		if(curriculumElementTypeEl.getSelected() == 0) {
			String[] onValues = new String[] {
					translate("type.calendars.enabled.enabled"), translate("type.calendars.enabled.disabled")
			};
			calendarsEnabledEl.setKeysAndValues(calendarsKeys, onValues, null);
			
			if(preferedEnabled == CurriculumCalendars.enabled || preferedEnabled == CurriculumCalendars.disabled) {
				calendarsEnabledEl.select(preferedEnabled.name(), true);
			} else {
				calendarsEnabledEl.select(CurriculumCalendars.disabled.name(), true);
			}
		} else {
			String typeVal = null;
			if(selectedType == null) {
				typeVal = "???";
			} else if(selectedType.getCalendars() == CurriculumCalendars.enabled) {
				typeVal = translate("type.calendars.enabled.enabled");
			} else if(selectedType.getCalendars() == CurriculumCalendars.disabled) {
				typeVal = translate("type.calendars.enabled.disabled");
			}

			String[] onValues = new String[] {
					translate("type.calendars.enabled.enabled"), translate("type.calendars.enabled.disabled"),
					translate("type.calendars.enabled.inherited", typeVal)
			};
			calendarsEnabledEl.setKeysAndValues(calendarsTypedKeys, onValues, null);
			calendarsEnabledEl.select(preferedEnabled.name(), true);
		}
	}
	
	private void updateLecturesEnabled(CurriculumLectures preferedEnabled, CurriculumElementType selectedType) {
		if(curriculumElementTypeEl.getSelected() == 0) {
			String[] onValues = new String[] {
					translate("type.lectures.enabled.enabled"), translate("type.lectures.enabled.disabled")
			};
			lecturesEnabledEl.setKeysAndValues(lecturesKeys, onValues, null);
			
			if(preferedEnabled == CurriculumLectures.enabled || preferedEnabled == CurriculumLectures.disabled) {
				lecturesEnabledEl.select(preferedEnabled.name(), true);
			} else {
				lecturesEnabledEl.select(CurriculumLectures.disabled.name(), true);
			}
		} else {
			String typeVal = null;
			if(selectedType == null) {
				typeVal = "???";
			} else if(selectedType.getLectures() == CurriculumLectures.enabled) {
				typeVal = translate("type.lectures.enabled.enabled");
			} else if(selectedType.getLectures() == CurriculumLectures.disabled) {
				typeVal = translate("type.lectures.enabled.disabled");
			}

			String[] onValues = new String[] {
					translate("type.lectures.enabled.enabled"), translate("type.lectures.enabled.disabled"),
					translate("type.lectures.enabled.inherited", new String[] { typeVal })
			};
			lecturesEnabledEl.setKeysAndValues(lecturesTypedKeys, onValues, null);
			lecturesEnabledEl.select(preferedEnabled.name(), true);
		}
	}
	
	private void updateLearningProgressEnabled(CurriculumLearningProgress preferedEnabled, CurriculumElementType selectedType) {
		if(curriculumElementTypeEl.getSelected() == 0) {
			String[] onValues = new String[] {
					translate("type.learning.progress.enabled.enabled"), translate("type.learning.progress.enabled.disabled")
			};
			learningProgressEnabledEl.setKeysAndValues(learningProgressKeys, onValues, null);
			
			if(preferedEnabled == CurriculumLearningProgress.enabled || preferedEnabled == CurriculumLearningProgress.disabled) {
				learningProgressEnabledEl.select(preferedEnabled.name(), true);
			} else {
				learningProgressEnabledEl.select(CurriculumLearningProgress.disabled.name(), true);
			}
		} else {
			String typeVal = null;
			if(selectedType == null) {
				typeVal = "???";
			} else if(selectedType.getLearningProgress() == CurriculumLearningProgress.enabled) {
				typeVal = translate("type.learning.progress.enabled.enabled");
			} else if(selectedType.getLearningProgress() == CurriculumLearningProgress.disabled) {
				typeVal = translate("type.learning.progress.enabled.disabled");
			}

			String[] onValues = new String[] {
					translate("type.learning.progress.enabled.enabled"), translate("type.learning.progress.enabled.disabled"),
					translate("type.learning.progress.enabled.inherited", typeVal)
			};
			learningProgressEnabledEl.setKeysAndValues(learningProgressTypedKeys, onValues, null);
			learningProgressEnabledEl.select(preferedEnabled.name(), true);
		}
	}
	
	private void updateStatusWarning(UserRequest ureq) {
		statusEl.clearWarning();
		if(!statusEl.isOneSelected()) return;
		
		CurriculumElementStatus status = CurriculumElementStatus.valueOf(statusEl.getSelectedKey());
		if(status == CurriculumElementStatus.preparation || status == CurriculumElementStatus.provisional) {
			Date begin = beginEl.getDate();
			if(begin != null && ureq.getRequestTimestamp().compareTo(begin) > 0) {
				statusEl.setWarningKey("warning.date.preparation.status", translate("status." + status.name()));
			}
		} else if(status == CurriculumElementStatus.confirmed || status == CurriculumElementStatus.active) {
			Date end = endEl.getDate();
			if(end != null && ureq.getRequestTimestamp().compareTo(end) > 0) {
				statusEl.setWarningKey("warning.date.active.status", translate("status." + status.name()));
			}
		} else if(status == CurriculumElementStatus.finished) {
			Date end = endEl.getDate();
			if(end != null && ureq.getRequestTimestamp().compareTo(end) < 0) {
				statusEl.setWarningKey("warning.date.finished.status", translate("status." + status.name()));
			}
		}
		
		
	}
	
	private List<CurriculumElementType> getTypes() {
		List<CurriculumElementType> types = curriculumService.getAllowedCurriculumElementType(parentElement, element);
		if(types.isEmpty()) {
			CurriculumElementType defaultType = curriculumService.getDefaultCurriculumElementType();
			if(defaultType != null) {
				types.add(defaultType);
			}
		}
		if(element != null && element.getType() != null && !types.contains(element.getType())) {
			types.add(element.getType());
		}
		return types;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		displayNameEl.clearError();
		if(!StringHelper.containsNonWhitespace(displayNameEl.getValue())) {
			displayNameEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		} else if(displayNameEl.getValue().length() > 255) {
			displayNameEl.setErrorKey("form.error.toolong", "255");
			allOk &= false;
		}
		
		identifierEl.clearError();
		if(!StringHelper.containsNonWhitespace(identifierEl.getValue())) {
			identifierEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		} else if(identifierEl.getValue().length() > 64) {
			identifierEl.setErrorKey("form.error.toolong", "64");
			allOk &= false;
		}
		
		curriculumElementTypeEl.clearError();
		if(!curriculumElementTypeEl.isOneSelected()) {
			displayNameEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		calendarsEnabledEl.clearError();
		if(!calendarsEnabledEl.isOneSelected()) {
			calendarsEnabledEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		lecturesEnabledEl.clearError();
		if(!lecturesEnabledEl.isOneSelected()) {
			lecturesEnabledEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		learningProgressEnabledEl.clearError();
		if(!learningProgressEnabledEl.isOneSelected()) {
			learningProgressEnabledEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		statusEl.clearError();
		if(!statusEl.isOneSelected()) {
			statusEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		updateStatusWarning(ureq);
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		CurriculumElementType elementType = getSelectedType();
		CurriculumLectures lectures = CurriculumLectures.valueOf(lecturesEnabledEl.getSelectedKey());
		CurriculumCalendars calendars = CurriculumCalendars.valueOf(calendarsEnabledEl.getSelectedKey());
		CurriculumLearningProgress learningProgress = CurriculumLearningProgress.valueOf(learningProgressEnabledEl.getSelectedKey());
		CurriculumElementStatus status = CurriculumElementStatus.valueOf(statusEl.getSelectedKey());
		
		boolean create = element == null;
		if(create) {
			//create a new one
			element = curriculumService.createCurriculumElement(identifierEl.getValue(), displayNameEl.getValue(),
					status, beginEl.getDate(), endEl.getDate(), parentElement, elementType, calendars, lectures, learningProgress, curriculum);
		} else {
			element = curriculumService.getCurriculumElement(element);
			element.setIdentifier(identifierEl.getValue());
			element.setDisplayName(displayNameEl.getValue());
			element.setDescription(descriptionEl.getValue());
			element.setBeginDate(beginEl.getDate());
			element.setEndDate(endEl.getDate());
			element.setType(elementType);
			element.setCalendars(calendars);
			element.setLectures(lectures);
			element.setLearningProgress(learningProgress);
			element.setElementStatus(status);
			element = curriculumService.updateCurriculumElement(element);
		}
		
		if (subjectsEl != null) {
			Set<Long> taxonomyLevelKeys= new HashSet<>();
			Set<Long> addedTaxonomies= new HashSet<>();
			Set<Long> removedTaxonomies= new HashSet<>();
			
			for(String key : subjectsEl.getKeys()) {
				try {
					taxonomyLevelKeys.add(Long.valueOf(key));
					
					if (subjectsEl.isKeySelected(key)) {
						addedTaxonomies.add(Long.valueOf(key));
					} else {
						removedTaxonomies.add(Long.valueOf(key));
					}
				} catch(NumberFormatException e) {}
			}
			
			List<TaxonomyLevel> taxonomyLevels = taxonomyService.getTaxonomyLevelsByKeys(taxonomyLevelKeys);
			List<TaxonomyLevel> addedLevels = taxonomyLevels.stream().filter(level -> addedTaxonomies.contains(level.getKey())).collect(Collectors.toList());
			List<TaxonomyLevel> removedLevels = taxonomyLevels.stream().filter(level -> removedTaxonomies.contains(level.getKey())).collect(Collectors.toList());
			
			curriculumService.updateTaxonomyLevels(element, addedLevels, removedLevels);			
		}
		
		if(create && element.getParent() != null) {
			dbInstance.commit();
			CurriculumElement rootElement = curriculumService.getImplementationOf(element);
			curriculumService.numberRootCurriculumElement(rootElement);
		}
		
		dbInstance.commitAndCloseSession(); // need to relaod properly the tree
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private CurriculumElementType getSelectedType() {
		String selectedTypeKey = curriculumElementTypeEl.getSelectedKey();
		if(StringHelper.containsNonWhitespace(selectedTypeKey)) {
			CurriculumElementTypeRef ref = new CurriculumElementTypeRefImpl(Long.valueOf(selectedTypeKey));
			return curriculumService.getCurriculumElementType(ref);
		}
		return null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(statusEl == source || beginEl == source || endEl == source) {
			updateStatusWarning(ureq);
		} else if(source == curriculumElementTypeEl) {
			CurriculumElementType elementType = getSelectedType();
			if(calendarsEnabledEl.isOneSelected()) {
				CurriculumCalendars enabled = CurriculumCalendars.valueOf(calendarsEnabledEl.getSelectedKey());
				updateCalendarsEnabled(enabled, elementType);
			}
			if(lecturesEnabledEl.isOneSelected()) {
				CurriculumLectures enabled = CurriculumLectures.valueOf(lecturesEnabledEl.getSelectedKey());
				updateLecturesEnabled(enabled, elementType);
			}
			if(learningProgressEnabledEl.isOneSelected()) {
				CurriculumLearningProgress enabled = CurriculumLearningProgress.valueOf(learningProgressEnabledEl.getSelectedKey());
				updateLearningProgressEnabled(enabled, elementType);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}