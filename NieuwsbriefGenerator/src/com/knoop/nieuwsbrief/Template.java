package com.knoop.nieuwsbrief;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

public final class Template {

	private final StringBuilder content;

	private final HashMap<String, String> values = new HashMap<>();

	private boolean isProcessed = false;
	private boolean areKeysSet = false;

	/**
	 * Create a new template for a given source file.
	 * 
	 * @param file
	 *            The File from which to create the template
	 * @param keys
	 *            The keys that ought to be provided. These keys are without the
	 *            sequence indicating variables.
	 */
	public Template(File file) {
		StringBuilder temp = null;
		try {
			temp = readTemplate(file);
		} catch (Exception e) {

			throw new IllegalArgumentException(e);
		}

		this.content = temp;

	}

	/**
	 * Reads the template from the given file. 
	 * @param file 
	 * @return
	 * @throws IOException
	 */
	private StringBuilder readTemplate(File file) throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(file)); 
		
		StringBuilder contentBuilder = new StringBuilder();
		while(reader.ready())
			contentBuilder.append(reader.readLine());
				
		reader.close();
		
		return contentBuilder;
			
	}

	/**
	 * Replace all instances of a variable in the template with the given
	 * string. This can be overwritten at any time until the Template is written
	 * to a new file.
	 * 
	 * 
	 * @param key
	 *            The key to replace. Note that this should be without any
	 *            special characters indicating that it is a variable.
	 * @param value
	 */
	public Template fill(String key, String value) {
		this.values.put(key, value);
		return this;
	}

	/**
	 * 
	 * 
	 * @param file
	 * @throws IOException 
	 */
	public Template process() {
		
		
		
		// check for undefined keys
		for (String value : values.values())
			if (value == null)
				throw new IllegalStateException("Not all keys are defined.");

		this.isProcessed = true;

		// Replace all keys
		for (Entry<String, String> entry : values.entrySet())
		{
			String key = this.templateVariable(entry.getKey());
			
			// Replace all indices of the key by the value. 
			int index = content.indexOf(key);
			while(index >= 0)
			{
				content.replace(index, index+key.length(), entry.getValue());
				index = content.indexOf(key);
			}
		}
		
		return this;
		
		
		
		
	}
	
	/**
	 * Get the content of the filled in template. This requires that the template has been processed. 
	 * @return
	 */
	public String publish()
	{
		if(!this.isProcessed)
			throw new IllegalStateException();
		return this.content.toString();
	}
	
	public void publish(File file) throws IOException
	{
		if(!this.isProcessed)
			throw new IllegalStateException();
		
		// First open the file, make sure it works.
		FileWriter fw = new FileWriter(file);
		
		// Now write to the opened FileWriter, and close it.
		fw.write(content.toString());
		fw.close();
	}
	

	/**
	 * Indicates whether this Template has been written to a file. If it has
	 * been written, then it can no longer be used to fill in keys.
	 * 
	 * @return true if the file has already been written to a file.
	 */
	public final boolean isProcessed() {
		return this.isProcessed;
	}

	/**
	 * 
	 * @param keys
	 */
	public void requireKeys(String...keys) {
		
		if(this.areKeysSet)
			throw new IllegalStateException("The required keys have already been set.");
		
		this.areKeysSet = true;
		
		for (String key : keys)
			if (!values.containsKey(key))
				values.put(key, null);
	}

	/**
	 * Turns the given key into the string how it is shown in the template.
	 * 
	 * @param key The key of the variable that needs to changed into the style that is used in the template.
	 * @return  A representation of the variable as used in the template. 
	 */
	protected String templateVariable(String key) {
		return "%" + key + "%";
	}

}
