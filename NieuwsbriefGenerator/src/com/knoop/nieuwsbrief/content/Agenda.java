package com.knoop.nieuwsbrief.content;

import java.util.LinkedList;
import java.util.List;

import com.knoop.nieuwsbrief.Main;
import com.knoop.nieuwsbrief.Newsletter;

public class Agenda extends Item {

	public Agenda(int id, Newsletter newsletter) {
		super(id, newsletter);
		super.overwrite(Item.Fields.TITEL, "agenda");
	}

	@Override
	protected String onBodyFormatting(boolean includesSideColumn) {
		StringBuilder builder = new StringBuilder();
		
		List<Evenement> events = super.newsletter.getEvents();
		
		/*
		 * Find and split all events that are supposed to be in the Agenda.
		 */
		LinkedList<LinkedList<Evenement>> agendaevents = new LinkedList<LinkedList<Evenement>>();
		agendaevents.add(new LinkedList<Evenement>());
		agendaevents.add(new LinkedList<Evenement>());
		
		for(Evenement event : events)
			if(event.inAgenda())
				agendaevents.get(0).add(event);
		
		while(agendaevents.get(0).size() > agendaevents.get(1).size() + 1)
			agendaevents.get(1).add(agendaevents.get(0).removeLast());
		
		builder.append("<table style=\"width: 100%\"><tr>");
		
		/*
		 * Make the agenda.
		 */
		for(LinkedList<Evenement> list : agendaevents) 
		{
			
			builder.append("<td style=\"vertical-align: top;\">");
			builder.append("<ul>");
			
			for(Evenement event : list)
			{
				builder.append("<li style=\"font-family: ").append(Main.HTML.BODY_FONTS).append(";\"><i>").append(event.getWat())
				.append(" - ").append(event.getDateForCalendar())
				.append("</i></li>\n");
			}
			
			builder.append("</ul>");
			builder.append("</td>");
		}
		
		builder.append("</tr></table>");
		
		return builder.toString();
		
	}

	@Override
	protected boolean usesSideColumn() {
		return false;
	}
	
	

}
