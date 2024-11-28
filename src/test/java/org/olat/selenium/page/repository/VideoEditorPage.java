/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.selenium.page.repository;

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 8 nov. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VideoEditorPage {
	
	private WebDriver browser;
	
	public VideoEditorPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public VideoEditorPage assertOnVideoEditor() {
		// Wait a little to load the video
		OOGraphene.waitingLong();
		By videoBy = By.xpath("//div[@id='o_video_editor_video']//div[contains(@class,'o_video_run')]//mediaelementwrapper/video");
		OOGraphene.waitElement(videoBy, 10, browser);
		By videoControlsBy = By.cssSelector(".o_video_editor .o_video_run .mejs__controls .mejs__button.mejs__playpause-button.mejs__play");
		OOGraphene.waitElement(videoControlsBy, 5, browser);
		return this;
	}
	
	public VideoEditorPage selectSegments() {
		By segmentsBy = By.cssSelector("li.o_sel_video_segments>a");
		browser.findElement(segmentsBy).click();
		By segmentsEditorBy = By.cssSelector(".o_video_edit_segments .o_video_common_form_header_add_item");
		OOGraphene.waitElement(segmentsEditorBy, browser);
		return this;
	}
	
	public VideoEditorPage addSegment() {
		try {
			By addBy = By.cssSelector(".o_video_edit_segments .o_video_common_form_header_add_item a.btn");
			OOGraphene.waitElement(addBy, browser);
			browser.findElement(addBy).click();
			
			By startBy = By.cssSelector(".o_video_segment_end .o_video_apply_position_timestamp input[type='text']");
			OOGraphene.waitElement(startBy, browser);
		} catch (Exception e) {
			OOGraphene.takeScreenshot("Add segment", browser);
			throw e;
		}
		return this;
	}
	
	public VideoEditorPage editSegment(String start, String end) {
		try {
			By startBy = By.cssSelector(".o_video_segment_start .o_video_apply_position_timestamp input[type='text']");
			OOGraphene.waitElement(startBy, browser);
			browser.findElement(startBy).clear();
			browser.findElement(startBy).sendKeys(start);
			
			By endBy = By.cssSelector(".o_video_segment_end .o_video_apply_position_timestamp input[type='text']");
			browser.findElement(endBy).clear();
			browser.findElement(endBy).sendKeys(end);
		} catch (Exception e) {
			OOGraphene.takeScreenshot("Edit segment", browser);
			throw e;
		}
		return this;
	}
	
	public VideoEditorPage save() {
		By saveBy = By.cssSelector(".o_video_segment_buttons>button.btn.btn-primary");
		browser.findElement(saveBy).click();
		By dirtySaveBy = By.xpath("//div[contains(@class,'o_video_segment_buttons')]/button[contains(@class,'btn-primary') and not(contains(@class,'o_button_dirty'))]");
		OOGraphene.waitElement(dirtySaveBy, browser);
		return this;
	}
	
	public VideoPage toolbarBack() {
		OOGraphene.clickBreadcrumbBack(browser);
		return new VideoPage(browser);
	}

}
