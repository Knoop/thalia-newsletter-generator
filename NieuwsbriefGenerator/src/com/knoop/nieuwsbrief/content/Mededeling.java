package com.knoop.nieuwsbrief.content;

import com.knoop.nieuwsbrief.Newsletter;

public class Mededeling extends Item {
	

	public Mededeling(int id, Newsletter newsletter) {
		super(id, newsletter);
		super.requireField("auteur");
		
		
	}
	

	@Override
	protected String onBodyFormatting(boolean includesSideColumn) {
		
		StringBuilder sb = new StringBuilder();
		sb.append(super.getContent());
		if(super.isSet("namens"))
			sb.append("<p>Namens ").append(super.get("namens")).append(",</p>\n");
		
		sb.append("<p>").append(super.get("auteur")).append("<br/>");
		
		if(super.isSet("auteur_titel"))
			sb.append("<i>").append(super.get("auteur_titel")).append("</i>\n");
		
		sb.append("</p>");
		
		return sb.toString();
	}

	@Override
	protected boolean usesSideColumn() {
		return false;
	}
	
	

}
