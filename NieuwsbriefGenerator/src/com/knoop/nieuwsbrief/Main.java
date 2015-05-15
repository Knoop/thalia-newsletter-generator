package com.knoop.nieuwsbrief;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import reader.XMLReader;

import com.knoop.nieuwsbrief.content.Evenement;
import com.knoop.nieuwsbrief.content.Item.EmptyRequiredFieldException;
import com.knoop.nieuwsbrief.content.Item.NoTypeDefinedException;
import com.sun.org.apache.xerces.internal.impl.io.MalformedByteSequenceException;

import datatype.XMLNode;

public class Main {

	public static void main(String[] args) {

		/*
		 * Make it look regular
		 */
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		
		// Get a source. See if it was already given through arguments. 
		File sourceFile = getSourceFile(args);
		
		// If no source was given, use it. 
		if(sourceFile == null)
		{
			System.out.println("No source file chosen when prompt. Going to abort.");
			return;
		}
		else
			System.out.println("Selected source: "+sourceFile.getAbsolutePath());
		
		
		
		
		// Get a destination. If none given, stop
		File destinationFile = getDestinationFile(args);
		if(destinationFile == null)
		{
			System.out.println("No destination file chosen when prompt. Going to abort.");
			return;
		}
		else
			System.out.println("Selected destination: "+destinationFile.getAbsolutePath());
		
		// Simple value assignments
		Newsletter newsletter = null;
		
		int selected;
		boolean succesful;
		
		// Repeat the process of converting the input while it is unsuccesful, or unwanted.
		do
		{
			
			String message;
			try {
				newsletter = convert(sourceFile);
				message = makeConfirmation(newsletter);
				succesful = true;
				
			} catch(Exception e)
			{
				succesful = false;
				e.printStackTrace();
				
				message = "Onbekende fout.";
				
				if(e instanceof MalformedByteSequenceException)
					message = "Het bronbestand kan niet worden gelezen.\nHet is niet correct gecodeerd in UTF-8. \nMet notepad++ kan dit worden verholpen. Ga naar codering>UTF-8";
				else if(e instanceof NoTypeDefinedException)
					message = "Er is geen type aangegeven in item \""+ ((NoTypeDefinedException)e).getId()+"\".";
				else if(e instanceof EmptyRequiredFieldException)
					message = "Een van de vereiste velden was niet ingevuld. Het gaat om item \""+ ((EmptyRequiredFieldException)e).getId()+"\" en het veld \""+((EmptyRequiredFieldException)e).getField()+"\".";
				else if(e instanceof NullPointerException)
					message = e.getMessage();
				else if(e instanceof FileNotFoundException)
					message = "Kon bestand niet openen of niet gebruiken om in opslaan.\nHet bestand bestaat niet";
				else if(e instanceof SAXException)
					message = "Het bronbestand kan niet worden gelezen.\nHet bevat een fout in de XML opmaak.";
				else if(e instanceof IOException)
					message = "Het bronbestand kan niet worden gelezen. ";
				else if(e instanceof IllegalStateException || e instanceof ParserConfigurationException)
					message = "Het bronbestand kan niet worden gelezen.\nDit is een ongewone fout. Laat het weten als je deze melding ziet. ["+e.getClass().getSimpleName()+"]";
				else
					message = e.getClass().getSimpleName()+"\n"+e.getMessage();
				
			}
			
			System.out.println("Displaying message to user: ");
			System.out.println(message);
			
			selected = showMessage(succesful, message);
			
			logGenerationReport(succesful, selected);
			// Show additional prompt to confirm that required files have been edited. 
			if(!succesful)				
				selected = JOptionPane.showConfirmDialog(null, "Let op, voordat je opnieuw genereert moet je indien nodig eerst de bestanden bijwerken.\nDoe dat nuvoor je verder gaat." , "Waarschuwing", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
				
		}
		// We should try again if the user selected cancel when it succeded, or ok when it failed
		while((succesful && selected == JOptionPane.CANCEL_OPTION) || (!succesful && selected == JOptionPane.OK_OPTION));
		
		
		
		if(newsletter != null && succesful && selected != JOptionPane.CLOSED_OPTION)
		{
			try {
				performSave(newsletter, destinationFile);
				
			} catch (FileNotFoundException e) {
				
				System.out.println("Writing failed. Cause: "+e.getMessage()+".");
				dump(newsletter);
				
			}
			
			performCopyToClipboard(newsletter);
			
			
		}
		
		
	
		

	}
	
	
	private static void logGenerationReport(boolean succesful, int selectedoption)
	{
		if(succesful)
		{	
			System.out.println("The generation of the newsletter was succesful.");
			switch(selectedoption)
			{
			case JOptionPane.CANCEL_OPTION: System.out.println("The user decided to discard it, so it is not saved."); break;
			case JOptionPane.CLOSED_OPTION: System.out.println("The user closed the screen before confirming. Not going to save."); break;
			case JOptionPane.OK_OPTION: System.out.println("The user accepted the result. Going to save."); break;
			}
			
		}
		else
		{
			System.out.println("The generation of the newsletter failed.");
			switch(selectedoption)
			{
			case JOptionPane.CANCEL_OPTION: 
			case JOptionPane.CLOSED_OPTION: System.out.println("The user decided to stop. Nothing is saved."); break;
			case JOptionPane.OK_OPTION: System.out.println("The user decided to retry."); break;
			}
			
		}
	}
	
	
	private static File getSourceFile(String[]  args)
	{
		// Get a source. See if it was already given through arguments. 
		
		File sourceFile = null;
		if(args.length > 0 && args[0] != null && args[0].length() != 0)
		{	
			File given = new File(args[0]);
			if(given.exists())
				sourceFile = given;
			else	
				System.out.println("The provided source doesn't exist. Going to prompt for a file.");
			
		}
		// If no file was optained, prompt.
		if(sourceFile == null)
			sourceFile = pickSourceFile();
		
		return sourceFile;
	}
	
	private static File getDestinationFile(String[] args)
	{
		// Get a source. See if it was already given through arguments. 
		
		if(args.length > 1 && args[1] != null && args[1].length() != 0)
			return  new File(args[1]);
		else
			return pickDestinationFile();
		
		
		
				
	}
	
	/**
	 * Shows a message.
	 * @param succesful
	 * @param message
	 * @return
	 */
	private static int showMessage(boolean succesful, String message)
	{
		
		return JOptionPane.showConfirmDialog(null, message+"\n"+"Klik op "+(succesful?"ok":"cancel")+" om te sluiten. Klik op "+(succesful?"cancel":"ok")+" om opnieuw te genereren." , succesful?"Nieuwsbrief inhoud":"Foutmelding", JOptionPane.OK_CANCEL_OPTION, succesful?JOptionPane.PLAIN_MESSAGE:JOptionPane.ERROR_MESSAGE);
		
	}
	
	/**
	 * Converts the given file to a Newsletter.
	 * @param source
	 * @return
	 * @throws Exception
	 */
	private static Newsletter convert(File source) throws Exception
	{
		XMLNode sourceNode = readSource(source);
		
		if (source != null){
			return new Newsletter(sourceNode);
			
		} else 	{
			throw new IllegalStateException();
		}
	}
		
	private static File pickSourceFile()
	{
		return pickFile(false);
	}
	
	
	private static File pickDestinationFile()
	{
		return pickFile(true);
	}
	
	/**
	 * Picks a file
	 * @param save Whether to show  save dialog (true) or an open dialog(false)
	 * @return
	 */
	private static File pickFile(boolean save)
	{
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Nieuwsbrief", save?"html":"xml");
		chooser.setFileFilter(filter);

		if ((save?chooser.showSaveDialog(null):chooser.showOpenDialog(null)) == JFileChooser.APPROVE_OPTION)
			return chooser.getSelectedFile();
		else
			return null;
	}

	public static File getNewsletterTemplateFile() {
		return new File("templates/nieuwsbrief-template.html");
	}

	/**
	 * Creates a text that shows the content of the newsletter.
	 * @param newsletter
	 * @return
	 */
	private static String makeConfirmation(Newsletter newsletter) {
		StringBuilder sb = new StringBuilder(
				"De nieuwsbrief is gegenereerd met de volgende eigenschappen:\n");

		sb.append("Week-jaar: ")
				.append(newsletter.getWeeknumber() + "-" + newsletter.getYear())
				.append("\n");
		sb.append("kopjes: \n");
		for (Evenement event : newsletter.getEvents())
			sb.append("\t").append(event.getTitle()).append(" - ")
					.append(event.getDateForCalendar()).append("\n");

		sb.append("Inl. e-mail:").append(newsletter.getEmailClientPreheader())
				.append("\n");

		return sb.toString();

	}

	/**
	 * Reads the given source and turns it into an XMLNode
	 * @param source
	 * @return
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	private static XMLNode readSource(File source) throws SAXException, IOException, ParserConfigurationException {
		
			XMLReader reader = new XMLReader();
			
			reader.setInputStream(new FileInputStream(source));
			XMLNode newsletterNode = reader.read();
			return newsletterNode;
			
	}
	
	private static void dump(Newsletter newsletter)
	{
		int hashcode = newsletter.hashCode();
		System.out.println("Going to try to dump.");
		try {
			File dumpFolder = new File("dump");
			File dumpLoc = new File(dumpFolder, hashcode+".html"); 
			
			if(!dumpFolder.exists())
				dumpFolder.mkdirs();

			save(newsletter, dumpLoc);
					
			System.out.println("Dump succedeed. Location: "+dumpLoc.getAbsolutePath());
			JOptionPane.showMessageDialog(null, "Het bestand kon niet worden opgeslagen op de aangegeven plek. In plaats daarvan is het bestand opgeslagen als:\n"+dumpLoc.getAbsolutePath()+".", "Opslaan mislukt", JOptionPane.ERROR_MESSAGE);
		} catch (FileNotFoundException e1) {
			System.out.println("Dump failed. Dump location didn't exist.");
			JOptionPane.showMessageDialog(null, "Het bestand kon niet worden opgeslagen op de aangegeven plek en ook niet worden gedumpt. Start het programma opnieuw.", "Dump mislukt", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Performs safety checks before saving. It will prompt a dialog if the destination already existed and will notify the user if the file is not saved. 
	 * If you do not want to force saving to a file, call this rather than {@code save}.
	 * @param newsletter
	 * @param destination
	 * @throws FileNotFoundException
	 */
	private static void performSave(Newsletter newsletter, File destination) throws FileNotFoundException
	{
		if(destination.exists())
		{
			System.out.println("Destination existed. Going to prompt for confirmation.");
			int selected = JOptionPane.showConfirmDialog(null, "Het bestand bestaat al. Wil je het overschrijven? \nDit kan niet ongedaan worden gemaakt. Klik op ok om te overschrijven. Klik op cancel om een ander doelbestand te selecteren." , "Waarschuwing", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
			if(selected == JOptionPane.CLOSED_OPTION){
				System.out.println("User dismissed prompt. Going to stop without saving.");
				JOptionPane.showMessageDialog(null, "Het bestand is niet opgeslagen.", "Waarschuwing", JOptionPane.WARNING_MESSAGE);
				return;
			}
			else if(selected == JOptionPane.CANCEL_OPTION)
			{
				destination = pickDestinationFile();
				System.out.println("User choose to pick new file. "+(destination==null?"User decided not to save.":"New destination:"+destination.getAbsolutePath()));
			}
		}
		
		if(destination == null)
		{
			System.out.println("Destination was null. Can't save!");
			JOptionPane.showMessageDialog(null, "Het bestand is niet opgeslagen.", "Waarschuwing", JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		save(newsletter, destination);
			
	}
	/**
	 * Saves the given string to the file. This will override any content. To notify the user if a file already existed, use {@code performSave} instead.
	 * @param newsletter.toString()
	 * @param destination
	 * @throws FileNotFoundException
	 */
	private static void save(Newsletter newsletter, File destination) throws FileNotFoundException {
		
		System.out.println("Saving to "+destination.getAbsolutePath());
		PrintWriter out = new PrintWriter(destination);
		out.write(newsletter.toHTML());
		out.close();
			

		
	}
	
	private static void performCopyToClipboard(Newsletter newsletter)
	{
		int selected = JOptionPane.showConfirmDialog(null, "Inhoud van nieuwsbrief naar plakbord kopiëren?" , "Kopiëren naar plakbord", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
		System.out.println("Prompting user to copy newsletter to clipboard.");
		if(selected == JOptionPane.YES_OPTION)
			copyToClipboard(newsletter);
		else
			System.out.println("Newsletter not copied to clipboard.");
	}
	/**
	 * Copies the HTML code of the newsletter to the system clipboard.
	 * @param newsletter
	 */
	private static void copyToClipboard(Newsletter newsletter)
	{
		StringSelection stringSelection = new StringSelection(newsletter.toHTML());
		Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
		clpbrd.setContents(stringSelection, null);
		
	}
	/**
	 * All tags for variables and XML.
	 * 
	 * @author Maurice
	 * 
	 */
	public static final class Tags {
		public static final String WEEK_NUMMER = "week-nummer";
		public static final String INLEIDING_CONTENT = "inleiding-content";
		public static final String INLEIDING_EMAILCLIENT = "inleiding-emailclient";
		public static final String AGENDA_LIJST_LINKS = "agenda-lijst-links";
		public static final String AGENDA_LIJST_RECHTS = "agenda-lijst-rechts";
		public static final String JAAR = "jaar";
	
		public static final String EVENEMENTEN = "evenementen";
		public static final String EVENEMENT = "evenement";
		public static final String ITEM = "item";
	
		public static final class Evenement {
	
			public static final String EVENEMENT_NAAM = "evenement-naam";
			public static final String EVENEMENT_BESCHRIJVING = "evenement-beschrijving";
			public static final String WAT = "wat";
			public static final String TITEL = "titel";
			public static final String WAAR = "waar";
			public static final String WANNEER = "wanneer";
			public static final String PRIJS = "prijs";
			public static final String BANNER = "banner";
			public static final String INAGENDA = "inagenda";
			public static final String DATUMAGENDA = "datumagenda";
			public static final String HEEFTKORTING = "heeftkorting";
			public static final String KOSTEN = "kosten";
			public static final String AANMELDDEADLINE = "aanmelddeadline";
		}
	
	}
	public static final class HTML {
	
		public static final String color = "#EE227A";
	
		public static final String HEADER_FONTS = "Calibri, 'Trebuchet MS', sans-serif";
		public static final String BODY_FONTS = "Arial, sans-serif";
		
		
		public static final String TD_STYLE = "padding-left: 30px; padding-right: 30px; padding-bottom: 10px; margin: 0px; vertical-align: top;";
		public static final String H2_STYLE = "color: " + color + "; font-family: "+HEADER_FONTS+"; font-size: 18px; margin-bottom: 10px;";
		public static final String BANNER_STYLE = "width: 640px;";
		public static final String EVENT_STYLE = "margin-top: 10px; margin-bottom: 10px;";
		public static final String EVENT_CONTENT_STYLE = "overflow: auto; font-family: "+BODY_FONTS+";";
		public static final String EVENT_DESCRIBER_CONTENT_STYLE = "font-family: "+BODY_FONTS+"; font-size: 15px; color: white; ";
		public static final String EVENT_DESCRIBER_HEADER_STYLE = "font-family: "+HEADER_FONTS+";font-size: 15px; color: "+color+";";
		public static final String EVENT_DESCRIPTION_STYLE = "width: 200px;	float:right; border-left-style: solid; border-color: "
				+ color + "; border-width: 2px; padding: 10px; height: auto;";
	
	}
	

}
