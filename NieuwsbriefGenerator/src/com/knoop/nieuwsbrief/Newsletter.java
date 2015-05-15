package com.knoop.nieuwsbrief;

import java.util.LinkedList;
import java.util.List;

import com.knoop.nieuwsbrief.content.Evenement;
import com.knoop.nieuwsbrief.content.Item;

import datatype.XMLAttribute;
import datatype.XMLNode;

public class Newsletter {

	private final String year, weeknumber;

	private final String emailpreheader;

	private final List<Item> items = new LinkedList<>();

	private String html;
	
	Newsletter(XMLNode node) {
		
		System.out.println("----------");
		System.out.println("Going to process xml");
		
		// De basis gegevens
		year = node.getAttribute(Main.Tags.JAAR).getValue();
		weeknumber = node.getAttribute(Main.Tags.WEEK_NUMMER).getValue();
		
		// De inhoud
		readItems(node.getChildrenByTag(Main.Tags.ITEM));
		

		// De emailinleiding, direct als tekst.
		XMLAttribute emailpreheaderAttrib = node.getAttribute(Main.Tags.INLEIDING_EMAILCLIENT);
		if (emailpreheaderAttrib != null)
		{
			
			emailpreheader = emailpreheaderAttrib.getValue();
			System.out.println("Read email preheader from file.");
		}
		else
		{
			System.out.println("No email preheader available. Going to generate one instead.");
			emailpreheader = generatePreheader();
			System.out.println("Generated preheader: "+emailpreheader);
		}

	}

	private void readItems(List<XMLNode> items) {
		
		System.out.println("Going to process content. "+items.size()+" items defined in file.");
		int counter = 0;
		for(XMLNode itemNode : items)
		{
			
			try
			{
				this.items.add(Item.fromXML(counter, this, itemNode));
				System.out.println("Processed item "+counter);
			}
			catch(IllegalArgumentException e)
			{
				System.out.println("Couldn't process item "+counter+" going to abort.");
				throw new IllegalStateException(e);
			}
			++counter;
			
		}
		
		System.out.println("All items have been processed succesfully.");
		
		
	}

	/**
	 * Generates a text like "De Thalia nieuwsbrief! Met deze
	 * 
	 * @return
	 */
	private String generatePreheader() {

		StringBuilder preheader = new StringBuilder();

		List<Evenement> events = getEvents();

		if (events.size() > 0) {
			preheader.append("Met deze week ");

			preheader.append(events.get(0).getWat());
			if (events.size() > 3) {
				// Append the next two events.
				for (int i = 1; i < 3; ++i)
					preheader.append(", ").append(events.get(i).getWat());
				preheader.append(" en meer!");
			} else {
				// For every remaining event that is not the last, add it with a
				// comma
				for (int i = 1; i < events.size() - 1; ++i)
					preheader.append(", ").append(events.get(i).getWat());
				// Append the last element, using "en".
				preheader.append(" en ").append(events.get(events.size() - 1).getWat())
						.append(".");
			}
		} else {
			preheader.append("De Thalia nieuwsbrief van week ")
					.append(weeknumber).append(".");
		}

		return preheader.toString();

	}

	public String getYear() {
		return year;
	}

	public String getWeeknumber() {
		return weeknumber;
	}

	public List<Item> getItems() {
		return this.items;
	}

	public List<Evenement> getEvents() {

		
		List<Evenement> events = new LinkedList<>();
		for (Item item : this.getItems())
			if (item instanceof Evenement)
				events.add((Evenement) item);

		return events;
	}

	public String getEmailClientPreheader() {
		//
		return this.emailpreheader;
	}

	public List<Item> getCalendarEvents() {

		List<Item> calendarEvents = new LinkedList<>();
		for (Evenement event : this.getEvents())
			if (event.getBool(Evenement.Fields.INAGENDA))
				calendarEvents.add(event);

		return calendarEvents;
	}
	
	public String toHTML()
	{
		if(this.html == null)
		{
			StringBuilder content = new StringBuilder();
			
			for(Item item : this.items)
				content.append(item.toHTML()).append('\n');
			
			this.html = new Template(Main.getNewsletterTemplateFile())
			.fill("jaar",this.getYear() + "")
			.fill("week-nummer",this.getWeeknumber() + "")
			.fill("inleiding-emailclient", this.getEmailClientPreheader())
			.fill("content", content.toString())
			.process()
			.publish();
		}
		
		return this.html;
		
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((emailpreheader == null) ? 0 : emailpreheader.hashCode());
		result = prime * result + ((items == null) ? 0 : items.hashCode());
		result = prime * result
				+ ((weeknumber == null) ? 0 : weeknumber.hashCode());
		result = prime * result + ((year == null) ? 0 : year.hashCode());
		return result;
	}

}
