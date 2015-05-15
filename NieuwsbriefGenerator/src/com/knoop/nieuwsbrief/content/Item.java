package com.knoop.nieuwsbrief.content;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map.Entry;

import com.knoop.nieuwsbrief.Main;
import com.knoop.nieuwsbrief.Newsletter;

import datatype.XMLAttribute;
import datatype.XMLNode;

public abstract class Item {

	private final HashMap<String, String> mapping;

	private final HashMap<FieldIndex, String> requiredFields;

	private final int id;
	/**
	 * The content of the item.
	 */
	private String content;

	/**
	 * The Newsletter this Item is part of.
	 */
	protected final Newsletter newsletter;

	protected Item(int id, Newsletter newsletter) {
		this.id = id;
		this.mapping = new HashMap<>();
		this.requiredFields = new HashMap<>();
		this.newsletter = newsletter;
		// Only a title is required by default.
		this.requireField(Fields.TITEL);
	}

	/**
	 * Set the given value for the given field and type.
	 * 
	 * @param type
	 * @param field
	 * @param value
	 */
	private boolean set(String type, String field, String value) {
		if(value == null)
			return this.mapping.remove(index(type, field)) != null;
		else
			return this.mapping.put(index(type, field), value) != null;
	}

	/**
	 * Overwrite the value set for this type on the given field.
	 * 
	 * @param field
	 * @param value
	 * @return true if a value already existed, false otherwise.
	 */
	protected boolean overwrite(String field, String value) {
		return this.set(callingType(), field, value);
	}

	/**
	 * Overwrite the value set for this type on the given field, but only if no value has been defined for that field.
	 * 
	 * @param field
	 * @param value
	 * @return true if a value already existed, false otherwise.
	 */
	protected boolean overwriteIfEmpty(String field, String value)
	{
		if(!this.isSet(field))
			return overwrite(field, value);
		else 
			return true;
	}
	private void setContent(String body) {
		this.content = body;
	}

	protected String getContent() {
		return this.content;
	}

	/**
	 * Requires the given field for the calling type.
	 * 
	 * @param field
	 *            The XML field that is required.
	 */
	protected void requireField(String... fields) {
		String type = callingType();
		for (String field : fields)
			this.requireField(type, field, null);

	}
	
	/**
	 * Requires the given field for the calling type.
	 * 
	 * @param field
	 *            The XML field that is required.
	 */
	protected void requireField(String field, String defVal) {
		this.requireField(callingType(), field, defVal);
			

	}
	
	private void requireField(String type, String field, String defVal)
	{
		this.requiredFields.put(new FieldIndex(type, field), defVal);
	}

	/**
	 * Checks whether the
	 * 
	 * @param andOror
	 * @param fields
	 * @return
	 */
	protected boolean isSet(String field) {
		return this.mapping.get(index(field)) != null;

	}

	/**
	 * Checks whether all given fields are set for the type that has called this
	 * method.
	 * 
	 * @param fields
	 *            All fields that should be set
	 * @return true if all fields have a value, false if at least one of those
	 *         fields does not have a value.
	 */
	protected boolean allSet(String... fields) {

		boolean result = true;

		for (int i = 0; i < fields.length && result; ++i)
			result &= isSet(fields[i]);

		return result;
	}

	/**
	 * Checks whether any of the given given fields are set for the type that
	 * has called this method.
	 * 
	 * @param fields
	 *            The fields of which at least one should be set
	 * @return true if there is at least one field of the given fields that has
	 *         a value, false if none of the given fields have a value.
	 */
	protected boolean anySet(String... fields) {

		boolean result = false;
		for (int i = 0; i < fields.length && !result; ++i)
			result |= isSet(fields[i]);

		return result;
	}

	public String getTitle() {
		return this.get(Fields.TITEL);
	}

	/**
	 * Tries to obtain the given fields. They are obtained in order. If the
	 * first field can not be obtained, then the second will be attempted. If
	 * none of them are found null is returned.
	 * 
	 * As soon as a field is found, it is returned.
	 * 
	 * @param fields
	 * @return
	 */
	public String get(String... fields) {
		for (String field : fields)
			if (isSet(field))
				return this.get(field);
		return null;
	}

	/**
	 * Get the String value stored for the given type and field
	 * 
	 * @param type
	 * @param field
	 * @return
	 */
	public String get(String field) {
		return this.mapping.get(index(field));
	}

	/**
	 * Get the boolean value for the given type and field.
	 * 
	 * @param type
	 * @param field
	 * @return The boolean, but only if the value stored was a boolean.
	 */
	public boolean getBool(String field, boolean defVal) {
		String value = this.get(field);
		Boolean bvalue = Boolean.valueOf(value);
		if (Boolean.toString(bvalue).equalsIgnoreCase(value))
			return bvalue;
		else
			return defVal;

	}

	/**
	 * Get the boolean value for the given type and field.
	 * 
	 * @param type
	 * @param field
	 * @return The boolean, but only if the value stored was a boolean.
	 */
	public boolean getBool(String field) {
		String value = this.get(field);
		Boolean bvalue = Boolean.valueOf(value);
		if (Boolean.toString(bvalue).equalsIgnoreCase(value))
			return bvalue;
		else
			throw new IllegalStateException("The value stored for \""
					+ index(field) + "\" is not a boolean");

	}

	public int getInt(String field) {
		return Integer.parseInt(this.get(field));
	}

	public int getInt(String field, int defVal) {
		try {
			return Integer.parseInt(this.get(field));

		} catch (NumberFormatException e) {
			return defVal;
		}

	}

	public double getDouble(String field) {
		return Double.parseDouble(this.get(field));
	}

	public double getDouble(String field, double defVal) {
		try {
			return Double.parseDouble(this.get(field));

		} catch (NumberFormatException e) {
			return defVal;
		}
	}

	/**
	 * Get the String index in the mapping of fields to value for this type and
	 * the given field.
	 * 
	 * @param field
	 * @return
	 */
	private String index(String field) {
		return this.index(this.getClass().getSimpleName(), field);
	}

	/**
	 * Get the String index in the mapping of fields to value for the given type
	 * and field.
	 * 
	 * @param type
	 * @param field
	 * @return
	 */
	private String index(String type, String field) {
		
		return field;
	}

	/**
	 * Obtains the class name of the object that called the method that called
	 * callingType().
	 * 
	 * @return The class name of the object that called the method that called
	 *         callingType().
	 */
	private String callingType() {
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
		/*
		 * element 0 is getStackTrace, element 1 is currentThread, element 2 is
		 * callingType, element 3 is always this class element 4 is the caller,
		 */
		return trace[4].getClassName();
	}

	/**
	 * Validates whether each required field has a value.
	 */
	private void validateRequiredFields() {
		for (Entry<FieldIndex, String> requirement : this.requiredFields.entrySet())
			if (!mapping.containsKey(index(requirement.getKey().type,requirement.getKey().field)))
				if(requirement.getValue() == null)
					// If no default was defined, then it is simply empty, so exception.
					throw new EmptyRequiredFieldException(requirement.getKey().field, id);
				else 
					// If a default was defined, use it. 
					this.overwrite(requirement.getKey().field, requirement.getValue());
						
	}

	public final String toHTML() {
		// First validate that it can be turned to HTML.
		this.validateRequiredFields();

		boolean usesSideColumn = this.usesSideColumn();
		StringBuilder builder = new StringBuilder();

		/*
		 * Create a title
		 */
		builder.append("<tr>\n");
		builder.append("<td colspan=\"2\" style=\"").append(Main.HTML.TD_STYLE)
				.append(" vertical-align: bottom; padding-top: 30px; \">\n");
		builder.append("<h2 style=\"").append(Main.HTML.H2_STYLE).append("\">");
		builder.append(this.getTitle().toUpperCase());
		builder.append("</h2>\n");
		builder.append("</tr>");

		/*
		 * Create the row for the body and side column
		 */
		builder.append("<tr>\n");

		/*
		 * Create the body. The style depends on whether the side column exists.
		 */
		builder.append("<td style=\"").append(Main.HTML.TD_STYLE);

		if (usesSideColumn())
			builder.append("padding-right: 20px;");

		builder.append("\"");

		if (!usesSideColumn())
			builder.append(" colspan=\"2\"");

		builder.append(">\n");
		// get the body
		builder.append(this.onBodyFormatting(usesSideColumn));
		builder.append("\n</td>\n");

		/*
		 * Perform all steps for the side column.
		 */
		if (usesSideColumn()) {
			// get the side column
			String sidecolumn = this.onSideColumnFormatting();
			builder.append("<td style=\"vertical-align: top;\">\n");

			if (sidecolumn != null)
				builder.append(sidecolumn);
			builder.append("</td>\n");
		}

		builder.append("</tr>\n");
		return builder.toString();

	}

	/**
	 * Called when all values have been filled from XML, and before the fields are validated. Use this to check values if required.
	 */
	protected void onValuesAssigned() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Creates the HTML code for the body.
	 * 
	 * @param includesSideColumn
	 * @return
	 */
	protected String onBodyFormatting(boolean includesSideColumn) {
		return this.getContent();
	}

	/**
	 * Creates HTML code for the side column.
	 * 
	 * @return The generated HTML code, or null if it should remain empty.
	 */
	protected String onSideColumnFormatting() {
		return null;
	}

	/**
	 * <p>
	 * Indicates whether this item uses the Side Column. When set to false, the
	 * body will span both the regular body and the side column.
	 * </p>
	 * <p>
	 * If the value is set to true, then the body and side column will be
	 * formatted separately. In other words, onSideColumnFormatting will be
	 * called. It is not required to actually do anything in the side column
	 * when the value is set to true. If you do not want to do anything, don't
	 * overwrite onSideColumnFormatting, and it will only be used as an empty
	 * place. This can also be achieved by letting onSideColumnFormatting return
	 * null.
	 * </p>
	 * 
	 * @return true if it uses the side column. False if it doesn't
	 */
	protected boolean usesSideColumn() {
		return false;
	}

	protected static class Fields {
		protected static final String TITEL = "titel";
	}

	/**
	 * Creates the correct Item from the given XMLNode.
	 * 
	 * @param node
	 * @return
	 */
	public static Item fromXML(int id, Newsletter newsletter, XMLNode node) {
		try {

			// Get the Type, but with capital.
			XMLAttribute typeAttrib = node.getAttribute("type");
			if(typeAttrib == null)
				throw new NoTypeDefinedException(id);
			
			String type = typeAttrib.getValue();
			type = Character.toUpperCase(type.charAt(0)) + type.substring(1);

			// Create the correct instance.
			Item item = (Item) Class
					.forName(Item.class.getPackage().getName() + '.' + type)
					.getConstructor(int.class, Newsletter.class).newInstance(id, newsletter);

			item.setContent(node.getFullContent(false));
			
			// Register all attributes that are type:field
			for (XMLAttribute attribute : node.getAttributes()) {
				String[] naming = attribute.getName().split(":");
				if (naming.length == 1)
					item.set(null, naming[0], attribute.getValue());
				else if(naming.length == 2)
					item.set(naming[0], naming[1], attribute.getValue());
			}
			
			// Notify that all values have been assigned. 
			item.onValuesAssigned();

			item.validateRequiredFields();
			
			return item;

		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			throw new IllegalArgumentException(
					"Couldn't create Item from node. The defined type doesn't exist.");
		}

	}
	
	@SuppressWarnings("serial")
	public static final class NoTypeDefinedException extends IllegalStateException { 
		
		private final int id;
		
		public NoTypeDefinedException(int id) {
			super();
			this.id = id;
		}
		public int getId() {
			return id;
		}
	}
	
	@SuppressWarnings("serial")
	public static final class EmptyRequiredFieldException extends IllegalStateException{
		
		private final String field;
		private final int id;
		
		public EmptyRequiredFieldException(String field, int id) {
			super();
			this.field = field;
			this.id = id;
		}
		public String getField() {
			return field;
		}
		public int getId() {
			return id;
		}
		
		
	}
	
	
	private static final class FieldIndex {
		
		private final String type, field;
		
		private FieldIndex(String type, String field)
		{
			this.type = type;
			this.field = field;
		}
		
	}
}
