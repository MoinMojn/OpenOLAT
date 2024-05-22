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
package org.olat.core.commons.services.ai.spi.openAI;

import java.util.Locale;

import org.olat.core.commons.services.text.TextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.github.sashirestela.openai.domain.chat.ChatMessage.SystemMessage;
import io.github.sashirestela.openai.domain.chat.ChatMessage.UserMessage;

/**
 * 
 * Prompt helper for OpenAI API
 * 
 * Initial date: 22.05.2024<br>
 * 
 * @author gnaegi@frentix.com, https://www.frentix.com
 *
 */
@Service
public class OpenAiPromptHelper {

	@Autowired
	private TextService textService;

//	String scTemplate_en = "Create a short summary title for the text. Then create a multiple choice question with 1 correct and 4 wrong answers. "
//			+ "Use the following format:" + "\n<sc>" + "\n<title>Title</title>" + "\n<question>Question</question>"
//			+ "\n<correct>Answer 1</correct>" + "\n<wrong>Answer 2</wrong>" + "</sc>";
//	String fibTemplate_en = "Search for 5 keywords in the text and create a gap text with it. Put the keywords in [].";
//
//	String mcTemplate_de = "Fasse den Text in einem kurzen Titel zusammen. Dann erstelle eine Multiple-Choice Frage mit 2 korrekten und 3 falchen Antworten."
//			+ "\nBenutze das folgende Format: " 
//			+ "\n<mc>" 
//			+ "\n<title>Titel</title>"
//			+ "\n<keywords>Schlüsselwörter</keywords>"
//			+ "\n<question>Frage</question>" 
//			+ "\n<correct>Korrekte Antwort</correct>"
//			+ "\n<wrong>Falsche Antwort</wrong>" 
//			+ "</mc>";
//	String scTemplate_de = "Fasse den Text in einem kurzen Titel zusammen. Erstelle eine Multiple-Choice Frage mit 1 korrekten und 4 falchen Antworten."
//			+ "nBeutze das folgende Format:" + "\n<sc>" + "\n<title>Titel</title>" + "\n<question>Frage</question>"
//			+ "\n<correct>Korrekte Antwort</correct>" + "\n<wrong>Falsche Antwort</wrong>" + "</sc>";
//	String fibTemplate_de = "Suche 5 Schlüsselwörter in dem Text und erstelle einen Lückentext. Setze die Schlüsselwörter in [].";

	
	/**
	 * Try to detect the language for the given input.
	 * 
	 * @param input the input text for the chat
	 * @return The locale if a supported language has been detected or NULL otherwise
	 */
	Locale detectSupportedLocale(String input) {
		Locale locale = textService.detectLocale(input);
		if (locale != null 
				&& locale.getLanguage().equals("en") || locale.getLanguage().equals("de")) {
			return locale;
		} 
		return null;		
	}
		
	/**
	 * Create a system prompt for question generation for the given locale
	 * 
	 * @param locale
	 * @return
	 */
	SystemMessage createQuestionSystemMessage(Locale locale) {
		if (locale != null && locale.getLanguage().equals("en")) {
			return SystemMessage.of("You are an assistant to create assessment questions based on text input.");
		} else if (locale != null && locale.getLanguage().equals("de")) {
			return SystemMessage.of("Du bitst ein Helfer um Testfragen aus einem Texten zu erstellen.");
		} 
		return null;
	}


	/**
	 * Create the user prompt for the generation of a multiple choice question.
	 * 
	 * @param input   The input text that serves as the knowledge base for the
	 *                question
	 * @param number  The number of questions to be generated
	 * @param correct The number of correct answers
	 * @param wrong   The number of wrong answers
	 * @param locale  The prompt locale (must be same as input)
	 * @return
	 */
	UserMessage createChoiceQuestionUserMessage(String input, int number, int correct, int wrong, Locale locale) {
		if (locale != null && locale.getLanguage().equals("en")) {
			return UserMessage.of("""
						Create a short summary title for the text. Then create %s different multiple choice question with %s correct and %s wrong answers.
						Format: 
						<items> 
							<item>
							 	<title>Title</title>
							 	<topic>Topic</topic>
								<keywords>Keywords</keywords>
							 	<question>Question</question>
								<correct>Correct answer</correct>
								<wrong>Wrong answer</wrong>
							</item>
						</items>
						
						Use this text for the questions:
						
						-----
						
						""".formatted(correct, wrong) + input);
		} else if (locale != null && locale.getLanguage().equals("de")) {
			return UserMessage.of("""
						Fasse den Text in einem kurzen Titel zusammen. Dann erstelle %s verschiedene Multiple-Choice Fragen mit %s korrekten und %s falschen Antworten.
						Benutze das folgende Format:
						<items> 
							<item> 
								<title>Titel</title>
							 	<topic>Thema</topic>
								<keywords>Schlüsselwörter</keywords>
								<question>Frage</question>
								<correct>Korrekte Antwort</correct>
								<wrong>Falsche Antwort</wrong>
							</item>
						</items>
						
						Verwende diesen Text für die Fragen:
							
						-----
							
						""".formatted(number, correct, wrong) + input);
		} 
		return null;
	}

}
