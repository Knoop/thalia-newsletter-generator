package com.knoop.nieuwsbrief.content;

import com.knoop.nieuwsbrief.Main;
import com.knoop.nieuwsbrief.Newsletter;

public class Evenement extends Item {

	public Evenement(int id, Newsletter newsletter) {
		super(id, newsletter);
		super.requireField(Fields.WANNEER, "onbekend");
		super.requireField(Fields.WAAR, "onbekend");
	}

	@Override
	protected String onBodyFormatting(boolean includesSideColumn) {
		
		StringBuilder builder = new StringBuilder();
		builder.append(super.getContent());
		
		if(super.anySet("kosten","prijs"))
		{
			// Open disclaimer
			builder.append("<p style=\"font-family: ").append(Main.HTML.BODY_FONTS).append("\"><i><b>Let op:</b> Aanmelddeadline = Afmelddeadline! Niet of niet op tijd afmelden betekent de door Thalia per persoon gemaakte kosten betalen.");
			
			String prijs = super.get("kosten","prijs");
			// If a proper price has been mentioned, use it.
			if(prijs != null && isMoney(prijs))
				builder.append(" Deze bedragen <b>&euro;").append(prijs);
			
			// Close it
			builder.append(".</b></i></p>");
		}
		
		return builder.toString();
	}

	public void onValuesAssigned()
	{
		// If the when isn't set, don't show it in agenda unless specified otherwise.
		if(!isSet(Fields.WANNEER))
			super.overwriteIfEmpty(Fields.INAGENDA, "false");
		
		if(this.inAgenda() && isSet(Fields.WANNEER) && !isSet(Fields.DATUMAGENDA))
		{
			System.out.println("No date given for calender. Will try to find it using value of field \""+Fields.WANNEER+"\"");
			super.overwrite(Fields.DATUMAGENDA, createDateForCalender(super.get(Fields.WANNEER)));
		}
			
	}
	
	
	private static String createDateForCalender(String string) {

		String[] parts = string.replace(",","").toLowerCase().split(" "); 
		for(int i = 0 ; i < parts.length - 1;++i)
			if(parts[i].matches("\\d\\d?") && Integer.parseInt(parts[i]) < 31 && parts[i+1].matches("januari|februari|maart|april|mei|juni|juli|augustus|september|oktober|november|december"))
				return parts[i]+" "+parts[i+1];
		
		System.out.println("Couldn't create the calendar date from \""+string+"\". Are you sure that it containes a day (number) followed by a month (in Dutch)?");
		return "onbekend";
	}
	
	

	@Override
	protected String onSideColumnFormatting() {
		
		StringBuilder builder = new StringBuilder();
		builder	.append("<div style=\"padding: 20px; background-color: black; border-left: 5px solid "+Main.HTML.color+"\">\n");
		writeDescriberLine("wat", this.getWat(), builder);
		
		
		if(isSet(Fields.PRIJS))
		{
			String prijs = super.get(Fields.PRIJS);
			if(isMoney(prijs))
				super.overwrite(Fields.PRIJS, "&euro;"+prijs);
			
		}
			
		for(String item : new String[]{Fields.WAAR,Fields.WANNEER,Fields.PRIJS,Fields.AANMELDDEADLINE})
		{
			if(super.isSet(item))
				writeDescriberLine(item, super.get(item), builder);
		}
		
		builder	.append("</div>\n");
		
		return builder.toString();
		
	}

	protected boolean usesSideColumn()
	{
		return super.anySet(Fields.WAAR, Fields.WAT, Fields.WANNEER, Fields.PRIJS, Fields.KOSTEN, Fields.AANMELDDEADLINE);
	}

	public static class Fields
	{
		public static final String AANMELDDEADLINE = "aanmelddeadline";
		public static final String WAT = "wat";
		public static final String WAAR = "waar";
		public static final String WANNEER = "wanneer";
		public static final String PRIJS = "prijs";
		public static final String KOSTEN = "kosten";
		public static final String INAGENDA = "inagenda";
		public static final String DATUMAGENDA = "datumagenda";
	}

	public boolean inAgenda() {
		
		return super.getBool("inagenda", true);
	}

	public String getDateForCalendar() {
		
		if(this.isSet(Fields.DATUMAGENDA))
			return this.get(Fields.DATUMAGENDA);
		else if(this.isSet(Fields.WANNEER))
			return this.get(Fields.WANNEER);
		else return "onbekend";
	}



	public String getWat() {
		
		if(this.isSet(Fields.WAT))
			return this.get(Fields.WAT);
		else
			return this.getTitle();
		

	}
	
	private static boolean isMoney(String value)
	{
		return value.matches("\\d+(,(\\d\\d|-))?");
	}
	
	private static void writeDescriberLine(String tag, String value, StringBuilder builder)
	{
		if(tag.length() == 0 || value.length() == 0)
			throw new IllegalArgumentException("Tag or value contained nothing");
		
		builder.append("<div style=\"margin-bottom: 5px;\">\n");
		// Header line, in uppercase, bold and dark pink
		builder
					.append("<b style=\"").append(Main.HTML.EVENT_DESCRIBER_HEADER_STYLE).append("\">")
					.append(tag.toUpperCase()).append("</b><br/>\n")
		// Value line, regular color, but always ending with a period. 
					.append("<span style=\"").append(Main.HTML.EVENT_DESCRIBER_CONTENT_STYLE).append("\">")
					.append(value).append(value.charAt(value.length()-1) == '.'?"":".").append("<br/></span>\n");
		builder.append("</div>\n");
	}
}
